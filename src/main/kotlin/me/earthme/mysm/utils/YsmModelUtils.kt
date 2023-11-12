package me.earthme.mysm.utils

import com.alibaba.fastjson.JSONObject
import me.earthme.mysm.model.YsmModelData
import me.earthme.mysm.utils.model.ModelAnimationMetaFileType
import kotlin.math.sin

/**
 * 关于YSM模型的一些工具类
 */
object YsmModelUtils {

    /**
     * 判断传入的动作名称是不是一个合法的轮盘动作
     * @param input 动作名称
     * @return 是否是一个合法的轮盘动作，如果是则为true，反之为false
     */
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

    /**
     * 从模型数据中获取全部的动作列表
     * @param modelInstance 模型数据
     * @return 动作列表
     */
    fun getAnimationListFromModel(modelInstance: YsmModelData): List<String>{
        val allFiles = modelInstance.getAllFiles()
        val ret: MutableList<String> = ArrayList()

        val allElements = ModelAnimationMetaFileType.values().map { a -> a.getFileName() }

        for ((fileName,singleContentData) in allFiles){
            if (!allElements.contains(fileName)){
                continue
            }

            ret.addAll(getAnimationListFromAnimationJson(String(singleContentData)))
        }

        return ret
    }

    /**
     * 从模型数据获取这个模型的动作列表
     * @param modelInstance 模型数据
     * @param modelAnimationMetaFileType 模型动作数据的类型（目前还没有tac的）
     * @return 动作列表
     */
    fun getAnimationListFromModel(modelInstance: YsmModelData, modelAnimationMetaFileType: ModelAnimationMetaFileType): List<String>{
        val allFiles = modelInstance.getAllFiles()
        val ret: MutableList<String> = ArrayList()
        val contentData = allFiles[modelAnimationMetaFileType.getFileName()]

        if (contentData != null){
            ret.addAll(getAnimationListFromAnimationJson(String(contentData)))
        }

        return ret
    }

    /**
     * 从一串json文本中获取动作列表，一般这个json可以从xxx.animation.json中发现
     * @param jsonStr 要解析的json文本
     * @return 动作列表
     */
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