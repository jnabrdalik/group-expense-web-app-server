package com.example.groupexpensewebapp.controller;

import com.example.groupexpensewebapp.dto.MemberInput;
import com.example.groupexpensewebapp.dto.MemberSummary;
import com.example.groupexpensewebapp.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public MemberSummary addMember(@RequestBody MemberInput input, Principal principal) {
        return memberService.addMember(input, principal != null ? principal.getName() : null);
    }

    @PutMapping("/{id}")
    public MemberSummary editMember(@PathVariable long id, @RequestBody MemberInput input, Principal principal) {
        return memberService.editMember(id, input, principal.getName());
    }

    @DeleteMapping("/{id}")
    public void deleteMember(@PathVariable long id, Principal principal) {
        memberService.deleteMember(id, principal.getName());
    }

}
