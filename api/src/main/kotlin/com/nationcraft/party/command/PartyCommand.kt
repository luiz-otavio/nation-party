package com.nationcraft.party.command

import com.nationcraft.party.event.*
import com.nationcraft.party.pojo.Party
import com.nationcraft.party.pojo.invite.Invite
import com.nationcraft.party.pojo.member.PartyMember
import com.nationcraft.party.repository.invite.InviteRepository
import com.nationcraft.party.repository.party.PartyRepository
import com.nationcraft.party.util.callTo
import com.nationcraft.party.util.translate
import me.saiintbrisson.minecraft.command.annotation.Command
import me.saiintbrisson.minecraft.command.annotation.Optional
import me.saiintbrisson.minecraft.command.command.Context
import me.saiintbrisson.minecraft.command.target.CommandTarget
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.math.floor

class PartyCommand {

    @Command(
        name = "party",
        aliases = ["p"],
        description = "Party command"
    )
    fun withoutArguments(context: Context<CommandSender>) {
        context.sendMessage(
            translate(
                " ",
                " &eMade by Wizard & Castruu - Available for 1.12.2 & 1.7.10",
                " &eFor any issues, make your comment in &fhttps://github.com/luiz-otavio/nation-party",
                " ",
                " &e/party create <name> - Create a party",
                " &e/party invite <player> - Invite a player to your party",
                " &e/party accept <name> - Accept a party invitation",
                " &e/party promote <player> - Promote a player to party leader",
                " &e/party leave - Leave your party",
                " &e/party kick <player> - Kick a player from your party",
                " &e/party disband - Disband your party",
                " &e/party list - List all parties",
                " &e/party info - Show party info",
                " &e/party chat - Enable party chat",
                " "
            )
        )
    }

    @Command(
        name = "party.create",
        aliases = ["criar"],
        description = "Create a party",
        permission = "party.create",
        usage = "<name>",
        target = CommandTarget.PLAYER
    )
    fun create(context: Context<Player>, name: String) {
        val player = context.sender

        if (PartyRepository.getParty(player) != null) {
            player.sendMessage(
                translate("&cThis party already exists!")
            )

            return
        }

        if (PartyRepository.getParty(name) != null) {
            player.sendMessage(
                translate("&cThis party already exists!")
            )

            return
        }

        val party = Party(name)

        val member = PartyMember(
            player.uniqueId,
            true
        )

        party.addPlayer(member)

        val event = callTo(
            PartyCreateEvent(player, party)
        )

        if (event.isCancelled) {
            return
        }

        PartyRepository.addParty(party)

        player.sendMessage(
            translate(
                "&aParty created!"
            )
        )
    }

    @Command(
        name = "party.invite",
        aliases = ["convidar"],
        description = "Invite a player to your party",
        permission = "party.invite",
        usage = "<player>",
        target = CommandTarget.PLAYER
    )
    fun invite(context: Context<Player>, target: Player?) {
        val player = context.sender

        if (target == null) {
            player.sendMessage(
                translate("&cThis player aren't online right now.")
            )

            return
        }

        val party = PartyRepository.getParty(player)

        if (party == null) {
            player.sendMessage(
                translate("&cYou are not in a party!")
            )

            return
        }

        val leader = party.getMember(player) ?: return

        if (!leader.isLeader) {
            player.sendMessage(
                translate("&cYou are not the leader of your party!")
            )

            return
        }

        if (PartyRepository.getParty(target) != null) {
            player.sendMessage(
                translate("&cThis player is already in a party!")
            )

            return
        }

        if (InviteRepository.hasInvite(party, target.uniqueId)) {
            player.sendMessage(
                translate("&cThis player already has an invite!")
            )

            return
        }

        val event = callTo(
            PartyInviteEvent(player, party)
        )

        if (event.isCancelled) {
            return
        }

        InviteRepository.addInvite(
            Invite(target.uniqueId, player.uniqueId, party)
        )

        target.sendMessage(
            translate(
                "&eYou have been invited to your party by &f${player.name}&e!"
            )
        )
    }

    @Command(
        name = "party.accept",
        aliases = ["aceitar"],
        description = "Accept a party invitation",
        permission = "party.accept",
        usage = "<name>",
        target = CommandTarget.PLAYER
    )
    fun accept(context: Context<Player>, name: String) {
        val player = context.sender

        if (PartyRepository.getParty(player) != null) {
            player.sendMessage(
                translate("&eYou are already in a party!")
            )
        } else {
            val invite = InviteRepository.getInvite(player.uniqueId, name)

            if (invite == null) {
                player.sendMessage(
                    translate("&eThere is no invite from this party name!")
                )

                return
            }

            invite.accept()
        }
    }

