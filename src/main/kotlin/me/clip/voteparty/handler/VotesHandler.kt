package me.clip.voteparty.handler

import me.clip.placeholderapi.PlaceholderAPI
import me.clip.voteparty.base.Addon
import me.clip.voteparty.conf.ConfigVoteParty
import me.clip.voteparty.plugin.VotePartyPlugin
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.concurrent.ThreadLocalRandom.current

class VotesHandler(override val plugin: VotePartyPlugin) : Addon
{
	
	private val conf: ConfigVoteParty
		get() = party.conf()
	
	
	fun giveGuaranteedVoteRewards(player: Player)
	{
		if (conf.voting?.guaranteedRewards?.enabled == false)
		{
			return
		}
		
		val cmds = conf.voting?.guaranteedRewards?.commands ?: return
		cmds.forEach()
		{
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPI.setPlaceholders(player, it))
		}
	}
	
	fun giveRandomVoteRewards(player: Player)
	{
		if (conf.voting?.perVoteRewards?.enabled == false)
		{
			return
		}
		
		val cmds = conf.voting?.perVoteRewards?.commands?.takeIf { it.isNotEmpty() } ?: return
		
		repeat(conf.voting?.perVoteRewards?.max_possible ?: 0) {
			val cmd = cmds.random()
			
			if (cmd.chance <= current().nextInt(100))
			{
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPI.setPlaceholders(player, cmd.command))
			}
		}
	}
	
	fun playerVoteEffects(player: Player)
	{
		if (conf.effects?.vote?.enabled == false)
		{
			return
		}
		
		val effects = conf.effects?.vote?.effects?.filterNotNull()?.takeIf { it.isNotEmpty() } ?: return
		
		val loc = player.location
		
		effects.forEach {
			party.hook().display(it, loc, null)
		}
	}
	
}