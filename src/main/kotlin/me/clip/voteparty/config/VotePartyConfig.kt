package me.clip.voteparty.config

import com.sun.org.apache.xpath.internal.operations.Bool
import com.sxtanna.korm.data.custom.KormList
import org.bukkit.Material

data class VotePartyConfig(var effects: EffectsConfig?,
                           var crate: CrateConfig?,
						   var voting: VoteConfig?,
						   var party: PartyConfig)
{
	
	
	data class EffectsConfig(
			val voteEffects: VoteEffects,
			val partyStartEffects: PartyStartEffects,
			val partyCommandExecuteEffects: PartyCommandExecuteEffects
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

	data class PartyConfig(
			val votesNeeded: Int,
			val disabledWorlds: List<String>,
			val offlineVotes: Boolean,
			val maxRewardsPerPlayer: Int,
			val startDelay: Int,
			val rewardCommands: RewardCommands,
			val prePartyCommands: PrePartyCommands,
			val partyCommands: PartyCommands
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

	data class RewardCommands(
			val delay: Int,
			val commands: List<String>
	)

	data class PrePartyCommands(
			val enabled: Boolean,
			val commands: List<String>
	)

	data class PartyCommands(
			val enabled: Boolean,
			val commands: List<String>
	)

	data class VoteEffects(
			val enabled: Boolean,
			val effects: List<String>
	)

	data class PartyStartEffects(
			val enabled: Boolean,
			val effects: List<String>
	)

	data class PartyCommandExecuteEffects(
			val enabled: Boolean,
			val effects: List<String>
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
		val DEF_REWARD_COMMANDS = RewardCommands(1, listOf("eco give {player} 100;0", "give {player} diamond 6;2", "give {player} iron_ingot 12;1"))
		val DEF_PRE_PARTY_COMMANDS = PrePartyCommands(false, listOf("broadcast Party will start soon!"))
		val DEF_PARTY_COMMANDS = PartyCommands(false, listOf("broadcast Party Starting!"))
		val DEF_PARTY_CONFIG = PartyConfig(50, listOf("world_nether"), true, 1, 15, DEF_REWARD_COMMANDS, DEF_PRE_PARTY_COMMANDS, DEF_PARTY_COMMANDS)
		val DEF_VOTE_EFFECTS = VoteEffects(true, listOf("flames", "hearts"))
		val DEF_PARTY_START_EFFECTS = PartyStartEffects(true, listOf("glyph", "hearts"))
		val DEF_PARTY_COMMAND_EXECUTE_EFFECTS = PartyCommandExecuteEffects(true, listOf("smoke", "hearts"))
		val DEF_EFFECTS_CONFIG = EffectsConfig(DEF_VOTE_EFFECTS, DEF_PARTY_START_EFFECTS, DEF_PARTY_COMMAND_EXECUTE_EFFECTS)
	}

}