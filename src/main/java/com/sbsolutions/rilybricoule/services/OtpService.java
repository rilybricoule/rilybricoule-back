package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.entity.OtpCode;
import com.sbsolutions.rilybricoule.entity.OtpPurpose;
import com.sbsolutions.rilybricoule.repository.OtpCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OtpCodeRepository otpCodeRepository;
    private final EmailService emailService;

    @Transactional
    public void generateAndSendOtp(String email, OtpPurpose purpose) {
        otpCodeRepository.deleteByEmailAndPurpose(email, purpose);

        String code = generateCode();

        OtpCode otpCode = OtpCode.builder()
                .email(email)
                .code(code)
                .purpose(purpose)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .build();

        otpCodeRepository.save(otpCode);

        String subject;
        String purposeText;

        if (purpose == OtpPurpose.EMAIL_VERIFICATION) {
            subject = "RilyBricoule - Vérification de votre adresse email";
            purposeText = "Utilisez le code ci-dessous pour vérifier votre adresse email et activer votre compte :";
        } else {
            subject = "RilyBricoule - Réinitialisation de votre mot de passe";
            purposeText = "Utilisez le code ci-dessous pour réinitialiser votre mot de passe :";
        }

        emailService.sendOtpEmail(email, code, subject, purposeText);
    }

    @Transactional
    public boolean verifyOtp(String email, String code, OtpPurpose purpose) {
        OtpCode otpCode = otpCodeRepository
                .findFirstByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(email, purpose)
                .orElse(null);

        if (otpCode == null) {
            return false;
        }

        if (otpCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        if (!otpCode.getCode().equals(code)) {
            return false;
        }

        otpCode.setUsed(true);
        otpCodeRepository.save(otpCode);
        return true;
    }

    private String generateCode() {
        int bound = (int) Math.pow(10, OTP_LENGTH);
        int number = RANDOM.nextInt(bound);
        return String.format("%0" + OTP_LENGTH + "d", number);
    }
}
