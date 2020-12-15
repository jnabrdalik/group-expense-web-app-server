package com.example.groupexpensewebapp.service;

import com.example.groupexpensewebapp.dto.MemberInput;
import com.example.groupexpensewebapp.dto.MemberDetails;
import com.example.groupexpensewebapp.model.Group;
import com.example.groupexpensewebapp.model.Member;
import com.example.groupexpensewebapp.model.User;
import com.example.groupexpensewebapp.repository.GroupRepository;
import com.example.groupexpensewebapp.repository.MemberRepository;
import com.example.groupexpensewebapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public MemberDetails addMember(MemberInput input, long groupId, String username) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        if (group.isArchived() ||
                group.isForRegisteredOnly() && !memberRepository.existsByRelatedUserName_AndGroup_Id(username, groupId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (input.getName() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        Member member = new Member();
        member.setName(input.getName());
        member.setGroup(group);

        String relatedUserName = input.getRelatedUserName();
        if (relatedUserName != null) {
            User relatedUser = userRepository.findByName(relatedUserName);
            if (relatedUser == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }

            if (memberRepository.existsByRelatedUserName_AndGroup_Id(relatedUser.getName(), group.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }

            member.setRelatedUser(relatedUser);
            member.setName(relatedUser.getName());
        }

        Member addedMember = memberRepository.save(member);
        return modelMapper.map(addedMember, MemberDetails.class);
    }

    public MemberDetails editMember(long memberId, MemberInput input, String username) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Group group = member.getGroup();
        if (group.isArchived() || group.isForRegisteredOnly() &&
                !memberRepository.existsByRelatedUserName_AndGroup_Id(username, group.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (input.getName() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        member.setName(input.getName());

        memberRepository.save(member);
        return modelMapper.map(member, MemberDetails.class);
    }

    public void deleteMember(long memberId, String username) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Group group = member.getGroup();
        if (group.isArchived() || group.isForRegisteredOnly() &&
                !memberRepository.existsByRelatedUserName_AndGroup_Id(username, group.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        User relatedUser = member.getRelatedUser();
        if (relatedUser != null && group.getCreator().getId() == relatedUser.getId()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete group creator!");
        }

        // easier and cleaner than checking manually
        try {
            memberRepository.deleteById(memberId);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete. Database error");
        }
    }
}
