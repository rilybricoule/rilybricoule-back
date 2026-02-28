package com.sbsolutions.rilybricoule.security.infrastructure.oauth2;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.sbsolutions.rilybricoule.dto.SocialUserInfo;
import com.sbsolutions.rilybricoule.entity.AuthProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;

@Component
public class AppleTokenVerifier implements OAuth2TokenVerifier {

    private static final String APPLE_JWKS_URL = "https://appleid.apple.com/auth/keys";
    private static final String APPLE_ISSUER = "https://appleid.apple.com";

    private final String clientId;

    public AppleTokenVerifier(@Value("${oauth2.apple.client-id}") String clientId) {
        this.clientId = clientId;
    }

    @Override
    public SocialUserInfo verify(String idToken) {
        try {
            ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
            JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(new URL(APPLE_JWKS_URL));
            jwtProcessor.setJWSKeySelector(
                    new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, keySource));

            JWTClaimsSet claims = jwtProcessor.process(idToken, null);

            if (!APPLE_ISSUER.equals(claims.getIssuer())) {
                throw new IllegalArgumentException("Invalid Apple token issuer");
            }
            if (!claims.getAudience().contains(clientId)) {
                throw new IllegalArgumentException("Invalid Apple token audience");
            }

            String email = claims.getStringClaim("email");
            String sub = claims.getSubject();

            String firstName = "";
            String lastName = "";

            return SocialUserInfo.builder()
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .providerId(sub)
                    .build();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to verify Apple token: " + e.getMessage());
        }
    }

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.APPLE;
    }
}
