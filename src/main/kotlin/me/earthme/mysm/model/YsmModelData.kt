package me.earthme.mysm.model

import me.earthme.mysm.data.mod.management.EnumModelFileType
import me.earthme.mysm.data.mod.management.YsmModelDesc
import java.util.function.Function

/**
 * 用于存储模型数据的类
 */
class YsmModelData (
    private val modelName: String,
    private val authChecker: Function<String,Boolean>,
    private val metaData: Map<String, ByteArray>,
    private val animationData: Map<String, ByteArray>,
    private val textureData: Map<String, ByteArray>
){
    private lateinit var modelData: YsmModelDesc

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

    fun getModelDesc(): YsmModelDesc {
        return this.modelData
    }

    fun refreshDesc(fileType: EnumModelFileType){
        this.modelData = YsmModelDesc(
            this.modelName,
            fileType,
            this.computeDataSize()
        )
    }

    private fun computeDataSize(): Long{
        var sumCounter: Long = 0

        for (metaData  in this.metaData){
            sumCounter += metaData.value.size
        }

        for (metaData  in this.animationData){
            sumCounter += metaData.value.size
        }

        for (metaData  in this.textureData){
            sumCounter += metaData.value.size
        }

        return sumCounter
    }

    fun getModelName(): String{
        return this.modelName
    }

    fun getAuthChecker(): Function<String,Boolean>{
        return this.authChecker
    }
}