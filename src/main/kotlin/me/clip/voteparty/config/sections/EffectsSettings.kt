package me.clip.voteparty.config.sections

import ch.jalu.configme.Comment
import ch.jalu.configme.SettingsHolder
import ch.jalu.configme.configurationdata.CommentsConfiguration
import ch.jalu.configme.properties.Property
import ch.jalu.configme.properties.PropertyInitializer.newBeanProperty
import me.clip.voteparty.config.objects.Effects


object EffectsSettings : SettingsHolder
{
	@JvmField
	@Comment("Configuration for particles when party commands are being executed")
	val PARTY_COMMAND_EXECUTE: Property<Effects>? = newBeanProperty(Effects::class.java, "effects.party_commands_execute", Effects(true, listOf("SMOKE_NORMAL", "HEART"), 0.0, 0.0, 0.0, 0.1, 2))

	@JvmField
	@Comment("Configuration for particles when a party starts")
	val PARTY_START: Property<Effects>? = newBeanProperty(Effects::class.java, "effects.party_start", Effects(true, listOf("SMOKE_NORMAL", "HEART"), 0.0, 0.0, 0.0, 0.1, 2))
	
	@JvmField
	@Comment("Configuration for particles when a player votes")
	val VOTE: Property<Effects>? = newBeanProperty(Effects::class.java, "effects.vote", Effects(true, listOf("SMOKE_NORMAL", "HEART"), 0.0, 0.0, 0.0, 0.1, 2))
	
	override fun registerComments(conf: CommentsConfiguration)
	{
		val pluginHeader = arrayOf(
				"Configuration for particle effects you can play",
				"throughout different parts of the plugin")
		conf.setComment("effects", *pluginHeader)
	}
}