package me.clip.voteparty.handler

import me.clip.voteparty.base.Addon
import me.clip.voteparty.conf.ConfigVoteParty
import me.clip.voteparty.plugin.VotePartyPlugin
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.concurrent.ThreadLocalRandom.current

class PartyHandler(override val plugin: VotePartyPlugin) : Addon
{
	private val conf: ConfigVoteParty
		get() = party.conf()
	
	fun giveRandomPartyRewards(player: Player)
	{
		val cmds = conf.party?.rewardCommands?.commands?.takeIf { it.isNotEmpty() } ?: return
		
		repeat(conf.party?.maxRewardsPerPlayer ?: 0)
		{
			val cmd = cmds.random()
			
			if (cmd.chance <= current().nextInt(100))
			{
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.command.replace("{player}", player.name))
			}
		}
		
	}
	
	fun giveGuaranteedPartyRewards(player: Player)
	{
		if (conf.party?.guaranteedRewards?.enabled == false)
		{
			return
		}
		
		val cmds = conf.party?.guaranteedRewards?.commands ?: return
		cmds.forEach {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), it.replace("{player}", player.name))
		}
	}
}