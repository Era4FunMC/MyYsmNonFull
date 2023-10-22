package me.earthme.mysm.model.loaders.impl

import com.google.common.collect.Maps
import me.earthme.mysm.ResourceConstants
import me.earthme.mysm.model.IModelLoader
import me.earthme.mysm.model.YsmModelData
import me.earthme.mysm.utils.FileUtils
import me.earthme.mysm.utils.RSAEncryptUtils
import me.earthme.mysm.utils.ysm.AESEncryptUtils
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.File
import java.nio.file.Files
import java.security.MessageDigest
import java.security.Signature
import java.util.*
import java.util.function.Function
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class MyYsmModelLoaderImpl: IModelLoader{
    override fun canLoad(modelFile: File): Boolean {
        return !modelFile.isDirectory && modelFile.name.endsWith(".mysm")
    }

    override fun loadModel(modelFile: File, authChecker: Function<String, Boolean>): YsmModelData {
        val allFiles = readFromMyYsmFile(DataInputStream(ByteArrayInputStream(Files.readAllBytes(modelFile.toPath()))))

        val metaData: MutableMap<String, ByteArray> = Maps.newHashMap()
        val animationData: MutableMap<String, ByteArray> = Maps.newHashMap()
        val textureData: MutableMap<String, ByteArray> = Maps.newHashMap()

        val missingFileNames: MutableList<String> = ArrayList(
            listOf(
                "main.json",
                "arm.json",
                "main.animation.json",
                "arm.animation.json",
                "extra.animation.json"
            )
        )

        for ((fileName,data) in allFiles) {
            missingFileNames.remove(fileName)
            if (!fileName.endsWith(".png")) {
                when (fileName) {
                    "main.json" -> metaData["main"] = data
                    "arm.json" -> metaData["arm"] = data
                    "main.animation.json" -> animationData["main"] = data
                    "arm.animation.json" -> animationData["arm"] = data
                    "extra.animation.json" -> animationData["extra"] = data
                }
            } else {
                textureData[fileName] = data
            }
        }

        for (missingFileName in missingFileNames) {
            require(!(missingFileName == "main.json" || missingFileName == "arm.json")) {
                "Model meta has not found!Missing: $missingFileName"
            }

            when (missingFileName) {
                "main.animation.json" -> animationData["main"] = ResourceConstants.defaultMainAnimationJsonContent!!.toByteArray()

                "arm.animation.json" -> animationData["arm"] = ResourceConstants.defaultArmAnimationJsonContent!!.toByteArray()
                "extra.animation.json" -> animationData["extra"] = ResourceConstants.defaultExtraAnimationJsonContent!!.toByteArray()
            }
        }
        return YsmModelData(FileUtils.fileNameWithoutExtension(modelFile.name),authChecker,metaData,animationData,textureData)
    }

    private fun readFromMyYsmFile(fileData: DataInputStream): Map<String,ByteArray>{
        val fileCount = fileData.readInt()
        val result: MutableMap<String,ByteArray> = HashMap()

        for (i in 0 until fileCount){
            val singleResult = readSingleFile(fileData)
            result[singleResult.first] = singleResult.second
        }

        return result
    }

    private fun readSingleFile(dataBuf : DataInputStream): Pair<String, ByteArray> {
        val encryptedFileNameData = ByteArray(dataBuf.readInt())
        dataBuf.read(encryptedFileNameData)

        val encryptedFileData = ByteArray(dataBuf.readInt())
        dataBuf.read(encryptedFileData)

        val signatureDataEncrypted = ByteArray(dataBuf.readInt())
        dataBuf.read(signatureDataEncrypted)

        val aesKeyDataEncrypted = ByteArray(dataBuf.readInt())
        dataBuf.read(aesKeyDataEncrypted)

        val aesIvKeyDataEncrypted = ByteArray(dataBuf.readInt())
        dataBuf.read(aesIvKeyDataEncrypted)

        val publicKeyData = ByteArray(dataBuf.readInt())
        dataBuf.read(publicKeyData)
        val publicKey = RSAEncryptUtils.getPublicKeyFromBytes(publicKeyData)

        val decryptedKeyData = RSAEncryptUtils.rsaDecrypt(aesKeyDataEncrypted,publicKey)
        val decryptedIvKeyData = RSAEncryptUtils.rsaDecrypt(aesIvKeyDataEncrypted,publicKey)
        val aesSecretKey = SecretKeySpec(decryptedKeyData,"AES")
        val aesIvKey = IvParameterSpec(decryptedIvKeyData)

        val decryptedContentData = AESEncryptUtils.decrypt(aesSecretKey,aesIvKey,encryptedFileData).toByteArray()
        val decryptedFileName = AESEncryptUtils.decrypt(aesSecretKey,aesIvKey,encryptedFileNameData).toByteArray()
        val decryptedSignatureData = AESEncryptUtils.decrypt(aesSecretKey,aesIvKey,signatureDataEncrypted).toByteArray()

        val fileName = String(Base64.getDecoder().decode(decryptedFileName))

        val md = MessageDigest.getInstance("MD5")
        val contentByteArrayInputStream = ByteArrayInputStream(decryptedContentData)
        val mdBuffer = ByteArray(2048)
        var dataLen: Int
        while (true){
            dataLen = contentByteArrayInputStream.read(mdBuffer)

            if (dataLen <= 0){
                break
            }

            md.update(mdBuffer,0,dataLen)
        }
        val digested = md.digest()
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initVerify(publicKey)
        signature.update(digested)

        if (!signature.verify(decryptedSignatureData)){
            return Pair(fileName, ByteArray(0))
        }

        return Pair(fileName,decryptedContentData)
    }
}