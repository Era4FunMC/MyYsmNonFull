package me.earthme.mysm.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import java.io.File
import java.nio.file.Files
import java.util.concurrent.ConcurrentHashMap

data class ModelPermissionData (
    val adminList: MutableSet<String>,
    val modelsNeedAuth: MutableSet<String>,
    val playerHeldModelInfo: MutableSet<PlayerHeldModelData>,
    @Volatile @Transient
    var isDirty: Boolean = false
){
    companion object{
        private val gson: Gson = GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create()

        fun fromFile(file: File): ModelPermissionData{
            val readContent = Files.readString(file.toPath())
            return gson.fromJson(readContent,ModelPermissionData::class.java)
        }
    }

    fun setModelNeedAuth(modelLocation: NamespacedKey,needAuth: Boolean){
        synchronized(this){
            this.isDirty = true
            if (needAuth){
                this.modelsNeedAuth.add(modelLocation.key)
            }else{
                this.modelsNeedAuth.remove(modelLocation.key)
            }
        }
    }

    fun removePlayerHeldModel(modelLocation: NamespacedKey,player: Player){
        synchronized(this){
            this.isDirty = true
            val modelName = modelLocation.key

            for (singleInfo in this.playerHeldModelInfo){
                if (singleInfo.playerName == player.name){
                    singleInfo.heldModels.remove(modelName)
                }
                break
            }
        }
    }

    fun addPlayerHeldModel(modelLocation: NamespacedKey,player: Player){
        synchronized(this){
            this.isDirty = true
            val modelName = modelLocation.key

            for (singleInfo in this.playerHeldModelInfo){
                if (singleInfo.playerName == player.name){
                    if (singleInfo.heldModels.contains(modelName)){
                        return
                    }
                    singleInfo.heldModels.add(modelName)
                    break
                }
            }

            val newInfo = PlayerHeldModelData(player.name,ConcurrentHashMap.newKeySet())
            newInfo.heldModels.add(modelName)
            this.playerHeldModelInfo.add(newInfo)
        }
    }

    fun doesPlayerHeldModel(modelLocation: NamespacedKey,player: Player): Boolean{
        synchronized(this){
            val modelName = modelLocation.key
            for (singleInfo in this.playerHeldModelInfo){
                if (singleInfo.playerName == player.name){
                    return singleInfo.heldModels.contains(modelName)
                }
            }

            return false
        }
    }

    fun writeToFile(targetFile: File){
        var encoded: String

        synchronized(this){
            encoded = gson.toJson(this)
        }

        Files.writeString(targetFile.toPath(),encoded)
    }

    fun isModelNeedAuth(modelLocation: NamespacedKey): Boolean{
        synchronized(this){
            return this.modelsNeedAuth.contains(modelLocation.key)
        }
    }

    fun getAllHeldDataOfPlayer(player: Player): Set<NamespacedKey>{
        synchronized(this){
            val ret: MutableSet<NamespacedKey> = HashSet()
            for (data in playerHeldModelInfo){
                if (data.playerName == player.name){
                    for (modelName in data.heldModels){
                        ret.add(NamespacedKey("yes_steve_model",modelName))
                    }
                    break
                }
            }

            return ret
        }
    }
}