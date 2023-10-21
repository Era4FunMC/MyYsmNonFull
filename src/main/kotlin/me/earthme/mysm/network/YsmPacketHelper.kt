package me.earthme.mysm.network

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPluginMessage
import io.netty.buffer.ByteBuf
import me.earthme.mysm.utils.ysm.YsmNbtUtils
import me.earthme.mysm.manager.PlayerDataManager
import me.earthme.mysm.utils.mc.MCPacketCodecUtils
import org.bukkit.NamespacedKey
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.lang.reflect.Method

object YsmPacketHelper {
    const val NETWORK_CHANNEL_NAMESPACE = "yes_steve_model"
    val NETWORK_CHANNELS_INGOING = listOf("yes_steve_model:0","yes_steve_model:5","yes_steve_model:7","yes_steve_model:network")
    val NETWORK_CHANNELS_OUTGOING = listOf("yes_steve_model:1","yes_steve_model:2","yes_steve_model:3","yes_steve_model:4","yes_steve_model:network")

    fun attachChannelForPlayer(player: Player){
        for (channel in NETWORK_CHANNELS_INGOING){
            try {
                val senderClass: Class<out CommandSender?> = player::class.java
                val addChannel: Method = senderClass.getDeclaredMethod("addChannel", String::class.java)
                addChannel.setAccessible(true)
                addChannel.invoke(player, channel)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        for (channel in NETWORK_CHANNELS_OUTGOING){
            try {
                val senderClass: Class<out CommandSender?> = player::class.java
                val addChannel: Method = senderClass.getDeclaredMethod("addChannel", String::class.java)
                addChannel.setAccessible(true)
                addChannel.invoke(player, channel)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    fun heldModelsData(dataBuf: ByteBuf,models: Set<NamespacedKey>): ByteBuf{
        MCPacketCodecUtils.writeVarInt(models.size,dataBuf)
        for (singleModel in models){
            MCPacketCodecUtils.writeUtf(singleModel.toString(),32767,dataBuf)
        }

        return dataBuf
    }

    fun reloadPacketData(dataBuf: ByteBuf): ByteBuf{
        //dataBuf.writeByte(1) //Garbage message
        return dataBuf
    }

    fun sendCustomPayLoad(target: Player, channel: NamespacedKey, data: ByteBuf){
        val byteArray = ByteArray(data.readableBytes())
        data.readBytes(byteArray)
        PacketEvents.getAPI().playerManager.sendPacket(target,WrapperPlayServerPluginMessage(channel.toString(),byteArray))
    }

    fun modelUpdatePacketData(dataBuf: ByteBuf,ownerEntity: Player): ByteBuf{
        val modelData = PlayerDataManager.createOrGetPlayerData(ownerEntity.name)
        MCPacketCodecUtils.writeVarInt(ownerEntity.entityId,dataBuf) //What ? Entity id? Could we set the model of a non player entity?()
        MCPacketCodecUtils.writeUUID(ownerEntity.uniqueId,dataBuf) // UUID of target entity
        dataBuf.writeBytes(YsmNbtUtils.createNbtForSync(modelData))
        return dataBuf
    }

    fun modelUpdatePacketDataForge(dataBuf: ByteBuf,ownerEntity: Player): ByteBuf{
        val modelData = PlayerDataManager.createOrGetPlayerData(ownerEntity.name)
        MCPacketCodecUtils.writeVarInt(ownerEntity.entityId,dataBuf) //What ? Entity id? Could we set the model of a non player entity?()
        dataBuf.writeBytes(YsmNbtUtils.createNbtForSync(modelData))
        return dataBuf
    }

    fun md5ContainedPacketData(dataBuffer: ByteBuf,containedMd5: String): ByteBuf{
        MCPacketCodecUtils.writeUtf(containedMd5,32767,dataBuffer)
        return dataBuffer
    }

    fun cacheDataPacketData(dataBuf: ByteBuf,data: ByteArray): ByteBuf{
        MCPacketCodecUtils.writeVarInt(data.size,dataBuf)
        dataBuf.writeBytes(data)
        return dataBuf
    }
}