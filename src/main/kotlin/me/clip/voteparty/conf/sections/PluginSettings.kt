package me.clip.voteparty.conf.sections

import ch.jalu.configme.Comment
import ch.jalu.configme.SettingsHolder
import ch.jalu.configme.configurationdata.CommentsConfiguration
import ch.jalu.configme.properties.Property
import ch.jalu.configme.properties.PropertyInitializer.newProperty

internal object PluginSettings : SettingsHolder
{

	@JvmField
	@Comment("The default language of the plugin")
	val LANGUAGE: Property<String> = newProperty("settings.language", "en-US")

	@JvmField
	@Comment("The prefix of all the messages in the plugin")
	val PREFIX: Property<String> = newProperty("settings.prefix", "&d&lVote&5&lParty &7&l» ")

	@JvmField
	@Comment("How often to save the current amount of votes (in seconds)")
	val SAVE_INTERVAL: Property<Int> = newProperty("settings.counter.save-interval", 300)

	@JvmField
	@Comment("How often do we want to save the player data (in seconds)")
	val PLAYER_SAVE_INTERVAL: Property<Int> = newProperty("settings.data.save-interval", 300)

	@JvmField
	@Comment("Do you want to save a player's data instantly on vote? This will happen along with the interval saving above.",
	"Some servers just prefer to save instantly to ensure all the data is there.")
	val SAVE_ON_VOTE: Property<Boolean> = newProperty("settings.data.save-on-vote", false)

	@JvmField
	@Comment("Would you like to enable Brigadier command support? (I think it does more than just pretty colors) 1.13+")
	val BRIGADIER: Property<Boolean> = newProperty("settings.brigadier", false);


	override fun registerComments(conf: CommentsConfiguration)
	{
		conf.setComment("settings",
		                "VoteParty",
		                "Creator: Clip & Glare",
		                "Contributors: https://github.com/VoteParty/VoteParty/graphs/contributors",
		                "Issues: https://github.com/VoteParty/VoteParty/issues",
		                "Spigot: https://www.spigotmc.org/resources/987/",
		                "Wiki: https://wiki.helpch.at/glares-plugins/voteparty",
		                "Discord: https://helpch.at/discord")
	}

}
