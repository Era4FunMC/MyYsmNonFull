package me.earthme.mysm.data.mod.management

import io.netty.buffer.ByteBuf
import me.earthme.mysm.utils.mc.MCPacketCodecUtils.readUtf
import me.earthme.mysm.utils.mc.MCPacketCodecUtils.readVarInt
import me.earthme.mysm.utils.mc.MCPacketCodecUtils.writeUtf
import me.earthme.mysm.utils.mc.MCPacketCodecUtils.writeVarInt

class YsmModelDesc (
    private val modelName: String,
    private val fileType: EnumModelFileType,
    private val fileSize: Long
){
    companion object{
        fun readFromBuffer(buffer: ByteBuf): YsmModelDesc {
            return YsmModelDesc(
                buffer.readUtf(32767),
                EnumModelFileType.values()[buffer.readVarInt()],
                buffer.readLong()
            )
        }

    }

    fun writeToBuffer(buffer: ByteBuf){
        buffer.writeUtf(this.modelName,32767)
        buffer.writeVarInt(this.fileType.ordinal)
        buffer.writeLong(this.fileSize)
    }
}