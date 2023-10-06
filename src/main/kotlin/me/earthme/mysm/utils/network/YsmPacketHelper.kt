package me.earthme.mysm.utils.network

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import me.earthme.mysm.NMSUtils
import me.earthme.mysm.manager.PlayerDataManager
import me.earthme.mysm.utils.nms.MCPacketCodecUtils
import org.bukkit.NamespacedKey
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.lang.reflect.Method

object YsmPacketHelper {
    const val NETWORK_CHANNEL_NAMESPACE = "yes_steve_model"
    val NETWORK_CHANNELS_INGOING = listOf("yes_steve_model:0","yes_steve_model:5","yes_steve_model:7")
    val NETWORK_CHANNELS_OUTGOING = listOf("yes_steve_model:1","yes_steve_model:2","yes_steve_model:3","yes_steve_model:4")

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
        dataBuf.writeByte(1) //Garbage message
        return dataBuf
    }

    fun sendCustomPayLoad(target: Player, channel: NamespacedKey, data: ByteBuf){
        //TODO Prevent using nms
        NMSUtils.sendPacket(target, NMSUtils.wrapNewCustomPacket(NMSUtils.wrapNewResourceLocation(channel), NMSUtils.wrapNewFriendlyByteBuf(data)))
    }

    fun modelUpdatePacketData(dataBuf: ByteBuf,ownerEntity: Player): ByteBuf{
        val modelData = PlayerDataManager.createOrGetPlayerData(ownerEntity.name)
        MCPacketCodecUtils.writeVarInt(ownerEntity.entityId,dataBuf) //What ? Entity id? Could we set the model of a non player entity?()
        MCPacketCodecUtils.writeUUID(ownerEntity.uniqueId,dataBuf) // UUID of target entity

        val friendlyByteBuf = NMSUtils.wrapNewFriendlyByteBuf(dataBuf) //TODO Prevent using nms
        NMSUtils.writeNbtToFriendlyByteBuf(NMSUtils.createNbtForSync(modelData),friendlyByteBuf) //TODO Prevent using nms

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