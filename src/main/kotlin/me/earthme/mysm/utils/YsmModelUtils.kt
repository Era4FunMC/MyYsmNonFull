package me.earthme.mysm.utils

import com.alibaba.fastjson.JSONObject
import me.earthme.mysm.data.YsmModelFileInstance
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

    fun getAnimationListFromModel(modelInstance: YsmModelFileInstance): List<String>{
        val animationDataMap = modelInstance.animationData!!
        val ret: MutableList<String> = ArrayList()

        for (singleContentData in animationDataMap.values){
            ret.addAll(getAnimationListFromAnimationJson(String(singleContentData)))
        }

        return ret
    }

    fun getAnimationListFromModel(modelInstance: YsmModelFileInstance,modelAnimationMetaFileType: ModelAnimationMetaFileType): List<String>{
        val animationDataMap = modelInstance.animationData!!
        val ret: MutableList<String> = ArrayList()
        val contentData = animationDataMap[modelAnimationMetaFileType.getFileName()]

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