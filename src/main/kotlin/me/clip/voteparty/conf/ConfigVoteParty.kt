package me.clip.voteparty.conf

import com.sxtanna.korm.data.custom.KormList
import me.clip.voteparty.conf.base.Config
import me.clip.voteparty.plugin.XMaterial
import me.clip.voteparty.version.EffectType

data class ConfigVoteParty(var settings: SettingsConfig?, var effects: EffectsConfig?, var crate: CrateConfig?, var voting: VoteConfig?, var party: PartyConfig?) : Config
{
	
	data class SettingsConfig(
			var language: String,
			var prefix: String
	) : Config
	
	data class EffectsConfig(
			var vote: Effects,
			var party_start: Effects,
			var party_command_execute: Effects
	) : Config
	
	
	data class CrateConfig(
			var enabled: Boolean,
			var material: XMaterial,
			var name: String,
			var lore: List<String>
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
			var startDelay: Long,
			var rewardCommands: CommandsReward,
			var guaranteedRewards: Commands,
			var prePartyCommands: Commands,
			var partyCommands: Commands
	) : Config
	
	data class RewardsPerVote(
			var enabled: Boolean,
			var max_possible: Int,
			var commands: List<Command>
	) : Config
	
	
	data class Effects(
			var enabled: Boolean,
			var effects: List<EffectType?>?
	) : Config
	
	data class Commands(
			var enabled: Boolean,
			var commands: List<String>
	) : Config
	
	data class CommandsReward(
			var delay: Int,
			var commands: List<Command>
	) : Config
	
	
	// [1, "eco give %player_name%"]
	
	@KormList(["chance", "command"])
	data class Command(var chance: Int, var command: String)
	
	
	companion object
	{
		
		private val DEF_CRATE_CONFIG = CrateConfig(true, XMaterial.CHEST, "&b&lVote&f&lParty &7Crate", listOf("&aPlace me &e:)"))
		private val DEF_PER_VOTE_REWARDS = RewardsPerVote(false, 1, listOf(Command(50, "eco give %player_name% 100"), Command(70, "give %player_name% STEAK 10")))
		private val DEF_GUARANTEED_REWARDS = Commands(true, listOf("eco give %player_name% 10", "give %player_name% STEAK 8"))
		private val DEF_GLOBAL_COMMANDS = Commands(true, listOf("broadcast Only %whatever_this_placeholder_is% more votes until a VoteParty!"))
		private val DEF_VOTE_CONFIG = VoteConfig(DEF_PER_VOTE_REWARDS, DEF_GUARANTEED_REWARDS, DEF_GLOBAL_COMMANDS)
		private val DEF_REWARD_COMMANDS = CommandsReward(1, listOf(Command(50, "eco give %player_name% 100"), Command(50, "give %player_name% DIAMOND 6"), Command(50, "give %player_name% IRON_INGOT 12")))
		private val DEF_PRE_PARTY_COMMANDS = Commands(false, listOf("broadcast Party will start soon!"))
		private val DEF_PARTY_COMMANDS = Commands(false, listOf("broadcast Party Starting!"))
		private val DEF_PARTY_CONFIG = PartyConfig(50, listOf("world_nether"), true, 1, 15, DEF_REWARD_COMMANDS, DEF_GUARANTEED_REWARDS, DEF_PRE_PARTY_COMMANDS, DEF_PARTY_COMMANDS)
		private val DEF_VOTE_EFFECTS = Effects(true, listOf(EffectType.FLAME, EffectType.HEART))
		private val DEF_PARTY_START_EFFECTS = Effects(true, listOf(EffectType.SLIME, EffectType.HEART))
		private val DEF_PARTY_COMMAND_EXECUTE_EFFECTS = Effects(true, listOf(EffectType.SMOKE_NORMAL, EffectType.HEART))
		private val DEF_EFFECTS_CONFIG = EffectsConfig(DEF_VOTE_EFFECTS, DEF_PARTY_START_EFFECTS, DEF_PARTY_COMMAND_EXECUTE_EFFECTS)
		private val DEF_SETTINGS_CONFIG = SettingsConfig("en_US", "&d&lV&dote&5&lP&5arty &7&l» ")
		
		
		val DEF = ConfigVoteParty(DEF_SETTINGS_CONFIG, DEF_EFFECTS_CONFIG, DEF_CRATE_CONFIG, DEF_VOTE_CONFIG, DEF_PARTY_CONFIG)
		
	}
	
}