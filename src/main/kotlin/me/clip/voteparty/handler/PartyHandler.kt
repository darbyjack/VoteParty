package me.clip.voteparty.handler

import me.clip.voteparty.base.Addon
import me.clip.voteparty.base.color
import me.clip.voteparty.base.formMessage
import me.clip.voteparty.base.meta
import me.clip.voteparty.base.name
import me.clip.voteparty.base.reduce
import me.clip.voteparty.base.runTaskLater
import me.clip.voteparty.base.runTaskTimer
import me.clip.voteparty.conf.ConfigVoteParty
import me.clip.voteparty.plugin.VotePartyPlugin
import me.clip.voteparty.version.EffectType
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class PartyHandler(override val plugin: VotePartyPlugin) : Addon
{
	private val conf: ConfigVoteParty
		get() = party.conf()
	
	val crate: ItemStack
		get() = buildCrate(1)
	
	
	fun giveRandomPartyRewards(player: Player)
	{
		val take = conf.party?.max_rewards_per_player?.takeIf { it > 0 } ?: return
		val cmds = conf.party?.reward_commands?.commands?.takeIf { it.isNotEmpty() } ?: return
		
		val iter = cmds.reduce(take).iterator()
		
		plugin.runTaskTimer(conf.party?.reward_commands?.delay ?: 1L)
		{
			if (!iter.hasNext())
			{
				return@runTaskTimer cancel()
			}
			
			server.dispatchCommand(server.consoleSender, formMessage(player, iter.next().command))
		}
	}
	
	fun giveGuaranteedPartyRewards(player: Player)
	{
		executeCommands(conf.party?.guaranteed_rewards?.enabled,
		                conf.party?.guaranteed_rewards?.commands,
		                player)
	}
	
	fun runPrePartyCommands()
	{
		executeCommands(conf.party?.pre_party_commands?.enabled,
		                conf.party?.pre_party_commands?.commands)
	}
	
	fun runPartyCommands()
	{
		executeCommands(conf.party?.party_commands?.enabled,
		                conf.party?.party_commands?.commands)
	}
	
	fun runPartyStartEffects()
	{
		executeEffects(conf.effects?.party_start?.enabled,
		               conf.effects?.party_start?.effects,
		               server.onlinePlayers)
	}
	
	fun runPartyCommandEffects(player: Player)
	{
		executeEffects(conf.effects?.party_command_execute?.enabled,
		               conf.effects?.party_command_execute?.effects,
		               listOf(player))
	}
	
	
	fun buildCrate(amount: Int): ItemStack
	{
		val item = conf.crate?.material?.parseItem(true) ?: ItemStack(Material.CHEST, 1)
		item.amount = amount
		
		return item.meta()
		{
			name = conf.crate?.name ?: "Vote Party Crate"
			lore = conf.crate?.lore?.map(::color)
		}
	}
	
	fun startParty()
	{
		runPrePartyCommands()
		
		plugin.runTaskLater((conf.party?.start_delay ?: 10L) * 20L)
		{
			runPartyCommands()
			runPartyStartEffects()
			
			server.onlinePlayers.forEach()
			{
				giveGuaranteedPartyRewards(it)
				giveRandomPartyRewards(it)
			}
		}
	}
	
	
	private fun executeCommands(enabled: Boolean?, cmds: Collection<String>?, player: Player? = null)
	{
		if (enabled == false)
		{
			return
		}
		
		if (player != null && (player.world in conf.party?.disabled_worlds ?: emptySet()))
		{
			return
		}
		
		cmds?.forEach()
		{
			server.dispatchCommand(server.consoleSender, if (player == null) it else formMessage(player, it))
		}
	}
	
	
	private fun executeEffects(enabled: Boolean?, effects: Collection<EffectType?>?, players: Collection<Player>)
	{
		if (enabled == false)
		{
			return
		}
		
		val targets = players.filter()
		{
			it.world !in conf.party?.disabled_worlds ?: emptySet()
		}
		
		effects?.filterNotNull()?.forEach()
		{ effect ->
			targets.forEach()
			{ player ->
				party.hook().display(effect, player.location, null)
			}
		}
	}
	
}