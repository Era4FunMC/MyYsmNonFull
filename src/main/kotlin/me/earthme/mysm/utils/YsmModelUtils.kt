package me.earthme.mysm.utils

import com.alibaba.fastjson.JSONObject
import me.earthme.mysm.data.WrappedYsmCacheFileInstance
import me.earthme.mysm.data.YsmModelData
import me.earthme.mysm.utils.model.ModelAnimationMetaFileType

object YsmModelUtils {
    fun hasAnyExtraAnimation(input: String): Boolean{
        if (input.length != 6){
            return false
        }

        if (input.subSequence(0,4) != "extra"){
            return false
        }

        val extraAnimationIdString = input.subSequence(5,5)

        try {
            val animationId = Integer.parseInt(extraAnimationIdString as String?)
            if (-1 <= animationId && animationId < 8){
                return true
            }
        }catch (_: Exception){}

        return false
    }

    fun getAnimationListFromModel(modelInstance: YsmModelData): List<String>{
        val allFiles = modelInstance.getAllFiles()
        val ret: MutableList<String> = ArrayList()

        for (singleContentData in allFiles.values){
            ret.addAll(getAnimationListFromAnimationJson(String(singleContentData)))
        }

        return ret
    }

    fun getAnimationListFromModel(modelInstance: YsmModelData, modelAnimationMetaFileType: ModelAnimationMetaFileType): List<String>{
        val allFiles = modelInstance.getAllFiles()
        val ret: MutableList<String> = ArrayList()
        val contentData = allFiles[modelAnimationMetaFileType.getFileName()]

        if (contentData != null){
            ret.addAll(getAnimationListFromAnimationJson(String(contentData)))
        }

        return ret
    }

    fun getAnimationListFromAnimationJson(jsonStr: String): List<String>{
        val jsonObject = JSONObject.parseObject(jsonStr)
        val animationsObject = jsonObject.getJSONObject("animations")

        val ret: MutableList<String> = ArrayList()
        for (singleKey in animationsObject.keys){
            ret.add(singleKey)
        }

        return ret
    }
}