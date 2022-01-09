package com.nationcraft.party.listener

import com.nationcraft.party.event.PartyChatEvent
import com.nationcraft.party.repository.party.PartyRepository
import com.nationcraft.party.util.callTo
import com.nationcraft.party.util.translate
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PartyHandler : Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onAsyncPlayerChat(event: AsyncPlayerChatEvent) {
        val player = event.player

        val party = PartyRepository.getParty(player) ?: return

        val member = party.getMember(player) ?: return

        if (!member.isChatting) {
            return
        }

        val call = callTo(
            PartyChatEvent(player, party, event.message)
        )

        if (!call.isCancelled) {
            event.isCancelled = true

            for (each in party.members) {
                val user = each.getPlayer() ?: continue

                user.sendMessage(
                    translate("&e&l[P] &e${player.name}&7: &7${event.message}")
                )

                user.playSound(
                    user.location,
                    Sound.CHICKEN_EGG_POP,
                    1F,
                    1F
                )
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player

        val party = PartyRepository.getParty(player) ?: return

        for (member in party.members) {
            val target = member.getPlayer() ?: continue

            if (target == player) {
                continue
            }

            target.sendMessage(
                translate("&e&l[NP] &e${player.name}&7 saiu do servidor.")
            )
        }

        val user = party.getMember(player) ?: return

        if (user.isLeader) {
            val anyone = party.members.firstOrNull { it != user && it.isOnline() }

            if (anyone == null) {
                party.removePlayer(user)

                PartyRepository.removeParty(party)

                return
            }

            val targetPlayer = anyone.getPlayer() ?: return

            user.isLeader = false
            anyone.isLeader = true

            val messages = arrayOf(
                translate("&e&l[NP] &e${player.name}&7 não é mais o lider do grupo."),
                translate("&e&l[NP] &e${targetPlayer.name}&7 agora é o líder do grupo.")
            )

            for (member in party.members) {
                val target = member.getPlayer() ?: continue

                target.sendMessage(messages)
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        val party = PartyRepository.getParty(player) ?: return

        for (member in party.members) {
            val target = member.getPlayer() ?: continue

            if (target == player) {
                continue
            }

            target.sendMessage(
                translate("&e&l[NP] &e${player.name}&7 entrou no servidor.")
            )
        }
    }

}