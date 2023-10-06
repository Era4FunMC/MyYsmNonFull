package me.earthme.mysm.utils.ysm

import org.jetbrains.annotations.Contract
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


object EncryptUtils {
    @Throws(Exception::class)
    fun encryptDataWithKnownKey(paramArrayOfbyte1: ByteArray?, paramArrayOfbyte2: ByteArray?): ByteArray {
        val secretKeySpec = SecretKeySpec(paramArrayOfbyte1, "AES")
        val ivParameterSpec = IvParameterSpec(paramArrayOfbyte1)
        return encrypt(secretKeySpec, ivParameterSpec, paramArrayOfbyte2).toByteArray()
    }

    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    @Throws(IOException::class, GeneralSecurityException::class)
    fun encrypt(secretKey: SecretKey?, paramAlgorithmParameterSpec: AlgorithmParameterSpec?, data: ByteArray?): ByteArrayOutputStream {
        val byteArrayInputStream = ByteArrayInputStream(data)
        val byteArrayOutputStream = ByteArrayOutputStream()
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, paramAlgorithmParameterSpec)
        val buffer = ByteArray(64)
        var bytesRead: Int
        while (byteArrayInputStream.read(buffer).also { bytesRead = it } != -1) {
            val encryptedData = cipher.update(buffer, 0, bytesRead)
            if (encryptedData != null) {
                byteArrayOutputStream.write(encryptedData)
            }
        }
        val finalData = cipher.doFinal()
        if (finalData != null) {
            byteArrayOutputStream.write(finalData)
        }
        return byteArrayOutputStream
    }

    @Throws(IOException::class, GeneralSecurityException::class)
    fun decrypt(secretKey: SecretKey?, paramAlgorithmParameterSpec: AlgorithmParameterSpec?, data: ByteArray?): ByteArrayOutputStream {
        val byteArrayInputStream = ByteArrayInputStream(data)
        val byteArrayOutputStream = ByteArrayOutputStream()
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, paramAlgorithmParameterSpec)
        val buffer = ByteArray(64)
        var bytesRead: Int
        while (byteArrayInputStream.read(buffer).also { bytesRead = it } != -1) {
            val decryptedData = cipher.update(buffer, 0, bytesRead)
            if (decryptedData != null) {
                byteArrayOutputStream.write(decryptedData)
            }
        }
        val finalData = cipher.doFinal()
        if (finalData != null) {
            byteArrayOutputStream.write(finalData)
        }
        return byteArrayOutputStream
    }

    fun generateSecretKey(): SecretKey {
        val keyGenerator: KeyGenerator? = try {
            KeyGenerator.getInstance("AES")
        } catch (noSuchAlgorithmException: NoSuchAlgorithmException) {
            throw RuntimeException(noSuchAlgorithmException)
        }
        keyGenerator!!.init(128)
        return keyGenerator.generateKey()
    }

    @Contract(value = "_ -> new", pure = true)
    fun generateSecretKey(keyData: ByteArray?): SecretKey {
        return SecretKeySpec(keyData, "AES")
    }

    @Contract(" -> new")
    fun generateIV(): IvParameterSpec {
        val ivBytes = ByteArray(16)
        SecureRandom().nextBytes(ivBytes)
        return IvParameterSpec(ivBytes)
    }
}

