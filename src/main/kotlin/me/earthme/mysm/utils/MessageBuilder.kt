package me.earthme.mysm.utils

import me.earthme.mysm.I18nManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import java.awt.Color

/**
 * Powered by AFterNode/GeneralNext
 */
class MessageBuilder {
    companion object {
        @JvmStatic val COLOR_GRAY = TextColor.color(Color(150, 150, 150).rgb)
        @JvmStatic val COLOR_GREEN = TextColor.color(Color(50, 255, 50).rgb)
        @JvmStatic val COLOR_RESET = TextColor.color(Color(255, 255, 255).rgb)

        @JvmStatic val PREFIX = Component
            .text("[").color(COLOR_GRAY)
            .append(Component.text("MyYSM").color(COLOR_GREEN))
            .append(Component.text("]").color(COLOR_GREEN))
    }

    private val component = Component.text()

    init{
        component.append(PREFIX)
    }

    /**
     * 创建带有前缀的新行
     */
    fun newLine(): MessageBuilder {
        component.appendNewline().append(PREFIX)
        return this
    }

    /**
     * 添加文字
     */
    fun text(content: String): MessageBuilder {
        component.append(Component.text(content))
        return this
    }

    /**
     * 添加可翻译文字
     */
    fun translatable(key: String): MessageBuilder {
        text(I18nManager.parseTranslatableKey(key))
        return this
    }

    /**
     * 添加可翻译文字
     */
    fun translatable(key: String, args: Array<*>): MessageBuilder {
        text(I18nManager.parseTranslatableKey(key, args))
        return this
    }

    /**
     * 添加客户端翻译文字
     */
    fun mTranslatable(key: String): MessageBuilder {
        component.append(Component.translatable(key))
        return this
    }

    /**
     * 转换为Component
     */
    fun toComponent() = component.asComponent()
}