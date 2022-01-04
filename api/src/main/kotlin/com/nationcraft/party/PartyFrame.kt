package com.nationcraft.party

import com.nationcraft.party.command.PartyCommand
import com.nationcraft.party.listener.PartyHandler
import com.nationcraft.party.repository.party.PartyRepository
import me.saiintbrisson.bukkit.command.BukkitFrame
import me.saiintbrisson.minecraft.ViewFrame
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

class PartyFrame(
    plugin: Plugin
) {

    private val viewFrame: ViewFrame
    private val bukkitFrame: BukkitFrame

    init {
        viewFrame = ViewFrame(plugin)

        bukkitFrame = BukkitFrame(plugin)

        bukkitFrame.registerCommands(
            PartyCommand()
        )

        Bukkit.getPluginManager().registerEvents(PartyHandler(), plugin)
    }

    fun getRepository() = PartyRepository

}