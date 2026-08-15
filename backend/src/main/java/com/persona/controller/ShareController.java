package com.persona.controller;

import com.persona.dto.ApiResponse;
import com.persona.dto.ShareCreateRequest;
import com.persona.model.User;
import com.persona.service.AuthService;
import com.persona.service.ShareService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/share")
public class ShareController {
    private final ShareService shareService;
    private final AuthService authService;

    public ShareController(ShareService shareService, AuthService authService) {
        this.shareService = shareService;
        this.authService = authService;
    }

    @PostMapping("/create")
    public ApiResponse<?> createShare(@Valid @RequestBody ShareCreateRequest r) {
        User user = authService.requireCurrentUser();
        return ApiResponse.success(shareService.createShare(
                r.getReportCode(),
                r.getRelationshipType(),
                Boolean.TRUE.equals(r.getAllowInviteeView()),
                user));
    }
    @GetMapping("/{shareCode}")
    public ApiResponse<?> getShareInfo(@PathVariable String shareCode) {
        return ApiResponse.success(shareService.getShareInfo(shareCode));
    }
    @PostMapping("/{shareCode}/bind")
    public ApiResponse<?> bindInvitee(@PathVariable String shareCode, @RequestBody Map<String, String> body) {
        User user = authService.requireCurrentUser();
        return ApiResponse.success(shareService.bindInviteeReport(shareCode, body.get("reportCode"), user));
    }
    @GetMapping("/by-report/{reportCode}")
    public ApiResponse<?> getLinksByReport(@PathVariable String reportCode) {
        User user = authService.requireCurrentUser();
        return ApiResponse.success(shareService.getLinksByReport(reportCode, user));
    }
}
