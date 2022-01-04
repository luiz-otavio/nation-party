@file:JvmName("Text")

package com.nationcraft.party.util

import org.bukkit.ChatColor

fun translate(message: String): String =
    ChatColor.translateAlternateColorCodes('&', message)

fun translate(vararg message: String) =
    message.map { translate(it) }.toTypedArray()
