package me.earthme.mysm.data

import org.bukkit.NamespacedKey

data class PlayerModelData(
    //Temp data
    @Transient
    var doAnimation: Boolean = false,
    @Transient
    var sendAnimation: Boolean = true,
    @Transient
    var currentAnimation: String = "idle",
    @Transient @Volatile
    var isDirty: Boolean = false,

    //Data need save
    var mainResourceLocation: NamespacedKey = NamespacedKey.fromString("yes_steve_model:default")!!,
    var mainTextPngResourceLocation: NamespacedKey = NamespacedKey.fromString("yes_steve_model:default/blue.png")!!,
    val username: String = "NONE"
)