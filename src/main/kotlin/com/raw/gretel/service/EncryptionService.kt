package com.raw.gretel.service

import com.raw.gretel.domain.Iv
import com.raw.gretel.repository.IvRepository
import javassist.NotFoundException
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

@Service
class EncryptionService(
    private val ivRepository: IvRepository,
    private val secretKey: SecretKey,
) {

    companion object {
        const val encryptionAlgorithm = "AES/CBC/PKCS5Padding"
    }

    fun getIv(encryptedId: String): IvParameterSpec {
        var iv: IvParameterSpec? = null
        ivRepository.findById(encryptedId).ifPresent { iv = IvParameterSpec(Base64.getDecoder().decode(it.iv))}
        return iv ?: throw NotFoundException("Initialize vector not found")
    }

    fun findIv(userId: String): Iv {
        return ivRepository.findAll()
            .first { decrypt(it.id, IvParameterSpec(Base64.getDecoder().decode(it.iv))) == userId }
    }
//[62, -81, 92, -77, 7, 74, -83, 26, 7, -64, 37, 19, -81, -83, 76, 126]
    fun save(userId: String): Iv {
        val generatedIv = generateIv()
        val encryptedId = encrypt(userId, generatedIv)
        return ivRepository.save(Iv(encryptedId, Base64.getEncoder().encodeToString(generatedIv.iv)))
    }

    fun encrypt(input: String, iv: IvParameterSpec): String {
        val cipher = Cipher.getInstance(encryptionAlgorithm)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv)
        val cipherText = cipher.doFinal(input.toByteArray())
        return Base64.getEncoder().encodeToString(cipherText)
    }

    fun decrypt(cipherText: String, iv: IvParameterSpec): String {
        val cipher = Cipher.getInstance(encryptionAlgorithm)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)
        val plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText))
        return String(plainText)
    }

    private fun generateIv(): IvParameterSpec {
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        return IvParameterSpec(iv)
    }
}