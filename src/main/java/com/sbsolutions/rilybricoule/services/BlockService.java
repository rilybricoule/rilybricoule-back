package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.entity.User;
import com.sbsolutions.rilybricoule.entity.UserBlock;
import com.sbsolutions.rilybricoule.repository.UserBlockRepository;
import com.sbsolutions.rilybricoule.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BlockService {

    private final UserBlockRepository userBlockRepository;
    private final UserRepository userRepository;

    @Transactional
    public void blockUser(Long blockerId, Long blockedId) {
        if (blockerId == null || blockedId == null) {
            throw new IllegalArgumentException("blockerId and blockedId are required");
        }

        if (blockerId.equals(blockedId)) {
            throw new IllegalArgumentException("You cannot block yourself");
        }

        // idempotent: already blocked => do nothing
        if (userBlockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)) {
            return;
        }

        User blocker = userRepository.findById(blockerId)
                .orElseThrow(() -> new RuntimeException("Blocker not found: " + blockerId));

        User blocked = userRepository.findById(blockedId)
                .orElseThrow(() -> new RuntimeException("Blocked user not found: " + blockedId));

        UserBlock block = UserBlock.builder()
                .blocker(blocker)
                .blocked(blocked)
                .build();

        userBlockRepository.save(block);
    }

    @Transactional
    public void unblockUser(Long blockerId, Long blockedId) {
        if (blockerId == null || blockedId == null) {
            throw new IllegalArgumentException("blockerId and blockedId are required");
        }
        userBlockRepository.deleteByBlockerIdAndBlockedId(blockerId, blockedId);
    }

    @Transactional(readOnly = true)
    public boolean isBlockedOneWay(Long blockerId, Long blockedId) {
        return userBlockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId);
    }

    @Transactional(readOnly = true)
    public boolean isBlockedEitherWay(Long userA, Long userB) {
        return userBlockRepository.existsByBlockerIdAndBlockedIdOrBlockerIdAndBlockedId(
                userA, userB,
                userB, userA
        );
    }

    // Helper for chat sending rule
    @Transactional(readOnly = true)
    public void assertMessagingAllowed(Long senderId, Long receiverId) {
        boolean blocked = isBlockedEitherWay(senderId, receiverId);
        if (blocked) {
            throw new IllegalStateException("Messaging is blocked between these users");
        }
    }
}
