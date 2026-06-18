package com.sbsolutions.rilybricoule.services;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class TwoFAService {
    // ↑ Ce service gère toute la logique 2FA
    //   Il ne touche pas à la BDD — c'est le controller qui le fait

    // ── Générer un nouveau secret ─────────────────────────────────
    public String generateSecret() {
        SecretGenerator generator = new DefaultSecretGenerator(32);
        return generator.generate();
        // ↑ Retourne un string aléatoire comme "JBSWY3DPEHPK3PXP..."
        //   Ce secret est partagé entre le backend et Google Authenticator
        //   C'est la "clé" qui permet de générer les mêmes codes des deux côtés
    }

    // ── Générer le QR Code en image Base64 ────────────────────────
    public String generateQrCodeBase64(String secret, String email) {
        // ↑ Crée une image QR Code que l'admin peut scanner
        //   avec Google Authenticator

        QrData data = new QrData.Builder()
                .label(email)
                // ↑ Ce qui s'affiche dans Google Authenticator sous le code
                .secret(secret)
                // ↑ Le secret encodé dans le QR
                .issuer("RiLyBricoule Admin")
                // ↑ Le nom de l'app dans Google Authenticator
                .algorithm(HashingAlgorithm.SHA1)
                // ↑ L'algorithme de hachage (SHA1 est le standard TOTP)
                .digits(6)
                // ↑ Codes à 6 chiffres (standard)
                .period(30)
                // ↑ Un nouveau code toutes les 30 secondes
                .build();

        QrGenerator generator = new ZxingPngQrGenerator();
        try {
            byte[] imageData = generator.generate(data);
            return Base64.getEncoder().encodeToString(imageData);
            // ↑ Convertit l'image PNG en texte Base64
            //   Le frontend peut l'afficher avec : <img src="data:image/png;base64,..." />
        } catch (QrGenerationException e) {
            throw new RuntimeException("Erreur lors de la génération du QR code", e);
        }
    }

    // ── Vérifier un code à 6 chiffres ─────────────────────────────
    public boolean verifyCode(String secret, String code) {
        // ↑ Vérifie si le code entré par l'admin est correct
        //   secret = le secret stocké en BDD pour cet admin
        //   code   = le code à 6 chiffres entré par l'admin (ex: "482937")

        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);

        return verifier.isValidCode(secret, code);
        // ↑ La librairie :
        //   1. Prend l'heure actuelle
        //   2. Génère le code attendu avec le secret + l'heure
        //   3. Compare avec le code entré
        //   4. Retourne true si ça match
        //   Elle accepte aussi le code précédent et suivant (±30s)
        //   pour gérer les décalages d'horloge
    }
}
