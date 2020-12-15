package com.example.groupexpensewebapp.controller;

import com.example.groupexpensewebapp.dto.MemberDetails;
import com.example.groupexpensewebapp.dto.MemberInput;
import com.example.groupexpensewebapp.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/{groupId}")
    public MemberDetails addMember(@RequestBody MemberInput input, @PathVariable long groupId, Principal principal) {
        return memberService.addMember(input, groupId, principal != null ? principal.getName() : null);
    }

    @PutMapping("/{id}")
    public MemberDetails editMember(@PathVariable long id, @RequestBody MemberInput input, Principal principal) {
        return memberService.editMember(id, input, principal.getName());
    }

    @DeleteMapping("/{id}")
    public void deleteMember(@PathVariable long id, Principal principal) {
        memberService.deleteMember(id, principal.getName());
    }

}
