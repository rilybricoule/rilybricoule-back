package com.sbsolutions.rilybricoule.repository;


import com.sbsolutions.rilybricoule.entity.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {

    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    void deleteByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    boolean existsByBlockerIdAndBlockedIdOrBlockerIdAndBlockedId(
            Long blocker1, Long blocked1,
            Long blocker2, Long blocked2
    );
}
