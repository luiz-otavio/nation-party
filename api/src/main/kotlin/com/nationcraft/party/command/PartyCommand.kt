package com.nationcraft.party.command

import me.saiintbrisson.minecraft.command.annotation.Command
import me.saiintbrisson.minecraft.command.command.Context
import org.bukkit.command.CommandSender

class PartyCommand {

    @Command(
        name = "party",
        aliases = ["p"],
        description = "Party command"
    )
    fun withoutArguments(context: Context<CommandSender>) {
        context.sendMessage("&cYou need to specify an argument")
    }

}