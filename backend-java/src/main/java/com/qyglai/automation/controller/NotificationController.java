package com.qyglai.automation.controller;

import java.util.List;

import com.qyglai.automation.common.ApiResponse;
import com.qyglai.automation.dto.NotificationCreateRequest;
import com.qyglai.automation.entity.MessageNotificationEntity;
import com.qyglai.automation.service.AutomationWorkspaceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消息通知接口。
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final AutomationWorkspaceService service;

    public NotificationController(AutomationWorkspaceService service) {
        this.service = service;
    }

    /**
     * 创建消息通知。
     *
     * @param request 消息创建请求
     * @return 消息通知实体
     */
    @PostMapping
    public ApiResponse<MessageNotificationEntity> create(@Valid @RequestBody NotificationCreateRequest request) {
        return ApiResponse.ok(service.createNotification(request.channel(), request.title(), request.content()));
    }

    /**
     * 查询消息通知。
     *
     * @return 消息通知列表
     */
    @GetMapping
    public ApiResponse<List<MessageNotificationEntity>> list() {
        return ApiResponse.ok(service.listNotifications());
    }
}

