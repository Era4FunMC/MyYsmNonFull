package me.earthme.mysm.data

import com.google.common.collect.Maps
import me.earthme.mysm.ResourceConstants
import org.jetbrains.annotations.Contract
import java.io.File
import java.nio.file.Files
import java.util.function.Function

class YsmModelData (
    private val modelName: String,
    private val authChecker: Function<String,Boolean>,
    private val metaData: Map<String, ByteArray>,
    private val animationData: Map<String, ByteArray>,
    private val textureData: Map<String, ByteArray>
){

    companion object{
        @Contract("_, _, _ -> new")
        fun createFromFolder(
            filesInFolder: List<File>,
            modelName: String,
            needAuthChecker: Function<String,Boolean>,
        ) : YsmModelData{
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
                val fileName = singleUnknownFile.name
                val read = Files.readAllBytes(singleUnknownFile.toPath())
                missingFileNames.remove(fileName)
                if (!fileName.endsWith(".png")) {
                    when (fileName) {
                        "main.json" -> metaData["main"] = read
                        "arm.json" -> metaData["arm"] = read
                        "main.animation.json" -> animationData["main"] = read
                        "arm.animation.json" -> animationData["arm"] = read
                        "extra.animation.json" -> animationData["extra"] = read
                    }
                } else {
                    textureData[fileName] = read
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
            return YsmModelData(modelName,needAuthChecker,metaData,animationData,textureData)
        }
    }

    fun getAllFiles(): Map<String,ByteArray>{
        val ret: MutableMap<String,ByteArray> = HashMap()

        ret["main.json"] = this.metaData["main"]!!
        ret["arm.json"] = this.metaData["arm"]!!
        ret["main.animation.json"] = this.animationData["main"]!!
        ret["arm.animation.json"] = this.animationData["arm"]!!
        ret["extra.animation.json"] = this.animationData["extra"]!!

        ret.putAll(this.textureData)

        return ret
    }

    fun getModelName(): String{
        return this.modelName
    }

    fun getAuthChecker(): Function<String,Boolean>{
        return this.authChecker
    }
}