package com.sbsolutions.rilybricoule.security.infrastructure.oauth2;

import com.sbsolutions.rilybricoule.dto.SocialUserInfo;
import com.sbsolutions.rilybricoule.entity.AuthProvider;

public interface OAuth2TokenVerifier {

    SocialUserInfo verify(String idToken);

    AuthProvider getProvider();
}
