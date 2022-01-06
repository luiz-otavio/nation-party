package com.nationcraft.party

import com.nationcraft.party.command.PartyCommand
import com.nationcraft.party.listener.PartyHandler
import com.nationcraft.party.repository.party.PartyRepository
import me.saiintbrisson.bukkit.command.BukkitFrame
import me.saiintbrisson.minecraft.ViewFrame
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class PartyPlugin : JavaPlugin() {

    companion object {
        fun getInstance() = getPlugin(PartyPlugin::class.java)
    }

    private lateinit var viewFrame: ViewFrame
    private lateinit var bukkitFrame: BukkitFrame

    override fun onEnable() {
        viewFrame = ViewFrame(this)
        bukkitFrame = BukkitFrame(this)

        bukkitFrame.registerCommands(
            PartyCommand()
        )

        Bukkit.getPluginManager().registerEvents(PartyHandler(), this)
    }

    fun getRepository() = PartyRepository

}