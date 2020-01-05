package me.clip.voteparty.config.sections

import ch.jalu.configme.Comment
import ch.jalu.configme.SettingsHolder
import ch.jalu.configme.configurationdata.CommentsConfiguration
import ch.jalu.configme.properties.Property
import ch.jalu.configme.properties.PropertyInitializer.newBeanProperty
import me.clip.voteparty.config.objects.Effects
import me.clip.voteparty.version.EffectType


object EffectsSettings : SettingsHolder
{
	@JvmField
	@Comment("Configuration for particles when party commands are being executed")
	val PARTY_COMMAND_EXECUTE: Property<Effects> = newBeanProperty(Effects::class.java, "effects.party_commands_execute", Effects(true, listOf(EffectType.SMOKE_NORMAL, EffectType.HEART)))
	
	@JvmField
	@Comment("Configuration for particles when a party starts")
	val PARTY_START: Property<Effects> = newBeanProperty(Effects::class.java, "effects.party_start", Effects(true, listOf(EffectType.SLIME, EffectType.HEART)))
	
	@JvmField
	@Comment("Configuration for particles when a player votes")
	val VOTE: Property<Effects> = newBeanProperty(Effects::class.java, "effects.vote", Effects(true, listOf(EffectType.FLAME, EffectType.HEART)))
	
	override fun registerComments(conf: CommentsConfiguration)
	{
		val pluginHeader = arrayOf(
				"Configuration for particle effects you can play",
				"throughout different parts of the plugin")
		conf.setComment("effects", *pluginHeader)
	}
}