    @Command(
        name = "party.leave",
        aliases = ["sair"],
        description = "Leave your party",
        permission = "party.leave",
        target = CommandTarget.PLAYER
    )
    fun leave(context: Context<Player>) {
        val player = context.sender

        val party = PartyRepository.getParty(player)

        if (party == null) {
            player.sendMessage(
                translate("&cYou are not in a party!")
            )

            return
        }

        val member = party.getMember(player) ?: return

        if (member.isLeader) {
            player.sendMessage(
                translate("&cYou are the leader of your party! You must disband the party before leaving it!")
            )

            return
        }

        val event = callTo(
            PartyLeaveEvent(player, party)
        )

        if (event.isCancelled) {
            return
        }

        party.removePlayer(member)

        player.sendMessage(
            translate("&aYou left the party!")
        )

        for (target in party.members) {
            val user = target.getPlayer() ?: continue

            user.sendMessage(
                translate("&e${player.name} &ahas left the party!")
            )
        }
    }

    @Command(
        name = "party.kick",
        aliases = ["kickar"],
        description = "Kick a player from your party",
        permission = "party.kick",
        usage = "<player>",
        target = CommandTarget.PLAYER
    )
    fun kick(context: Context<Player>, player: Player?) {
        val target = context.sender

        if (player == null) {
            target.sendMessage(
                translate("&cYou must specify a player to kick!")
            )

            return
        }

        val party = PartyRepository.getParty(player)

        if (party == null) {
            target.sendMessage(
                translate("&You aren't in a party!")
            )

            return
        }

        if (player == target) {
            target.sendMessage(
                translate("&cYou can't kick yourself!")
            )

            return
        }

        val member = party.getMember(target) ?: return

        if (!member.isLeader) {
            target.sendMessage(
                translate("&cYou can't kick a player from your party!")
            )

            return
        }

        val user = party.getMember(player)

        if (user == null) {
            target.sendMessage(
                translate("&cThis player isn't in your party!")
            )

            return
        }

        val event = callTo(
            PartyKickEvent(player, party)
        )

        if (event.isCancelled) {
            return
        }

        party.removePlayer(user)

        target.sendMessage(
            translate("&eYou kicked a player from your party!")
        )
    }

