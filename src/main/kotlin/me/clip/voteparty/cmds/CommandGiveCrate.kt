package me.clip.voteparty.cmds

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandIssuer
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Values
import co.aikar.commands.bukkit.contexts.OnlinePlayer

@CommandAlias("vp")
class CommandGiveCrate : BaseCommand() {

    @Description("{@@descriptions.give-crate}")
    fun execute(issuer: CommandIssuer, @Values("@players") target: OnlinePlayer, @Default("1") amount: Int) {
        // Handle giving a crate to a player
    }
}