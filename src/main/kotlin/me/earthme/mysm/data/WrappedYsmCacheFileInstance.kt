package me.earthme.mysm.data

import com.google.common.collect.Maps
import com.google.gson.Gson
import me.earthme.mysm.ResourceConstants
import me.earthme.mysm.utils.*
import me.earthme.mysm.utils.ysm.*
import me.earthme.mysm.utils.ysm.MiscUtils
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.SecretKey

class WrappedYsmCacheFileInstance (
    private val modelName: String?,
    private var needAuth: Boolean,
    private val metaData: Map<String, ByteArray>?,
    private val animationData: Map<String, ByteArray>?,
    private val textureData: Map<String, ByteArray>?
){
    companion object{
        private val GSON_CODEC = Gson()

        fun createFromModelData(ysmModelData: YsmModelData,dataClass: Class<*>,dataClassLoader: ClassLoader): WrappedYsmCacheFileInstance{
            val currentLoader = Thread.currentThread().contextClassLoader
            Thread.currentThread().contextClassLoader = dataClassLoader
            try {
                val missingFileNames: MutableList<String> = ArrayList(
                    listOf(
                        "main.json",
                        "arm.json",
                        "main.animation.json",
                        "arm.animation.json",
                        "extra.animation.json"
                    )
                )

                val metaData: MutableMap<String, ByteArray> = Maps.newHashMap()
                val animationData: MutableMap<String, ByteArray> = Maps.newHashMap()
                val textureData: MutableMap<String, ByteArray> = Maps.newHashMap()

                for ((fileName,data) in ysmModelData.getAllFiles()){
                    missingFileNames.remove(fileName)
                    if (!fileName.endsWith(".png")) {
                        when (fileName) {
                            "main.json" -> {
                                val encodedMainJson = GSON_CODEC.fromJson(String(data), dataClass)
                                val encodedMainObject: ByteArray = MiscUtils.objectToByteArray(encodedMainJson)
                                metaData["main"] = encodedMainObject
                            }

                            "arm.json" -> {
                                val encodedArmJson = GSON_CODEC.fromJson(String(data), dataClass)
                                val encodedArmObject: ByteArray = MiscUtils.objectToByteArray(encodedArmJson)
                                metaData["arm"] = encodedArmObject
                            }

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
                return WrappedYsmCacheFileInstance(ysmModelData.getModelName(),ysmModelData.getAuthChecker().apply(ysmModelData.getModelName()), metaData, animationData, textureData)
            }finally {
                Thread.currentThread().contextClassLoader = currentLoader
            }
        }
    }

    fun getModelName(): String{
        return this.modelName!!
    }

    @Throws(IOException::class)
    fun toWritableBytes(
        data: WrappedYsmCacheFileInstance,
        key: SecretKey,
        spec: AlgorithmParameterSpec
    ): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        byteArrayOutputStream.write(YsmCodecUtil.intToByteArray(1498629968))
        byteArrayOutputStream.write(YsmCodecUtil.intToByteArray(1))
        val arrayOfByte1 = getEncrypted(data, key, spec)
        val arrayOfByte2: ByteArray = MD5Utils.degist(arrayOfByte1)!!
        byteArrayOutputStream.write(arrayOfByte2)
        byteArrayOutputStream.write(arrayOfByte1)
        return byteArrayOutputStream.toByteArray()
    }

    private fun getEncrypted(
        data: WrappedYsmCacheFileInstance,
        key: SecretKey,
        spec: AlgorithmParameterSpec
    ): ByteArray {
        return try {
            val outputStream = ByteArrayOutputStream()
            YsmCodecUtil.writeString(outputStream, data.modelName!!)
            YsmCodecUtil.writeBoolean(outputStream, data.needAuth)
            YsmCodecUtil.writeMap(outputStream, data.metaData!!)
            YsmCodecUtil.writeMap(outputStream, data.textureData!!)
            YsmCodecUtil.writeMap(outputStream, data.animationData!!)
            YsmCodecUtil.writeBytes(outputStream, data.metaData)
            YsmCodecUtil.writeBytes(outputStream, data.textureData)
            YsmCodecUtil.writeBytes(outputStream, data.animationData)
            val compressedBytes: ByteArray = CompressUtil.compress(outputStream.toByteArray())!!
            EncryptUtils.encrypt(key, spec, compressedBytes).toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            ByteArray(0)
        }
    }
}