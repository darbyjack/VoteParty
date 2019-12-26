package me.clip.voteparty.config

import com.sxtanna.korm.data.custom.KormList
import org.bukkit.Material

data class VotePartyConfig(var effects: EffectsConfig?,
                           var crate: CrateConfig?,
						   var voting: VoteConfig?)
{
	
	
	data class EffectsConfig(
			val enabled: Boolean,
			val effects: List<String>
	                        )
	
	data class CrateConfig(
			val enabled: Boolean,
			val material: Material
	                      )

	data class VoteConfig(
			val perVoteRewards: PerVoteRewards,
			val guaranteedRewards: GuaranteedRewards,
			val globalCommands: GlobalCommands
	)

	data class PerVoteRewards(
			val enabled: Boolean,
			val max_possible: Int,
			val commands: List<String>
	)

	data class GuaranteedRewards(
			val enabled: Boolean,
			val commands: List<String>
	)

	data class GlobalCommands(
			val enabled: Boolean,
			val commands: List<String>
	)

	// [1, "eco give %player_name%"]
	
	@KormList(["chance", "command"])
	data class Command(val chance: Int, val command: String)

	companion object
	{
		val DEF_CRATE_CONFIG = CrateConfig(true, Material.CHEST)
		val DEF_PER_VOTE_REWARDS = PerVoteRewards(true, 1, listOf("eco give {player} 10;0", "give {player} steak 8;0"))
		val DEF_GUARANTEED_REWARDS = GuaranteedRewards(true, listOf("eco give {player} 10;0", "give {player} steak 8;0"))
		val DEF_GLOBAL_COMMANDS = GlobalCommands(true, listOf("broadcast Only {votes} more votes until a VoteParty!"))
		val DEF_VOTE_CONFIG = VoteConfig(DEF_PER_VOTE_REWARDS, DEF_GUARANTEED_REWARDS, DEF_GLOBAL_COMMANDS)
	}

}