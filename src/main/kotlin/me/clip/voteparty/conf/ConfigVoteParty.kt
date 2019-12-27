package me.clip.voteparty.conf

import com.sxtanna.korm.data.custom.KormList
import me.clip.voteparty.conf.base.Config
import org.bukkit.Material

data class ConfigVoteParty(var effects: EffectsConfig?, var crate: CrateConfig?, var voting: VoteConfig?, var party: PartyConfig?) : Config
{
	
	data class EffectsConfig(
			var vote: Effects,
			var party_start: Effects,
			var party_command_execute: Effects
	                        ) : Config
	
	
	data class CrateConfig(
			var enabled: Boolean,
			var material: Material
	                      ) : Config
	
	data class VoteConfig(
			var perVoteRewards: RewardsPerVote,
			var guaranteedRewards: Commands,
			var globalCommands: Commands
	                     ) : Config
	
	
	data class PartyConfig(
			var votesNeeded: Int,
			var disabledWorlds: List<String>,
			var offlineVotes: Boolean,
			var maxRewardsPerPlayer: Int,
			var startDelay: Int,
			var rewardCommands: CommandsReward,
			var prePartyCommands: Commands,
			var partyCommands: Commands
	                      ) : Config
	
	data class RewardsPerVote(
			var enabled: Boolean,
			var max_possible: Int,
			var commands: List<String>
	                         ) : Config
	
	
	data class Effects(
			var enabled: Boolean,
			var effects: List<String>
	                  ) : Config
	
	data class Commands(
			var enabled: Boolean,
			var commands: List<String>
	                   ) : Config
	
	data class CommandsReward(
			var delay: Int,
			var commands: List<String>
	                         ) : Config
	
	
	// [1, "eco give %player_name%"]
	
	@KormList(["chance", "command"])
	data class Command(var chance: Int, var command: String)
	
	
	companion object
	{
		
		private val DEF_CRATE_CONFIG = CrateConfig(true, Material.CHEST)
		private val DEF_PER_VOTE_REWARDS = RewardsPerVote(true, 1, listOf("eco give {player} 10;0", "give {player} steak 8;0"))
		private val DEF_GUARANTEED_REWARDS = Commands(true, listOf("eco give {player} 10;0", "give {player} steak 8;0"))
		private val DEF_GLOBAL_COMMANDS = Commands(true, listOf("broadcast Only {votes} more votes until a VoteParty!"))
		private val DEF_VOTE_CONFIG = VoteConfig(DEF_PER_VOTE_REWARDS, DEF_GUARANTEED_REWARDS, DEF_GLOBAL_COMMANDS)
		private val DEF_REWARD_COMMANDS = CommandsReward(1, listOf("eco give {player} 100;0", "give {player} diamond 6;2", "give {player} iron_ingot 12;1"))
		private val DEF_PRE_PARTY_COMMANDS = Commands(false, listOf("broadcast Party will start soon!"))
		private val DEF_PARTY_COMMANDS = Commands(false, listOf("broadcast Party Starting!"))
		private val DEF_PARTY_CONFIG = PartyConfig(50, listOf("world_nether"), true, 1, 15, DEF_REWARD_COMMANDS, DEF_PRE_PARTY_COMMANDS, DEF_PARTY_COMMANDS)
		private val DEF_VOTE_EFFECTS = Effects(true, listOf("flames", "hearts"))
		private val DEF_PARTY_START_EFFECTS = Effects(true, listOf("glyph", "hearts"))
		private val DEF_PARTY_COMMAND_EXECUTE_EFFECTS = Effects(true, listOf("smoke", "hearts"))
		private val DEF_EFFECTS_CONFIG = EffectsConfig(DEF_VOTE_EFFECTS, DEF_PARTY_START_EFFECTS, DEF_PARTY_COMMAND_EXECUTE_EFFECTS)
		
		
		val DEF = ConfigVoteParty(DEF_EFFECTS_CONFIG, DEF_CRATE_CONFIG, DEF_VOTE_CONFIG, DEF_PARTY_CONFIG)
		
	}
	
}