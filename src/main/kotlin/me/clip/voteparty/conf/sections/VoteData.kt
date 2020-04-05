package me.clip.voteparty.conf.sections

import ch.jalu.configme.SettingsHolder
import ch.jalu.configme.properties.Property
import ch.jalu.configme.properties.PropertyInitializer

internal object VoteData : SettingsHolder
{
	@JvmField
	val COUNTER: Property<Int> = PropertyInitializer.newProperty("votes", 0)
}