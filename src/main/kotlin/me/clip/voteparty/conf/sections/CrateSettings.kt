package me.clip.voteparty.conf.sections

import ch.jalu.configme.SettingsHolder
import ch.jalu.configme.properties.Property
import ch.jalu.configme.properties.PropertyInitializer.*
import me.clip.voteparty.plugin.XMaterial

internal object CrateSettings : SettingsHolder
{
	
	@JvmField
	val ENABLED: Property<Boolean> = newProperty("crate.enabled", true)
	
	@JvmField
	val LORE: Property<List<String>> = newListProperty("crate.lore", "", "&7Place the chest in order to", "&7to receive rewards!", "")
	
	@JvmField
	val MATERIAL: Property<XMaterial> = newBeanProperty(XMaterial::class.java,"crate.material", XMaterial.CHEST)
	
	@JvmField
	val NAME: Property<String> = newProperty("crate.name", "&d&lVote&5&lParty &f&lCrate")
	
}