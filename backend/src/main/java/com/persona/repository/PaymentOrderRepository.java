package com.persona.repository;

import com.persona.model.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {
    Optional<PaymentOrder> findByOrderNo(String orderNo);
    Optional<PaymentOrder> findFirstByReportIdAndUserIdOrderByCreatedAtDesc(Long reportId, Long userId);
    List<PaymentOrder> findByReportIdAndUserIdOrderByCreatedAtDesc(Long reportId, Long userId);
}
