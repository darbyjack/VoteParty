package me.clip.voteparty.cmds

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandIssuer
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description

@CommandAlias("vp")
class CommandStartParty : BaseCommand() {

    @Description("{@@descriptions.start-party}")
    fun execute(issuer: CommandIssuer) {
        // Start the party
    }

}