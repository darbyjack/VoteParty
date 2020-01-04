package me.clip.voteparty.conf

import com.sxtanna.korm.data.custom.KormComment
import com.sxtanna.korm.data.custom.KormList
import me.clip.voteparty.base.PREFIX
import me.clip.voteparty.conf.base.Config
import me.clip.voteparty.plugin.XMaterial
import me.clip.voteparty.version.EffectType
import org.bukkit.World
import java.util.concurrent.ThreadLocalRandom

data class ConfigVoteParty(var settings: SettingsConfig?,
                           var effects: EffectsConfig?,
                           var crate: CrateConfig?,
                           var voting: VoteConfig?,
                           var party: PartyConfig?
                          ) : Config
{
	
	data class SettingsConfig(
			@KormComment("The default language of the plugin")
			var language: String,
			@KormComment("The prefix of all the messages in the plugin")
			var prefix: String
	                         ) : Config
	
	data class EffectsConfig(
			@KormComment("Configuration for particles when a player votes")
			var vote: Effects,
			@KormComment("Configuration for particles when a party starts")
			var party_start: Effects,
			@KormComment("Configuration for particle effects you can play",
			             "throughout different parts of the plugin", "", "Configuration for particles when party", "commands are being executed")
			var party_command_execute: Effects
	                        ) : Config
	
	
	data class CrateConfig(
			@KormComment("Determines if the crate can be used.", "This is checked in multiple places such as placing the crate, and giving crates.")
			var enabled: Boolean,
			var material: XMaterial,
			var name: String,
			var lore: List<String>
	                      ) : Config
	
	data class VoteConfig(
			@KormComment("Random commands to try and execute on players", "Format is [Chance Percentage, \"Command\"]",
			             "Refer to example in party for how to add more")
			var perVoteRewards: RewardsPerVote,
			@KormComment("Guaranteed commands you want to execute on the player that voted")
			var guaranteedRewards: Commands,
			@KormComment("Configuration for the voting part of the plugin", "", "Global commands to run when a player votes")
			var globalCommands: Commands
	                     ) : Config
	
	
	data class PartyConfig(
			@KormComment("The amount of votes needed to start the party")
			var votesNeeded: Int,
			@KormComment("Configuration for the vote party", "", "Input worlds you would like to disable the party in"
			             , "For example, disabledWorlds: [\"world_nether\"]", "would disable parties in the nether")
			var disabledWorlds: Set<World>,
			@KormComment("Allow offline votes to count towards party")
			var offlineVotes: Boolean,
			@KormComment("Max random rewards from the list below")
			var maxRewardsPerPlayer: Int,
			@KormComment("How long to wait to start party after reaching votes needed")
			var startDelay: Long,
			@KormComment("Random commands to try and execute on players", "Format is [Chance Percentage, \"Command\"]",
			             "For example, if I wanted to give a player", "\$100 from Essentials, with a 90% chance,",
			             "I would do the following:", "[90, \"eco give %player_name% 100\"]")
			var rewardCommands: CommandsReward,
			@KormComment("Configure the commands you want to guarantee the player will get", "In other words, players will ALWAYS get ALL of these")
			var guaranteedRewards: Commands,
			@KormComment("Commands to execute at the beginning of the start delay")
			var prePartyCommands: Commands,
			@KormComment("Commands to execute when a party is starting")
			var partyCommands: Commands
	                      ) : Config
	
	data class RewardsPerVote(
			var enabled: Boolean,
			@KormComment("Max possible commands that will execute from the list above")
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
			@KormComment("How long should the plugin wait", "inbetween each command being executed")
			var delay: Long,
			var commands: List<Command>
	                         ) : Config
	
	
	// [1, "eco give %player_name%"]
	
	@KormList(["chance", "command"])
	data class Command(var chance: Int, var command: String)
	{
		
		fun randomChance(): Boolean
		{
			return chance <= ThreadLocalRandom.current().nextInt(100)
		}
		
	}
	
	
	companion object
	{
		
		private val DEF_CRATE_CONFIG = CrateConfig(true, XMaterial.CHEST, "&b&lVote&f&lParty &7Crate", listOf("&aPlace me &e:)"))
		private val DEF_PER_VOTE_REWARDS = RewardsPerVote(true, 1, listOf(Command(50, "eco give %player_name% 100"), Command(70, "give %player_name% STEAK 10")))
		private val DEF_GUARANTEED_REWARDS = Commands(true, listOf("eco give %player_name% 10", "give %player_name% STEAK 8"))
		private val DEF_GLOBAL_COMMANDS = Commands(true, listOf("broadcast Only %voteparty_votes_required_party% more votes until a VoteParty!"))
		private val DEF_VOTE_CONFIG = VoteConfig(DEF_PER_VOTE_REWARDS, DEF_GUARANTEED_REWARDS, DEF_GLOBAL_COMMANDS)
		private val DEF_REWARD_COMMANDS = CommandsReward(1, listOf(Command(50, "eco give %player_name% 100"), Command(50, "give %player_name% DIAMOND 6"), Command(50, "give %player_name% IRON_INGOT 12")))
		private val DEF_PRE_PARTY_COMMANDS = Commands(true, listOf("broadcast Party will start soon!"))
		private val DEF_PARTY_COMMANDS = Commands(true, listOf("broadcast Party Starting!"))
		private val DEF_PARTY_CONFIG = PartyConfig(50, setOf(), true, 1, 15, DEF_REWARD_COMMANDS, DEF_GUARANTEED_REWARDS, DEF_PRE_PARTY_COMMANDS, DEF_PARTY_COMMANDS)
		private val DEF_VOTE_EFFECTS = Effects(true, listOf(EffectType.FLAME, EffectType.HEART))
		private val DEF_PARTY_START_EFFECTS = Effects(true, listOf(EffectType.SLIME, EffectType.HEART))
		private val DEF_PARTY_COMMAND_EXECUTE_EFFECTS = Effects(true, listOf(EffectType.SMOKE_NORMAL, EffectType.HEART))
		private val DEF_EFFECTS_CONFIG = EffectsConfig(DEF_VOTE_EFFECTS, DEF_PARTY_START_EFFECTS, DEF_PARTY_COMMAND_EXECUTE_EFFECTS)
		private val DEF_SETTINGS_CONFIG = SettingsConfig("en_US", PREFIX)
		
		
		val DEF = ConfigVoteParty(DEF_SETTINGS_CONFIG, DEF_EFFECTS_CONFIG, DEF_CRATE_CONFIG, DEF_VOTE_CONFIG, DEF_PARTY_CONFIG)
		
	}
	
}