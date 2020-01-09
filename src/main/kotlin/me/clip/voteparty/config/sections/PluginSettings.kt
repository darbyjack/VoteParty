package me.clip.voteparty.config.sections

import ch.jalu.configme.Comment
import ch.jalu.configme.SettingsHolder
import ch.jalu.configme.configurationdata.CommentsConfiguration
import ch.jalu.configme.properties.Property
import ch.jalu.configme.properties.PropertyInitializer.newProperty

object PluginSettings : SettingsHolder
{
	@JvmField
	@Comment("The default language of the plugin")
	val LANGUAGE: Property<String>? = newProperty("settings.language", "en_US")
	
	@JvmField
	@Comment("The prefix of all the messages in the plugin")
	val PREFIX: Property<String>? = newProperty("settings.prefix", "&d&lVote&5&lParty &7&l» ")
	
	@JvmField
	@Comment("It is suggested that you don't modify this value manually.",
	         "Every X seconds, this value will update to the latest known amount of votes.",
	         "This allows you to persist vote counts through reboots.")
	val COUNTER: Property<Int>? = newProperty("settings.counter.votes", 0)
	
	@JvmField
	@Comment("How often to save the current amount of votes (in seconds)")
	val SAVE_INTERVAL: Property<Int>? = newProperty("settings.counter.save-interval", 60)
	
	override fun registerComments(conf: CommentsConfiguration)
	{
		val pluginHeader = arrayOf(
				"VoteParty",
				"Creator: Clip & Glare",
				"Contributors: https://github.com/VoteParty/VoteParty/graphs/contributors",
				"Issues: https://github.com/VoteParty/VoteParty/issues",
				"Spigot: https://www.spigotmc.org/resources/987/",
				"Wiki: https://wiki.helpch.at/glares-plugins/voteparty",
				"Discord: https://helpch.at/discord"
		                          )
		conf.setComment("settings", *pluginHeader)
	}
}