    @Command(
        name = "party.disband",
        aliases = ["disband"],
        description = "Disband your party",
        permission = "party.disband",
        target = CommandTarget.PLAYER
    )
    fun disband(context: Context<Player>) {
        val player = context.sender

        val party = PartyRepository.getParty(player)

        if (party == null) {
            player.sendMessage(
                translate("&eYou can't disband a party that doesn't exist!")
            )
        } else {
            val member = party.getMember(player) ?: return

            if (!member.isLeader) {
                player.sendMessage(
                    translate("&eYou can't disband a party that you aren't the leader!")
                )
            } else {
                val event = callTo(
                    PartyDestroyEvent(
                        party
                    )
                )

                if (event.isCancelled)
                    return
            }

            for (target in party.members) {
                val user = target.getPlayer() ?: continue

                user.sendMessage(
                    translate("&eYour party has been disbanded!")
                )
            }

            PartyRepository.removeParty(party)

            player.sendMessage(
                translate("&eYou disbanded your party!")
            )
        }


        @Command(
            name = "party.list",
            aliases = ["listar"],
            description = "List all parties",
            permission = "party.list",
            target = CommandTarget.PLAYER
        )
        fun list(context: Context<Player>, @Optional(def = ["0"]) page: Int) {
            val player = context.sender

            if (page < 0) {
                player.sendMessage(
                    translate("&ePage number must be greater than 0!")
                )

                return
            }

            val parties = PartyRepository.getParties()

            val maxPage = floor(parties.size / 10.0).toInt()

            if (page > maxPage) {
                player.sendMessage(
                    translate("&ePage number must be less than $maxPage!")
                )

                return
            }

            val start = page * 10
            val end = start + 10

            val partiesToShow = parties.subList(start, end)

            if (partiesToShow.isEmpty()) {
                player.sendMessage(
                    translate("&eThere are no parties to show!")
                )

                return
            }

            val transformer: (Party) -> CharSequence = { "&6${it.id} &e- &6${it.members.size}" }

            player.sendMessage(
                translate(
                    " ",
                    " &eParties:",
                    " &e${partiesToShow.joinToString("\n ", transform = transformer)}",
                    " ",
                    "&eParties: &7(Page $page/$maxPage)"
                )
            )
        }

        @Command(
            name = "party.info",
            aliases = ["info"],
            description = "Show party info",
            permission = "party.info",
            target = CommandTarget.PLAYER
        )
        fun info(context: Context<Player>, @Optional(def = ["0"]) page: Int) {
            val player = context.sender

            if (page < 0) {
                player.sendMessage(
                    translate("&ePage number must be greater than 0!")
                )

                return
            }

            val party = PartyRepository.getParty(player)

            if (party == null) {
                player.sendMessage(
                    translate("&eYou are not in a party!")
                )

                return
            }

            val players = party.members

            val maxPage = floor(players.size / 5.0).toInt()

            if (page > maxPage) {
                player.sendMessage(
                    translate("&ePage number must be less than $maxPage!")
                )

                return
            }

            val start = page * 5
            val end = start + 5

            val playersToShow = players.subList(start, end)

            val transformer: (PartyMember) -> CharSequence =
                { if (it.isOnline()) "&a${it.getPlayer()?.name}" else "&cUnknown" }

            player.sendMessage(
                translate(
                    " ",
                    " &eParty: &6${party.id}",
                    "  &eLeader: &6${party.getLeader()?.getPlayer()?.name}",
                    " ",
                    "&ePlayers: &7(Page: $page/$maxPage)",
                    " &e${playersToShow.joinToString("\n &e- ", transform = transformer)}"
                )
            )
        }

        @Command(
            name = "party.chat",
            aliases = ["chat"],
            description = "Enable party chat",
            permission = "party.chat",
            target = CommandTarget.PLAYER
        )
        fun chat(context: Context<Player>) {
            val player = context.sender

            val party = PartyRepository.getParty(player)

            if (party == null) {
                player.sendMessage(
                    translate("&eYou can't enable party chat if you aren't in a party!")
                )
            } else {
                val member = party.getMember(player)

                if (member == null) {
                    player.sendMessage(
                        translate("&eYou can't enable party chat if you aren't in a party!")
                    )
                } else {
                    val event = callTo(
                        PartyUpdateChattingEvent(party, player, !member.isChatting)
                    )

                    if (event.isCancelled) {
                        return
                    }

                    member.isChatting = event.isChatting

                    player.sendMessage(
                        translate("&eYou ${if (event.isChatting) "enabled" else "disabled"} party chat!")
                    )
                }
            }
        }

        @Command(
            name = "party.promote",
            aliases = ["promover"],
            description = "Promote a party member",
            permission = "party.promote",
            target = CommandTarget.PLAYER
        )
        fun promote(context: Context<Player>, target: Player?) {
            val player = context.sender

            if (target == null) {
                player.sendMessage(
                    translate("&eYou must specify a player!")
                )

                return
            }

            val party = PartyRepository.getParty(player)

            if (party == null) {
                player.sendMessage(
                    translate("&eYou can't promote a player if you aren't in a party!")
                )
            } else {
                val member = party.getMember(player)

                if (member == null) {
                    player.sendMessage(
                        translate("&eYou can't promote a player if you aren't in a party!")
                    )
                } else {
                    if (!member.isLeader) {
                        player.sendMessage(
                            translate("&eYou can't promote a player if you aren't the party leader!")
                        )
                    } else {
                        val targetMember = party.getMember(target)

                        if (targetMember == null) {
                            player.sendMessage(
                                translate("&e${target.name} is not in your party!")
                            )
                        } else {
                            val event = callTo(
                                PartyPromoteEvent(target, player, party)
                            )

                            if (event.isCancelled) {
                                return
                            }

                            targetMember.isLeader = true
                            member.isLeader = false

                            player.sendMessage(
                                translate("&eYou promoted ${target.name} to party leader!")
                            )

                            target.sendMessage(
                                translate("&eYou were promoted to party leader by ${player.name}!")
                            )

                            for (each in party.members) {
                                val user = each.getPlayer() ?: continue

                                user.sendMessage(
                                    translate("&e${target.name} was promoted to party leader!")
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}