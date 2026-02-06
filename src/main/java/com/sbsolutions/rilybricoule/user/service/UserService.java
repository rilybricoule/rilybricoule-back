package com.sbsolutions.rilybricoule.user.service;

import java.util.List;

public interface UserService {

    void assignRoleToUser(Long userId, List<Long> roleIds);
}
