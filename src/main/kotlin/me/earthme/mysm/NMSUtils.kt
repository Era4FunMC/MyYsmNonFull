package me.earthme.mysm

import io.netty.buffer.ByteBuf
import me.earthme.mysm.data.PlayerModelData
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.PacketDataSerializer
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.PacketPlayOutCustomPayload
import net.minecraft.resources.MinecraftKey
import org.bukkit.NamespacedKey
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer
import org.bukkit.entity.Player

object NMSUtils {

    fun wrapNewResourceLocation(namespaceKey: NamespacedKey): Any{
        return MinecraftKey(namespaceKey.namespace,namespaceKey.key)
    }

    fun wrapNewFriendlyByteBuf(byteBuf: ByteBuf): PacketDataSerializer{
        return PacketDataSerializer(byteBuf)
    }

    fun wrapNewCustomPacket(channel: Any,data: Any): Any{
        return PacketPlayOutCustomPayload(channel as MinecraftKey, data as PacketDataSerializer)
    }

    fun sendPacket(target: Player,packet: Any){
        val nmsPlayer = target as CraftPlayer
        nmsPlayer.handle.c.a(packet as Packet<*>)
    }

    fun createNbtForSync(modelData: PlayerModelData): NBTTagCompound{
        val ret = NBTTagCompound()
        ret.a("model_id",modelData.mainResourceLocation.toString())
        ret.a("select_texture",modelData.mainTextPngResourceLocation.toString())
        ret.a("animation",modelData.currentAnimation)
        ret.a("play_animation",modelData.doAnimation)
        return ret
    }

    fun writeNbtToFriendlyByteBuf(nbt: NBTTagCompound, buffer: PacketDataSerializer){
        buffer.a(nbt)
    }
}