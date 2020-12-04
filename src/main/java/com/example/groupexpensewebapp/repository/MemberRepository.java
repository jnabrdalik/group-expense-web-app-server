package com.example.groupexpensewebapp.repository;

import com.example.groupexpensewebapp.model.Member;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface MemberRepository extends CrudRepository<Member, Long> {

    boolean existsByRelatedUserName_AndGroup_Id(String username, long groupId);

    Optional<Member> findByRelatedUserName_AndGroup_Id(String username, long groupId);
}
