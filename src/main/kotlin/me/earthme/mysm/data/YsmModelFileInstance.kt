package me.earthme.mysm.data

import com.google.common.collect.Maps
import com.google.gson.Gson
import me.earthme.mysm.ResourceConstants
import me.earthme.mysm.utils.*
import me.earthme.mysm.utils.ysm.*
import me.earthme.mysm.utils.ysm.MiscUtils
import org.jetbrains.annotations.Contract
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.security.spec.AlgorithmParameterSpec
import java.util.function.Function
import javax.crypto.SecretKey

class YsmModelFileInstance (
    private val modelName: String?,
    private var needAuth: Boolean,
    val metaData: Map<String, ByteArray>?,
    val animationData: Map<String, ByteArray>?,
    val textureData: Map<String, ByteArray>?
){
    companion object{
        private val GSON_CODEC = Gson()

        @Contract("_, _, _ -> new")
        fun createFromFolder(
            filesInFolder: List<File>,
            modelName: String,
            needAuthChecker: Function<String,Boolean>,
            dataClass: Class<*>,
            dataClassLoader: ClassLoader
        ) : YsmModelFileInstance{
            val currentLoader: ClassLoader = YsmModelFileInstance::class.java.classLoader
            Thread.currentThread().contextClassLoader = dataClassLoader
            try {
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

                for (singleUnknownFile in filesInFolder) {
                    val fileName = if (singleUnknownFile.isDirectory) null else singleUnknownFile.name
                    if (fileName != null) {
                        val read = Files.readAllBytes(singleUnknownFile.toPath())
                        missingFileNames.remove(fileName)
                        if (!fileName.endsWith(".png")) {
                            when (fileName) {
                                "main.json" -> {
                                    val encodedMainJson = GSON_CODEC.fromJson(String(read), dataClass)
                                    val encodedMainObject: ByteArray = MiscUtils.objectToByteArray(encodedMainJson)
                                    metaData["main"] = encodedMainObject
                                }

                                "arm.json" -> {
                                    val encodedArmJson = GSON_CODEC.fromJson(String(read), dataClass)
                                    val encodedArmObject: ByteArray = MiscUtils.objectToByteArray(encodedArmJson)
                                    metaData["arm"] = encodedArmObject
                                }

                                "main.animation.json" -> animationData["main"] = read
                                "arm.animation.json" -> animationData["arm"] = read
                                "extra.animation.json" -> animationData["extra"] = read
                            }
                        } else {
                            textureData[fileName] = read
                        }
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
                return YsmModelFileInstance(modelName, needAuthChecker.apply(modelName), metaData, animationData, textureData)
            } finally {
                Thread.currentThread().contextClassLoader = currentLoader
            }
        }
    }

    //Java -> Kotlin translate error
    /*private var modelName: String? = null
    private var needAuth = false
    private var metaData: Map<String, ByteArray>? = null
    private var textureData: Map<String, ByteArray>? = null
    private var animationData: Map<String, ByteArray>? = null*/

    fun setNeedAuth(needAuth: Boolean){
        this.needAuth = needAuth
    }

    fun isNeedAuth(): Boolean{
        return this.needAuth
    }

    fun getModelName(): String{
        return this.modelName!!
    }

    @Throws(IOException::class)
    fun toWritableBytes(
        data: YsmModelFileInstance,
        key: SecretKey,
        spec: AlgorithmParameterSpec
    ): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        byteArrayOutputStream.write(YsmCodecUtil.intToByteArray(1498629968))
        byteArrayOutputStream.write(YsmCodecUtil.intToByteArray(1))
        val arrayOfByte1 = getProcessed(data, key, spec)
        val arrayOfByte2: ByteArray = MD5Utils.degist(arrayOfByte1)!!
        byteArrayOutputStream.write(arrayOfByte2)
        byteArrayOutputStream.write(arrayOfByte1)
        return byteArrayOutputStream.toByteArray()
    }

    private fun getProcessed(
        data: YsmModelFileInstance,
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