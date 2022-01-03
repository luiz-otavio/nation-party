package com.nationcraft.party

import me.saiintbrisson.minecraft.ViewFrame
import org.bukkit.plugin.Plugin

class PartyFrame(
    private val plugin: Plugin
) {

    private val viewFrame: ViewFrame

    init {
        viewFrame = ViewFrame(plugin)

        viewFrame.register(

        )

    }

}