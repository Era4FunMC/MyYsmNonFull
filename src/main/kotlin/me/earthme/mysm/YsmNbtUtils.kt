package me.earthme.mysm

import com.github.retrooper.packetevents.protocol.nbt.NBTByte
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound
import com.github.retrooper.packetevents.protocol.nbt.NBTString
import io.github.retrooper.packetevents.util.SpigotReflectionUtil
import me.earthme.mysm.data.PlayerModelData
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

object YsmNbtUtils {
    fun createNbtForSync(modelData: PlayerModelData): ByteArray{
        val outStreamParentStream = ByteArrayOutputStream()
        val outStream = DataOutputStream(outStreamParentStream)
        val nbtTag = NBTCompound()

        nbtTag.setTag("model_id",NBTString(modelData.mainResourceLocation.toString()))
        nbtTag.setTag("select_texture",NBTString(modelData.mainTextPngResourceLocation.toString()))
        nbtTag.setTag("animation",NBTString(modelData.currentAnimation))
        nbtTag.setTag("play_animation",NBTByte(modelData.doAnimation))

        SpigotReflectionUtil.writeNmsNbtToStream(SpigotReflectionUtil.toMinecraftNBT(nbtTag),outStream)

        return outStreamParentStream.toByteArray()
    }
}