package com.nationcraft.party.command

import com.nationcraft.party.event.*
import com.nationcraft.party.pojo.Party
import com.nationcraft.party.pojo.invite.Invite
import com.nationcraft.party.pojo.member.PartyMember
import com.nationcraft.party.repository.invite.InviteRepository
import com.nationcraft.party.repository.party.PartyRepository
import com.nationcraft.party.util.callTo
import com.nationcraft.party.util.translate
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.math.floor

class PartyCommand : Command(
    "party",
    "Party command",
    "/party",
    listOf("p")
) {

    override fun execute(sender: CommandSender, label: String, args: Array<String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage(
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

            return true
        }

        if (sender !is Player) {
            return false
        }

        if (args.size == 1) {
            if (args[0] == "sair" || args[0] == "leave") {
                val party = PartyRepository.getParty(sender)

                if (party == null) {
                    sender.sendMessage(
                        translate("&cYou are not in a party!")
                    )

                    return false
                }

                val member = party.getMember(sender) ?: return false

                if (member.isLeader) {
                    sender.sendMessage(
                        translate("&cYou are the leader of your party! You must disband the party before leaving it!")
                    )

                    return false
                }

                val event = callTo(
                    PartyLeaveEvent(sender, party)
                )

                if (event.isCancelled) {
                    return false
                }

                party.removePlayer(member)

                sender.sendMessage(
                    translate("&aYou left the party!")
                )

                for (target in party.members) {
                    val user = target.getPlayer() ?: continue

                    user.sendMessage(
                        translate("&e${sender.name} &ahas left the party!")
                    )
                }
            }

            if (args[0] == "disband" || args[0] == "deletar") {
                val party = PartyRepository.getParty(sender)

                if (party == null) {
                    sender.sendMessage(
                        translate("&eYou can't disband a party that doesn't exist!")
                    )
                } else {
                    val member = party.getMember(sender) ?: return false

                    if (!member.isLeader) {
                        sender.sendMessage(
                            translate("&eYou can't disband a party that you aren't the leader!")
                        )
                    } else {
                        val event = callTo(
                            PartyDestroyEvent(
                                party
                            )
                        )

                        if (event.isCancelled) {
                            return false
                        }
                    }

                    for (target in party.members) {
                        val user = target.getPlayer() ?: continue

                        user.sendMessage(
                            translate("&eYour party has been disbanded!")
                        )
                    }

                    PartyRepository.removeParty(party)

                    sender.sendMessage(
                        translate("&eYou disbanded your party!")
                    )

                    return true
                }
            }

            if (args[0] == "listar" || args[0] == "list") {
                val parties = PartyRepository.getParties()

                val maxPage = floor(parties.size / 10.0).toInt()

                val start = 0 * 10
                val end = start + 10

                val partiesToShow = parties.subList(
                    start,
                    if (end > parties.size) parties.size else end
                )

                if (partiesToShow.isEmpty()) {
                    sender.sendMessage(
                        translate("&eThere are no parties to show!")
                    )

                    return false
                }

                val transformer: (Party) -> CharSequence = { "&6${it.id} &e- &6${it.members.size}" }

                sender.sendMessage(
                    translate(
                        " ",
                        " &eParties:",
                        " &e${partiesToShow.joinToString("\n ", transform = transformer)}",
                        " ",
                        "&eParties: &7(Page 1/$maxPage)"
                    )
                )

                return true
            }

            if (args[0] == "info") {
                val party = PartyRepository.getParty(sender)

                if (party == null) {
                    sender.sendMessage(
                        translate("&eYou are not in a party!")
                    )

                    return false
                }

                val players = party.members

                val maxPage = floor(players.size / 5.0).toInt()

                val start = 0 * 5
                val end = start + 5

                val playersToShow = players.subList(
                    start,
                    if (end > players.size) players.size else end
                )

                val transformer: (PartyMember) -> CharSequence =
                    { if (it.isOnline()) "&a${it.getPlayer()?.name}" else "&cUnknown" }

                sender.sendMessage(
                    translate(
                        " ",
                        " &eParty: &6${party.id}",
                        "  &eLeader: &6${party.getLeader()?.getPlayer()?.name}",
                        " ",
                        "&ePlayers: &7(Page: 1/$maxPage)",
                        " &e${playersToShow.joinToString("\n &e- ", transform = transformer)}"
                    )
                )
            }

            if (args[0] == "chat") {
                val party = PartyRepository.getParty(sender)

                if (party == null) {
                    sender.sendMessage(
                        translate("&eYou can't enable party chat if you aren't in a party!")
                    )
                } else {
                    val member = party.getMember(sender)

                    if (member == null) {
                        sender.sendMessage(
                            translate("&eYou can't enable party chat if you aren't in a party!")
                        )
                    } else {
                        val event = callTo(
                            PartyUpdateChattingEvent(party, sender, !member.isChatting)
                        )

                        if (event.isCancelled) {
                            return false
                        }

                        member.isChatting = event.isChatting

                        sender.sendMessage(
                            translate("&eYou ${if (event.isChatting) "enabled" else "disabled"} party chat!")
                        )
                    }
                }
            }
        }

        if (args.size == 2) {
            if (args[0] == "create" || args[0] == "criar") {
                if (PartyRepository.getParty(sender) != null) {
                    sender.sendMessage(
                        translate("&cThis party already exists!")
                    )

                    return false
                }

                if (PartyRepository.getParty(args[1]) != null) {
                    sender.sendMessage(
                        translate("&cThis party already exists!")
                    )

                    return false
                }

                val party = Party(args[1])

                val member = PartyMember(
                    sender.uniqueId,
                    true
                )

                party.addPlayer(member)

                val event = callTo(
                    PartyCreateEvent(sender, party)
                )

                if (event.isCancelled) {
                    return false
                }

                PartyRepository.addParty(party)

                sender.sendMessage(
                    translate(
                        "&aParty created!"
                    )
                )

                return true
            }

            if (args[0] == "convidar" || args[0] == "invite") {
                val target = Bukkit.getPlayer(
                    args[1]
                )

                if (target == null) {
                    sender.sendMessage(
                        translate("&cThis player aren't online right now.")
                    )

                    return false
                }

                val party = PartyRepository.getParty(sender)

                if (party == null) {
                    sender.sendMessage(
                        translate("&cYou are not in a party!")
                    )

                    return false
                }

                val leader = party.getMember(sender) ?: return false

                if (!leader.isLeader) {
                    sender.sendMessage(
                        translate("&cYou are not the leader of your party!")
                    )

                    return false
                }

                if (PartyRepository.getParty(target) != null) {
                    sender.sendMessage(
                        translate("&cThis player is already in a party!")
                    )

                    return false
                }

                if (InviteRepository.hasInvite(party, target.uniqueId)) {
                    sender.sendMessage(
                        translate("&cThis player already has an invite!")
                    )

                    return false
                }

                val event = callTo(
                    PartyInviteEvent(sender, party)
                )

                if (event.isCancelled) {
                    return false
                }

                InviteRepository.addInvite(
                    Invite(target.uniqueId, sender.uniqueId, party)
                )

                target.sendMessage(
                    translate(
                        "&eYou have been invited to your party by &f${sender.name}&e!"
                    )
                )

                sender.sendMessage(
                    translate(
                        "&aInvite sent!"
                    )
                )

                return true
            }

            if (args[0] == "aceitar" || args[0] == "accept") {
                if (PartyRepository.getParty(sender) != null) {
                    sender.sendMessage(
                        translate("&eYou are already in a party!")
                    )
                } else {
                    val invite = InviteRepository.getInvite(sender.uniqueId, args[1])

                    if (invite == null) {
                        sender.sendMessage(
                            translate("&eThere is no invite from this party name!")
                        )

                        return false
                    }

                    invite.accept()

                    return true
                }
            }

            if (args[0] == "expulsar" || args[0] == "kick") {
                val player = Bukkit.getPlayer(
                    args[1]
                )

                if (player == null) {
                    sender.sendMessage(
                        translate("&cYou must specify a player to kick!")
                    )

                    return false
                }

                val party = PartyRepository.getParty(player)

                if (party == null) {
                    sender.sendMessage(
                        translate("&You aren't in a party!")
                    )

                    return false
                }

                if (player == sender) {
                    sender.sendMessage(
                        translate("&cYou can't kick yourself!")
                    )

                    return false
                }

                val member = party.getMember(sender) ?: return false

                if (!member.isLeader) {
                    sender.sendMessage(
                        translate("&cYou can't kick a player from your party!")
                    )

                    return false
                }

                val user = party.getMember(player)

                if (user == null) {
                    sender.sendMessage(
                        translate("&cThis player isn't in your party!")
                    )

                    return false
                }

                val event = callTo(
                    PartyKickEvent(player, party)
                )

                if (event.isCancelled) {
                    return false
                }

                party.removePlayer(user)

                sender.sendMessage(
                    translate("&eYou kicked a player from your party!")
                )

                return true
            }

            if (args[0] == "promover" || args[0] == "promote") {
                val target = Bukkit.getPlayer(
                    args[1]
                )

                if (target == null) {
                    sender.sendMessage(
                        translate("&eYou must specify a player!")
                    )

                    return false
                }

                val party = PartyRepository.getParty(sender)

                if (party == null) {
                    sender.sendMessage(
                        translate("&eYou can't promote a player if you aren't in a party!")
                    )
                } else {
                    val member = party.getMember(sender)

                    if (member == null) {
                        sender.sendMessage(
                            translate("&eYou can't promote a player if you aren't in a party!")
                        )
                    } else {
                        if (!member.isLeader) {
                            sender.sendMessage(
                                translate("&eYou can't promote a player if you aren't the party leader!")
                            )
                        } else {
                            val targetMember = party.getMember(target)

                            if (targetMember == null) {
                                sender.sendMessage(
                                    translate("&e${target.name} is not in your party!")
                                )
                            } else {
                                val event = callTo(
                                    PartyPromoteEvent(target, sender, party)
                                )

                                if (event.isCancelled) {
                                    return false
                                }

                                targetMember.isLeader = true
                                member.isLeader = false

                                sender.sendMessage(
                                    translate("&eYou promoted ${target.name} to party leader!")
                                )

                                target.sendMessage(
                                    translate("&eYou were promoted to party leader by ${sender.name}!")
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

            if (args[0] == "listar" || args[0] == "list") {
                val page = args[1].toIntOrNull() ?: -1

                if (page < 0) {
                    sender.sendMessage(
                        translate("&ePage number must be greater than 0!")
                    )

                    return false
                }

                val parties = PartyRepository.getParties()

                val maxPage = floor(parties.size / 10.0).toInt()

                if (page > maxPage) {
                    sender.sendMessage(
                        translate("&ePage number must be less than $maxPage!")
                    )

                    return false
                }

                val start = (page - 1) * 10
                val end = start + 10

                val partiesToShow = parties.subList(
                    start,
                    if (end > parties.size) parties.size else end
                )

                if (partiesToShow.isEmpty()) {
                    sender.sendMessage(
                        translate("&eThere are no parties to show!")
                    )

                    return false
                }

                val transformer: (Party) -> CharSequence = { "&6${it.id} &e- &6${it.members.size}" }

                sender.sendMessage(
                    translate(
                        " ",
                        " &eParties:",
                        " &e${partiesToShow.joinToString("\n ", transform = transformer)}",
                        " ",
                        "&eParties: &7(Page $page/$maxPage)"
                    )
                )

                return true
            }

            if (args[0] == "info") {
                val page = args[1].toIntOrNull() ?: -1

                if (page < 0) {
                    sender.sendMessage(
                        translate("&ePage number must be greater than 0!")
                    )

                    return false
                }

                val party = PartyRepository.getParty(sender)

                if (party == null) {
                    sender.sendMessage(
                        translate("&eYou are not in a party!")
                    )

                    return false
                }

                val players = party.members

                val maxPage = floor(players.size / 5.0).toInt()

                if (page > maxPage) {
                    sender.sendMessage(
                        translate("&ePage number must be less than $maxPage!")
                    )

                    return false
                }

                val start = (page - 1) * 5
                val end = start + 5

                val playersToShow = players.subList(
                    start,
                    if (end > players.size) players.size else end
                )

                val transformer: (PartyMember) -> CharSequence =
                    { if (it.isOnline()) "&a${it.getPlayer()?.name}" else "&cUnknown" }

                sender.sendMessage(
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
        }

        return false
    }
}