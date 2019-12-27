package me.clip.voteparty.handler

import me.clip.voteparty.base.Addon
import me.clip.voteparty.conf.ConfigVoteParty
import me.clip.voteparty.plugin.VotePartyPlugin
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class VoteHandler(override val plugin: VotePartyPlugin) : Addon
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
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), it.replace("{player}", player.name))
		}
	}
 
}