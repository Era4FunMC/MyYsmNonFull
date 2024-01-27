package me.earthme.mysm.model.loaders.impl

import com.google.common.collect.Maps
import me.earthme.mysm.ResourceConstants
import me.earthme.mysm.data.mod.management.EnumModelFileType
import me.earthme.mysm.model.IModelLoader
import me.earthme.mysm.model.YsmModelData
import me.earthme.mysm.utils.FileUtils
import java.io.File
import java.util.function.Function

class ZipFileYsmLoaderImpl: IModelLoader {
    override fun canLoad(modelFile: File): Boolean {
        return !modelFile.isDirectory && modelFile.name.endsWith(".zip")
    }

    override fun getFileType(): EnumModelFileType {
        return EnumModelFileType.ZIP_FILE
    }

    override fun loadModel(modelFile: File, authChecker: Function<String, Boolean>): YsmModelData {
        val zipData = FileUtils.readZipFile(modelFile)
        val metaData: MutableMap<String, ByteArray> = Maps.newHashMap()
        val animationData: MutableMap<String, ByteArray> = Maps.newHashMap()
        val textureData: MutableMap<String, ByteArray> = Maps.newHashMap()

        val missingFileNames: MutableList<String> = mutableListOf(
            "main.json",
            "arm.json",
            "main.animation.json",
            "arm.animation.json",
            "extra.animation.json"
        )

        for ((fileName,data) in zipData) {
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
}