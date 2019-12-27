package me.clip.voteparty.cmds

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandIssuer
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Description

@CommandAlias("vp")
class CommandAddVote : BaseCommand() {

    @Description("{@@descriptions.add-vote}")
    fun execute(issuer: CommandIssuer, @Default("1") amount: Int) {
        if (amount <= 0) {
            // Cannot add no or negative vote
        }
        // Check if the votes added makes the votes needed enough to start the party
        // Start the party if enough
    }
}