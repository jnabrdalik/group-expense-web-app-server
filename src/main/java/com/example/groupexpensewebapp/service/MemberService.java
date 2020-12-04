package com.example.groupexpensewebapp.service;

import com.example.groupexpensewebapp.dto.DebtNotificationInput;
import com.example.groupexpensewebapp.dto.MemberInput;
import com.example.groupexpensewebapp.dto.MemberSummary;
import com.example.groupexpensewebapp.model.Group;
import com.example.groupexpensewebapp.model.Member;
import com.example.groupexpensewebapp.model.UserEntity;
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

    private final MemberRepository repository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public MemberSummary addMember(MemberInput input, String username) {
        Group group = groupRepository.findById(input.getGroupId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        if (group.isArchived() ||
                group.isForRegisteredOnly() && !repository.existsByRelatedUserName_AndGroup_Id(username, input.getGroupId())) {
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
            UserEntity relatedUser = userRepository.findByName(relatedUserName);
            if (relatedUser == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }

            if (repository.existsByRelatedUserName_AndGroup_Id(relatedUser.getName(), group.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already in this group!");
            }

            member.setRelatedUser(relatedUser);
            member.setName(relatedUser.getName());
        }

        Member addedMember = repository.save(member);
        return modelMapper.map(addedMember, MemberSummary.class);
    }

//    public MemberSummary addUserAsMember()

    public MemberSummary editMember(long memberId, MemberInput input, String username) {
        Member member = repository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Group group = member.getGroup();
        if (group.isArchived() || group.isForRegisteredOnly() &&
                !repository.existsByRelatedUserName_AndGroup_Id(username, group.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (input.getName() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        member.setName(input.getName());

        repository.save(member);
        return modelMapper.map(member, MemberSummary.class);
    }

    public void deleteMember(long memberId, String username) {
        Member member = repository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Group group = member.getGroup();
        if (group.isArchived() || group.isForRegisteredOnly() &&
                !repository.existsByRelatedUserName_AndGroup_Id(username, group.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        UserEntity relatedUser = member.getRelatedUser();
        if (relatedUser != null && groupRepository.existsByIdAndCreator_Name(group.getId(), relatedUser.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete group creator!");
        }

        try {
            repository.deleteById(memberId);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete. Db error");
        }

    }

//    public void sendInvite(long memberId, String email, String username) {
//        if (!mailService.isEmailValid(email)) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
//        }
//
//        Person person = repository.findById(memberId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
//
//        long groupId = person.getGroup().getId();
////        if (!repository.existsByRelatedUserName_AndGroup_Id(username, groupId)) {
////            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
////        }
//
//        UserEntity user = new UserEntity();
//        userRepository.save(user);
//
//        person.setRelatedUser(user);
//        repository.save(person);
//
//        mailService.sendMessage(email, "Zaproszenie do aplikacji", "Cześć, " + person.getName() +
//                "! Zapraszam cię do grupy " + person.getGroup().getName() + ". Aby się zarejestrować, kliknij " +
//                "w poniższy link:\n" + "http://localhost:4200/sign-up/invite/" + memberId);
//    }
}
