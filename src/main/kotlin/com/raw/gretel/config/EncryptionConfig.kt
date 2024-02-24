package com.raw.gretel.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.security.SecureRandom
import java.util.*
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@Configuration
class EncryptionConfig(
    @Value("\${encryption.secretKey:#{null}}")
    private val existingSecretKey: String
) {

    @Bean
    fun secretKey(): SecretKey {
        if (existingSecretKey.isNullOrEmpty()) {
            val secretKey = generateSecretKey(256)
            println(secretKeyToString(secretKey))
            return secretKey
        }
        return stringToSecretKey(existingSecretKey)
    }




    final fun generateSecretKey(size: Int): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(size)
        return keyGenerator.generateKey()
    }

    fun secretKeyToString(secretKey: SecretKey): String {
        val rawData = secretKey.encoded
        return Base64.getEncoder().encodeToString(rawData)
    }

    fun stringToSecretKey(secretKeyAsString: String): SecretKey {
        val decodedKey = Base64.getDecoder().decode(secretKeyAsString)
        return SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
    }
}