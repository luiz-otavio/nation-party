package com.nationcraft.party

import com.nationcraft.party.command.PartyCommand
import com.nationcraft.party.listener.PartyHandler
import com.nationcraft.party.repository.party.PartyRepository
import org.bukkit.Bukkit
import org.bukkit.command.CommandMap
import org.bukkit.plugin.java.JavaPlugin

class PartyPlugin : JavaPlugin() {

    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(PartyHandler(), this)

        val commandMap = server.javaClass.getDeclaredField("commandMap").apply { isAccessible = true }

        val map = commandMap.get(server) as CommandMap

        map.register("party", PartyCommand())
    }

    fun getRepository() = PartyRepository

}