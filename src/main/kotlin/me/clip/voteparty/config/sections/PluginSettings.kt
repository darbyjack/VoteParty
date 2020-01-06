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
	val PREFIX: Property<String>? = newProperty("settings.prefix", "&d&lV&dote&5&lP&5arty &7&l» ")
	
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