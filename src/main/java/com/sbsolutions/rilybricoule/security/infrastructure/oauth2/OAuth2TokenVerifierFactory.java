package com.sbsolutions.rilybricoule.security.infrastructure.oauth2;

import com.sbsolutions.rilybricoule.entity.AuthProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OAuth2TokenVerifierFactory {

    private final Map<AuthProvider, OAuth2TokenVerifier> verifiers;

    public OAuth2TokenVerifierFactory(List<OAuth2TokenVerifier> verifierList) {
        this.verifiers = verifierList.stream()
                .collect(Collectors.toMap(OAuth2TokenVerifier::getProvider, Function.identity()));
    }

    public OAuth2TokenVerifier getVerifier(AuthProvider provider) {
        OAuth2TokenVerifier verifier = verifiers.get(provider);
        if (verifier == null) {
            throw new IllegalArgumentException("Unsupported OAuth2 provider: " + provider);
        }
        return verifier;
    }
}
