package com.payment.gateway.infrastructure.security.tokenization;

import com.payment.gateway.commons.exception.DomainException;
import com.payment.gateway.commons.utils.CryptoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for tokenizing sensitive card data.
 * Tokenization replaces sensitive data (like card numbers) with non-sensitive placeholders (tokens).
 * The original data is stored securely in a token vault.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenizationService {

    /**
     * In-memory token vault for demonstration purposes.
     * In production, this should be replaced with a secure database with encryption at rest.
     */
    private final ConcurrentHashMap<String, TokenVaultEntity> tokenVault = new ConcurrentHashMap<>();

    private static final int TOKEN_LENGTH = 16;
    private static final String TOKEN_PREFIX = "tok_";

    /**
     * Tokenizes a card number.
     *
     * @param cardNumber the plain card number to tokenize
     * @return the generated token
     */
    public String tokenize(String cardNumber) {
        if (cardNumber == null || cardNumber.isBlank()) {
            throw new DomainException("Card number cannot be null or empty");
        }

        // Normalize card number (remove spaces and dashes)
        String normalizedCardNumber = cardNumber.replaceAll("[\\s-]", "");

        if (!isValidCardNumberFormat(normalizedCardNumber)) {
            throw new DomainException("Invalid card number format");
        }

        // Check if we already have a token for this card number
        String cardHash = hashCardNumber(normalizedCardNumber);
        String existingToken = findTokenByCardHash(cardHash);

        if (existingToken != null) {
            log.debug("Returning existing token for card number");
            return existingToken;
        }

        // Generate new token
        String token = generateToken();

        // Store in vault
        TokenVaultEntity vaultEntry = new TokenVaultEntity(
            token,
            cardHash,
            normalizedCardNumber,
            extractLastFourDigits(normalizedCardNumber),
            extractCardBrand(normalizedCardNumber),
            Instant.now(),
            true
        );

        tokenVault.put(token, vaultEntry);

        log.info("Generated new token for card ending in: {}", vaultEntry.getLastFourDigits());

        return token;
    }

    /**
     * Detokenizes a token to retrieve the original card number.
     * This operation should be restricted to authorized services only.
     *
     * @param token the token to detokenize
     * @param authorizationContext the authorization context for this detokenization request
     * @return the original card number
     */
    public String detokenize(String token, String authorizationContext) {
        if (token == null || token.isBlank()) {
            throw new DomainException("Token cannot be null or empty");
        }

        // Validate authorization context (in production, this would check user permissions)
        if (!isAuthorizedForDetokenization(authorizationContext)) {
            throw new DomainException("Unauthorized: insufficient permissions for detokenization");
        }

        TokenVaultEntity vaultEntry = tokenVault.get(token);

        if (vaultEntry == null) {
            log.warn("Detokenization failed: token not found");
            throw new DomainException("Invalid token: token not found in vault");
        }

        if (!vaultEntry.isActive()) {
            log.warn("Detokenization failed: token is inactive");
            throw new DomainException("Token is no longer active");
        }

        log.info("Detokenization successful for token: {}", maskToken(token));

        return vaultEntry.getCardNumber();
    }

    /**
     * Retrieves token metadata without exposing the card number.
     *
     * @param token the token to look up
     * @return token metadata
     */
    public TokenMetadata getTokenMetadata(String token) {
        TokenVaultEntity vaultEntry = tokenVault.get(token);

        if (vaultEntry == null) {
            return null;
        }

        return new TokenMetadata(
            token,
            vaultEntry.getLastFourDigits(),
            vaultEntry.getCardBrand(),
            vaultEntry.getCreatedAt(),
            vaultEntry.isActive()
        );
    }

    /**
     * Deactivates a token (lifecycle management).
     *
     * @param token the token to deactivate
     */
    public void deactivateToken(String token) {
        TokenVaultEntity vaultEntry = tokenVault.get(token);

        if (vaultEntry != null) {
            vaultEntry.setActive(false);
            log.info("Token deactivated: {}", maskToken(token));
        }
    }

    /**
     * Reactivates a previously deactivated token.
     *
     * @param token the token to reactivate
     */
    public void reactivateToken(String token) {
        TokenVaultEntity vaultEntry = tokenVault.get(token);

        if (vaultEntry != null) {
            vaultEntry.setActive(true);
            log.info("Token reactivated: {}", maskToken(token));
        }
    }

    /**
     * Validates if a token exists and is active.
     *
     * @param token the token to validate
     * @return true if the token is valid and active
     */
    public boolean isValidToken(String token) {
        TokenVaultEntity vaultEntry = tokenVault.get(token);
        return vaultEntry != null && vaultEntry.isActive();
    }

    private String generateToken() {
        return TOKEN_PREFIX + UUID.randomUUID().toString().replace("-", "").substring(0, TOKEN_LENGTH);
    }

    private String hashCardNumber(String cardNumber) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(cardNumber.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new DomainException("Failed to hash card number", e);
        }
    }

    private String findTokenByCardHash(String cardHash) {
        return tokenVault.values().stream()
            .filter(entry -> entry.getCardHash().equals(cardHash))
            .filter(TokenVaultEntity::isActive)
            .map(TokenVaultEntity::getToken)
            .findFirst()
            .orElse(null);
    }

    private boolean isValidCardNumberFormat(String cardNumber) {
        // Luhn algorithm validation
        if (!cardNumber.matches("\\d{13,19}")) {
            return false;
        }

        int sum = 0;
        boolean alternate = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return sum % 10 == 0;
    }

    private String extractLastFourDigits(String cardNumber) {
        if (cardNumber.length() >= 4) {
            return cardNumber.substring(cardNumber.length() - 4);
        }
        return cardNumber;
    }

    private String extractCardBrand(String cardNumber) {
        if (cardNumber.startsWith("4")) {
            return "VISA";
        } else if (cardNumber.startsWith("5")) {
            return "MASTERCARD";
        } else if (cardNumber.startsWith("3")) {
            return "AMEX";
        } else if (cardNumber.startsWith("6")) {
            return "DISCOVER";
        }
        return "UNKNOWN";
    }

    private boolean isAuthorizedForDetokenization(String authorizationContext) {
        // In production, this would verify user permissions/roles
        // For now, we accept specific authorization contexts
        return "PAYMENT_PROCESSOR".equals(authorizationContext) ||
               "REFUND_SERVICE".equals(authorizationContext) ||
               "ADMIN".equals(authorizationContext);
    }

    private String maskToken(String token) {
        if (token == null || token.length() <= 8) {
            return "****";
        }
        return token.substring(0, 4) + "****" + token.substring(token.length() - 4);
    }
}
