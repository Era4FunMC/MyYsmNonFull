package me.earthme.mysm.utils.ysm

import com.github.retrooper.packetevents.PacketEvents
import io.netty.buffer.ByteBuf
import me.earthme.mysm.data.PlayerModelData
import org.bukkit.Bukkit
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

object YsmNbtUtils {

    fun createNbtForSyncNew(modelData: PlayerModelData,byteBuf: ByteBuf){
        val bos: ByteArrayOutputStream = ByteArrayOutputStream()
        val dos: DataOutputStream = DataOutputStream(bos)
        dos.writeByte(10)

        Bukkit.getPlayer(modelData.username)?.let{
            val protoVersion = PacketEvents.getAPI().playerManager.getClientVersion(it).protocolVersion
            if (protoVersion < 764){
                dos.writeUTF("")
            }
        }

        dos.writeByte(8)
        dos.writeUTF("model_id")
        dos.writeUTF(modelData.mainResourceLocation.toString())

        dos.writeByte(8)
        dos.writeUTF("select_texture")
        dos.writeUTF(modelData.mainTextPngResourceLocation.toString())

        dos.writeByte(8)
        dos.writeUTF("animation")
        dos.writeUTF(modelData.currentAnimation)

        dos.writeByte(1)
        dos.writeUTF("play_animation")
        dos.writeByte(if (modelData.doAnimation){ 1 } else {0})

        dos.writeByte(0)

        byteBuf.writeBytes(bos.toByteArray())
    }

}