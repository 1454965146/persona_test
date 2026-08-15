package com.persona.service;

import com.persona.model.PaymentOrder;
import com.persona.model.Report;
import com.persona.model.User;
import com.persona.repository.PaymentOrderRepository;
import com.persona.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_PAID = "PAID";

    private final ReportRepository reportRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final ReportService reportService;

    @Value("${payment.report-price-cents:990}") private int reportPriceCents;
    @Value("${payment.mock-enabled:true}") private boolean mockEnabled;

    public PaymentService(ReportRepository reportRepository,
                          PaymentOrderRepository paymentOrderRepository,
                          ReportService reportService) {
        this.reportRepository = reportRepository;
        this.paymentOrderRepository = paymentOrderRepository;
        this.reportService = reportService;
    }

    @Transactional
    public Map<String, Object> createOrder(String reportCode, User currentUser) {
        Report report = requireOwnedReport(reportCode, currentUser);
        if (Boolean.TRUE.equals(report.getPremiumUnlocked())) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("alreadyPaid", true);
            result.put("reportCode", report.getReportCode());
            result.put("premiumUnlocked", true);
            return result;
        }

        List<PaymentOrder> orders = paymentOrderRepository
                .findByReportIdAndUserIdOrderByCreatedAtDesc(report.getId(), currentUser.getId());
        PaymentOrder pending = orders.stream()
                .filter(order -> STATUS_PENDING.equals(order.getStatus()))
                .findFirst()
                .orElse(null);
        if (pending != null) {
            return orderView(pending);
        }

        PaymentOrder order = new PaymentOrder();
        order.setOrderNo(nextOrderNo());
        order.setReport(report);
        order.setUser(currentUser);
        order.setAmountCents(reportPriceCents);
        order.setStatus(STATUS_PENDING);
        order.setProvider(mockEnabled ? "MOCK_WECHAT" : "WECHAT");
        order.setUpdatedAt(LocalDateTime.now());
        order = paymentOrderRepository.save(order);
        return orderView(order);
    }

    @Transactional
    public Map<String, Object> mockPaySuccess(String orderNo, User currentUser) {
        if (!mockEnabled) {
            throw new RuntimeException("模拟支付未启用");
        }
        PaymentOrder order = paymentOrderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new RuntimeException("支付订单不存在"));
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("无权操作该支付订单");
        }
        if (STATUS_PAID.equals(order.getStatus())) {
            return paidOrderView(order);
        }
        if (!STATUS_PENDING.equals(order.getStatus())) {
            throw new RuntimeException("支付订单状态异常，请重新下单");
        }

        order.setStatus(STATUS_PAID);
        order.setProviderTradeNo("MOCK_" + order.getOrderNo());
        order.setPaidAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        paymentOrderRepository.save(order);
        reportService.generatePremiumReport(order.getReport());
        return paidOrderView(order);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getOrder(String orderNo, User currentUser) {
        PaymentOrder order = paymentOrderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new RuntimeException("支付订单不存在"));
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("无权访问该支付订单");
        }
        return orderView(order);
    }

    private Report requireOwnedReport(String reportCode, User currentUser) {
        Report report = reportRepository.findByReportCode(reportCode)
                .orElseThrow(() -> new RuntimeException("报告不存在"));
        if (report.getUser() == null || !report.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("无权操作该报告");
        }
        return report;
    }

    private Map<String, Object> orderView(PaymentOrder order) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderNo", order.getOrderNo());
        result.put("reportCode", order.getReport().getReportCode());
        result.put("amountCents", order.getAmountCents());
        result.put("amountYuan", String.format("%.2f", order.getAmountCents() / 100.0));
        result.put("status", order.getStatus());
        result.put("provider", order.getProvider());
        result.put("mockPay", mockEnabled);
        result.put("alreadyPaid", STATUS_PAID.equals(order.getStatus()));
        result.put("premiumUnlocked", Boolean.TRUE.equals(order.getReport().getPremiumUnlocked()));
        if (!mockEnabled && STATUS_PENDING.equals(order.getStatus())) {
            result.put("wechatParams", mockWechatParams(order));
        }
        return result;
    }

    private Map<String, Object> paidOrderView(PaymentOrder order) {
        Map<String, Object> result = orderView(order);
        result.put("paidAt", order.getPaidAt() == null ? null : order.getPaidAt().toString());
        return result;
    }

    private Map<String, Object> mockWechatParams(PaymentOrder order) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        params.put("nonceStr", randomHex(16));
        params.put("package", "prepay_id=" + randomHex(24));
        params.put("signType", "RSA");
        params.put("paySign", randomHex(32));
        return params;
    }

    private String nextOrderNo() {
        return "P" + System.currentTimeMillis() + randomDigits(6);
    }

    private String randomDigits(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) sb.append(RANDOM.nextInt(10));
        return sb.toString();
    }

    private String randomHex(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) sb.append(Integer.toHexString(RANDOM.nextInt(16)));
        return sb.toString();
    }
}
