package com.payment.gateway.infrastructure.security.encryption;

import com.payment.gateway.commons.exception.DomainException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for encrypting and decrypting sensitive data at rest.
 * Uses AES-256-GCM for authenticated encryption.
 */
@Slf4j
@Service
public class EncryptionService {

    private static final String AES_ALGORITHM = "AES";
    private static final String AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 128; // 128 bits
    private static final int KEY_SIZE = 256;

    /**
     * In-memory key store for demonstration purposes.
     * In production, use a proper KMS (Key Management Service) like AWS KMS, HashiCorp Vault, etc.
     */
    private final ConcurrentHashMap<String, KeyVersion> keyStore = new ConcurrentHashMap<>();

    private final SecureRandom secureRandom;
    private final String masterKeyId;

    public EncryptionService(
            @Value("${encryption.master-key:default-dev-master-key-change-in-production}") String masterKeySeed) {
        this.secureRandom = new SecureRandom();
        this.masterKeyId = initializeMasterKey(masterKeySeed);
    }

    /**
     * Encrypts plain text data.
     *
     * @param plainText the data to encrypt
     * @return encrypted data with key version and IV prepended
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            throw new DomainException("Plain text cannot be null or empty");
        }

        try {
            KeyVersion currentKey = getKeyVersion(masterKeyId);

            Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
            byte[] iv = generateIv();
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            cipher.init(Cipher.ENCRYPT_MODE, currentKey.getSecretKey(), parameterSpec);

            byte[] plainTextBytes = plainText.getBytes(StandardCharsets.UTF_8);
            byte[] cipherTextBytes = cipher.doFinal(plainTextBytes);

            // Combine key version, IV, and ciphertext
            ByteBuffer buffer = ByteBuffer.allocate(4 + iv.length + cipherTextBytes.length);
            buffer.putInt(currentKey.getVersion());
            buffer.put(iv);
            buffer.put(cipherTextBytes);

            String encryptedData = Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.array());

            log.debug("Data encrypted successfully with key version: {}", currentKey.getVersion());

            return encryptedData;

        } catch (Exception e) {
            log.error("Encryption failed: {}", e.getMessage(), e);
            throw new DomainException("Failed to encrypt data", e);
        }
    }

    /**
     * Decrypts encrypted data.
     *
     * @param encryptedData the encrypted data (includes key version and IV)
     * @return decrypted plain text
     */
    public String decrypt(String encryptedData) {
        if (encryptedData == null || encryptedData.isBlank()) {
            throw new DomainException("Encrypted data cannot be null or empty");
        }

        try {
            byte[] encryptedBytes = Base64.getUrlDecoder().decode(encryptedData);

            // Extract key version
            ByteBuffer buffer = ByteBuffer.wrap(encryptedBytes);
            int keyVersion = buffer.getInt();

            // Extract IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            buffer.get(iv);

            // Extract ciphertext
            byte[] cipherTextBytes = new byte[buffer.remaining()];
            buffer.get(cipherTextBytes);

            // Get the key used for encryption
            KeyVersion keyVersionObj = getKeyVersion(String.valueOf(keyVersion));
            if (keyVersionObj == null) {
                // Try with master key for backward compatibility
                keyVersionObj = getKeyVersion(masterKeyId);
            }

            Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            cipher.init(Cipher.DECRYPT_MODE, keyVersionObj.getSecretKey(), parameterSpec);

            byte[] plainTextBytes = cipher.doFinal(cipherTextBytes);

            log.debug("Data decrypted successfully with key version: {}", keyVersion);

            return new String(plainTextBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("Decryption failed: {}", e.getMessage(), e);
            throw new DomainException("Failed to decrypt data", e);
        }
    }

    /**
     * Rotates the encryption key.
     * Old data can still be decrypted using the key version stored with it.
     *
     * @return the new key version number
     */
    public int rotateKey() {
        int newVersion = keyStore.size() + 1;
        String newKeyId = String.valueOf(newVersion);

        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
            keyGenerator.init(KEY_SIZE, secureRandom);
            SecretKey secretKey = keyGenerator.generateKey();

            KeyVersion newKey = new KeyVersion(newKeyId, secretKey, Instant.now(), true);
            keyStore.put(newKeyId, newKey);

            log.info("Key rotated successfully. New key version: {}", newVersion);

            return newVersion;

        } catch (NoSuchAlgorithmException e) {
            throw new DomainException("Failed to generate new encryption key", e);
        }
    }

    /**
     * Re-encrypts data with the latest key version.
     * Use this during key rotation to migrate old data.
     *
     * @param oldEncryptedData data encrypted with an old key
     * @return data re-encrypted with the current key
     */
    public String reEncryptWithCurrentKey(String oldEncryptedData) {
        String plainText = decrypt(oldEncryptedData);
        return encrypt(plainText);
    }

    /**
     * Gets the current key version.
     *
     * @return the current key version number
     */
    public int getCurrentKeyVersion() {
        return keyStore.get(masterKeyId).getVersion();
    }

    private String initializeMasterKey(String masterKeySeed) {
        try {
            // Derive a key from the seed using PBKDF2 or similar in production
            // For now, use the seed directly (not recommended for production)
            byte[] keyBytes = masterKeySeed.getBytes(StandardCharsets.UTF_8);
            byte[] paddedKeyBytes = new byte[32]; // 256 bits
            System.arraycopy(keyBytes, 0, paddedKeyBytes, 0, Math.min(keyBytes.length, 32));

            SecretKey secretKey = new SecretKeySpec(paddedKeyBytes, AES_ALGORITHM);
            KeyVersion masterKey = new KeyVersion("1", secretKey, Instant.now(), true);
            keyStore.put("1", masterKey);

            log.info("Master key initialized");

            return "1";

        } catch (Exception e) {
            throw new DomainException("Failed to initialize master key", e);
        }
    }

    private KeyVersion getKeyVersion(String keyId) {
        KeyVersion key = keyStore.get(keyId);
        if (key == null) {
            throw new DomainException("Encryption key not found: " + keyId);
        }
        return key;
    }

    private byte[] generateIv() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);
        return iv;
    }

    /**
     * Represents an encryption key with version information.
     */
    private static class KeyVersion {
        @Getter
        private final String keyId;
        @Getter
        private final SecretKey secretKey;
        @Getter
        private final Instant createdAt;
        @Getter
        private final int version;
        @Getter
        private boolean active;

        KeyVersion(String keyId, SecretKey secretKey, Instant createdAt, boolean active) {
            this.keyId = keyId;
            this.secretKey = secretKey;
            this.createdAt = createdAt;
            this.active = active;
            this.version = Integer.parseInt(keyId);
        }

        void setActive(boolean active) {
            this.active = active;
        }
    }
}
