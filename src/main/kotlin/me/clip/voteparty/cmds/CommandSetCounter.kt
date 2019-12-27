package me.clip.voteparty.cmds

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandIssuer
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description

@CommandAlias("vp")
class CommandSetCounter : BaseCommand() {

    @Description("{@@descriptions.set-counter}")
    fun execute(issuer: CommandIssuer, amount : Int) {
        if (amount <= 0) {
            return;
        }
        // Set the votes needed to the command
        // Send message saying counter was updated
    }

}