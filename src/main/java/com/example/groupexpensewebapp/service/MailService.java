package com.example.groupexpensewebapp.service;

import com.example.groupexpensewebapp.dto.DebtNotificationInput;
import com.example.groupexpensewebapp.model.Member;
import com.example.groupexpensewebapp.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final MemberRepository memberRepository;

    public void sendDebtNotification(DebtNotificationInput input, String username) {
        Member sender = memberRepository.findByRelatedUserName_AndGroup_Id(username, input.getGroupId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        Member recipient = memberRepository.findByRelatedUserName_AndGroup_Id(input.getRecipientUsername(), input.getGroupId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        String emailAddress = recipient.getRelatedUser().getEmail();
        sendMessage(emailAddress,
                "Powiadomienie o długu",
                "Cześć, " + recipient.getName() + ". " + sender.getName() + " przypomina ci o długu w wysokości " +
                        String.format("%.2f", (double) input.getAmount() / 100) + " zł. Sprawdź:\n" +
                        "http://localhost:4200/groups/" + input.getGroupId()
        );
    }

    private void sendMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("debt.tracking.app@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
