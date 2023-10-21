package me.earthme.mysm.model

import java.util.function.Function

class YsmModelData (
    private val modelName: String,
    private val authChecker: Function<String,Boolean>,
    private val metaData: Map<String, ByteArray>,
    private val animationData: Map<String, ByteArray>,
    private val textureData: Map<String, ByteArray>
){
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