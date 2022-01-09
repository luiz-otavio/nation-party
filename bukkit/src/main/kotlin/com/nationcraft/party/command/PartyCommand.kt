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
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.math.ceil
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
                    " &e/party create <name> &6- &eCrie uma party.",
                    " &e/party invite <player> &6- &eConvide seu amigo para seu grupo.",
                    " &e/party accept <name> &6- &eAceita o convite de seu amigo.",
                    " &e/party promote <player> &6- &ePromova um membro para lider.",
                    " &e/party leave &6- &eSaia do seu grupo.",
                    " &e/party kick <player> &6- &eExpulsa um membro do seu grupo.",
                    " &e/party disband &6- &eDesfaz seu grupo.",
                    " &e/party list &6- &eListe todas as parties do servidor.",
                    " &e/party info &6- &eListe informações sobre seu grupo.",
                    " &e/party chat &6- &eHabilite ou desabilite o chat do seu grupo.",
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
                    sender.error("Você não está em nenhum grupo para realizar essa operação.")

                    return false
                }

                val member = party.getMember(sender) ?: return false

                if (member.isLeader) {
                    sender.error("Você é o lider do grupo! Para sair, promova alguém ou use /party disband.")

                    return false
                }

                val event = callTo(
                    PartyLeaveEvent(sender, party)
                )

                if (event.isCancelled) {
                    return false
                }

                party.removePlayer(member)

                sender.success(
                    "Você saiu do grupo ${party.id}."
                )

                val message = translate(
                    "&e[Party] &6${sender.name} &e saiu do grupo &6${party.id}&e."
                )

                for (target in party.members) {
                    val user = target.getPlayer() ?: continue

                    user.sendMessage(message)

                    user.playSound(
                        user.location,
                        Sound.ORB_PICKUP,
                        1f,
                        1f
                    )
                }
            }

            if (args[0] == "disband" || args[0] == "deletar") {
                val party = PartyRepository.getParty(sender)

                if (party == null) {
                    sender.error(
                        "Você não está em nenhum grupo para realizar essa operação."
                    )
                } else {
                    val member = party.getMember(sender) ?: return false

                    if (!member.isLeader) {
                        sender.error(
                            "Você não é o lider do grupo! Para deletar, seja promovido ou use /party leave."
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

                    val message = translate(
                        "&e[Party] &6${sender.name} &e deletou o grupo &6${party.id}&e."
                    )

                    for (target in party.members) {
                        val user = target.getPlayer() ?: continue

                        user.sendMessage(message)

                        user.playSound(
                            user.location,
                            Sound.ENDERDRAGON_GROWL,
                            1f,
                            1f
                        )
                    }

                    PartyRepository.removeParty(party)

                    sender.success(
                        "Você deletou o grupo ${party.id}."
                    )

                    return true
                }
            }

            if (args[0] == "listar" || args[0] == "list") {
                val parties = PartyRepository.getParties()

                val maxPage = ceil(parties.size / 10.0)
                    .toInt()

                val start = 0 * 10
                val end = start + 10

                val partiesToShow = parties.subList(
                    start,
                    if (end > parties.size) parties.size else end
                )

                if (partiesToShow.isEmpty()) {
                    sender.error(
                        "Não há nenhum grupo para mostrar."
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
                    sender.error(
                        "Você não está em nenhum grupo para realizar essa operação."
                    )

                    return false
                }

                val players = party.members

                val maxPage = ceil(players.size / 5.0)
                    .toInt()

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
                    sender.error(
                        "Você não está em nenhum grupo para realizar essa operação."
                    )
                } else {
                    val member = party.getMember(sender) ?: return false

                    val event = callTo(
                        PartyUpdateChattingEvent(party, sender, !member.isChatting)
                    )

                    if (event.isCancelled) {
                        return false
                    }

                    member.isChatting = event.isChatting

                    sender.success(
                        "Você &6${if (event.isChatting) "ativou" else "desativou"} &eo chat do grupo."
                    )
                }
            }
        }

        if (args.size == 2) {
            if (args[0] == "create" || args[0] == "criar") {
                if (PartyRepository.getParty(sender) != null) {
                    sender.error(
                        "Você já está em um grupo."
                    )

                    return false
                }

                if (PartyRepository.getParty(args[1]) != null) {
                    sender.error(
                        "Esse grupo de jogadores já existe."
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

                sender.success(
                    "Você criou um grupo de jogadores com o nome de &6${party.id}&e."
                )

                return true
            }

            if (args[0] == "convidar" || args[0] == "invite") {
                val target = Bukkit.getPlayer(
                    args[1]
                )

                if (target == null) {
                    sender.error(
                        "O jogador &6${args[1]} &enão está online."
                    )

                    return false
                }

                val party = PartyRepository.getParty(sender)

                if (party == null) {
                    sender.error(
                        "Você não está em nenhum grupo para realizar essa operação."
                    )

                    return false
                }

                val leader = party.getMember(sender) ?: return false

                if (!leader.isLeader) {
                    sender.error(
                        "Você não é o líder do grupo para realizar essa operação."
                    )

                    return false
                }

                if (PartyRepository.getParty(target) != null) {
                    sender.error(
                        "O jogador &6${target.name} &ejá está em um grupo."
                    )

                    return false
                }

                if (InviteRepository.hasInvite(party, target.uniqueId)) {
                    sender.error(
                        "O jogador &6${target.name} &ejá recebeu um convite para o grupo."
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

                target.success(
                    "&6${sender.name} &einvitou você para o grupo &6${party.id}&e."
                )

                sender.success(
                    "Você convidou &6${target.name} &epara o grupo &6${party.id}&e."
                )

                return true
            }

            if (args[0] == "aceitar" || args[0] == "accept") {
                if (PartyRepository.getParty(sender) != null) {
                    sender.error(
                        "Você já está em um grupo."
                    )
                } else {
                    val invite = InviteRepository.getInvite(sender.uniqueId, args[1])

                    if (invite == null) {
                        sender.error(
                            "Você não recebeu nenhum convite para o grupo &6${args[1]}&e."
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
                    sender.error(
                        "O jogador &6${args[1]} &enão está online."
                    )

                    return false
                }

                val party = PartyRepository.getParty(player)

                if (party == null) {
                    sender.error(
                        "O jogador &6${player.name} &enão está em nenhum grupo."
                    )

                    return false
                }

                if (player == sender) {
                    sender.error(
                        "Você não pode expulsar você mesmo."
                    )

                    return false
                }

                val member = party.getMember(sender) ?: return false

                if (!member.isLeader) {
                    sender.error(
                        "Você não é o líder do grupo para realizar essa operação."
                    )

                    return false
                }

                val user = party.getMember(player)

                if (user == null) {
                    sender.error(
                        "O jogador &6${player.name} &enão está no grupo."
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

                player.success(
                    "&6${sender.name} &eexpulsou você do grupo &6${party.id}&e."
                )

                return true
            }

            if (args[0] == "promover" || args[0] == "promote") {
                val target = Bukkit.getPlayer(
                    args[1]
                )

                if (target == null) {
                    sender.error(
                        "O jogador &6${args[1]} &enão está online."
                    )

                    return false
                }

                val party = PartyRepository.getParty(sender)

                if (party == null) {
                    sender.error(
                        "Você não está em nenhum grupo."
                    )
                } else {
                    val member = party.getMember(sender) ?: return false

                    if (!member.isLeader) {
                        sender.error(
                            "Você não é o líder do grupo para realizar essa operação."
                        )
                    } else {
                        val targetMember = party.getMember(target)

                        if (targetMember == null) {
                            sender.error(
                                "O jogador &6${target.name} &enão está no grupo."
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

                            sender.success(
                                "&6${target.name} &efoi promovido a líder do grupo &6${party.id}&e."
                            )

                            target.success(
                                "&6${sender.name} &epromoveu você a líder do grupo &6${party.id}&e."
                            )

                            val message = translate(
                                "&e[Party] &6${sender.name} &efoi promovido à lider do grupo."
                            )

                            for (each in party.members) {
                                val user = each.getPlayer() ?: continue

                                user.sendMessage(message)

                                user.playSound(
                                    user.location,
                                    Sound.LEVEL_UP,
                                    1F,
                                    1F
                                )
                            }
                        }
                    }
                }
            }

            if (args[0] == "listar" || args[0] == "list") {
                val page = args[1].toIntOrNull() ?: -1

                if (page < 0) {
                    sender.error(
                        "A página deve ser um número inteiro positivo."
                    )

                    return false
                }

                val parties = PartyRepository.getParties()

                val maxPage = ceil(parties.size / 10.0)
                    .toInt()

                if (page > maxPage) {
                    sender.error(
                        "A página não existe."
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
                    sender.error(
                        "Não há nenhum grupo para mostrar."
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
                    sender.error(
                        "A página deve ser um número inteiro positivo."
                    )

                    return false
                }

                val party = PartyRepository.getParty(sender)

                if (party == null) {
                    sender.error(
                        "Você não está em um grupo."
                    )

                    return false
                }

                val players = party.members

                val maxPage = ceil(players.size / 5.0)
                    .toInt()

                if (page > maxPage) {
                    sender.error(
                        "A página não existe."
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

    private fun CommandSender.success(message: String) {
        sendMessage(
            translate(
                "&a[Party] &e$message"
            )
        )

        if (this is Player) {
            playSound(location, Sound.ORB_PICKUP, 1f, 1f)
        }
    }

    private fun CommandSender.error(message: String) {
        sendMessage(
            translate(
                "&c$message"
            )
        )

        if (this is Player) {
            playSound(location, Sound.VILLAGER_NO, 1f, 1f)
        }
    }
}