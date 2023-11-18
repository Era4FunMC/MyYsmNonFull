package me.earthme.mysm.network

data class PendingPacketTask(
    val channelName: String,
    val channelData: ByteArray
) {
}