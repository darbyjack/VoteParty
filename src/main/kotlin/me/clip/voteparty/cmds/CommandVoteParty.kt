package me.clip.voteparty.cmds

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandHelp
import co.aikar.commands.CommandIssuer
import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import me.clip.voteparty.base.BASE_PERM

@CommandAlias("vp")
class CommandVoteParty : BaseCommand()
{

    @Subcommand("addvote")
    @Description("{@@descriptions.add-vote}")
    fun addVote(issuer: CommandIssuer, @Default("1") amount: Int)
    {
        // Handle checking to make sure it's a positive amount
        // Check if the votes added makes the votes needed enough to start the party
        // Start the party if enough
    }

    @Subcommand("givecrate")
    @Description("{@@descriptions.give-crate}")
    fun giveCrate(issuer: CommandIssuer, @Values("@players") target: OnlinePlayer, @Default("1") amount: Int)
    {
        // Handle giving a crate to a player
    }

    @Subcommand("setcounter")
    @Description("{@@descriptions.set-counter}")
    fun setCounter(issuer: CommandIssuer, amount: Int)
    {
        // Check if it's positive number
        // Set the votes needed to what was provided
        // Send message saying it was updated
    }

    @Subcommand("startparty")
    @Description("{@@descriptions.start-party}")
    fun startParty(issuer: CommandIssuer)
    {
        // Start the party
    }

    @HelpCommand
    @Description("{@@descriptions.help}")
    @CommandPermission(BASE_PERM + "help")
    fun help(help: CommandHelp)
    {
        help.showHelp()
    }

}