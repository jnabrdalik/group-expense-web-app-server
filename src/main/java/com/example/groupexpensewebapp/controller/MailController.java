package com.example.groupexpensewebapp.controller;

import com.example.groupexpensewebapp.dto.DebtNotificationInput;
import com.example.groupexpensewebapp.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("api/mail")
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;

    @PostMapping("debt-notification")
    public void sendDebtNotification(@RequestBody DebtNotificationInput input, Principal principal) {
        mailService.sendDebtNotification(input, principal.getName());
    }
}
