package me.clip.voteparty.conf.sections

import ch.jalu.configme.Comment
import ch.jalu.configme.SettingsHolder
import ch.jalu.configme.configurationdata.CommentsConfiguration
import ch.jalu.configme.properties.Property
import ch.jalu.configme.properties.PropertyInitializer.newProperty

internal object HookSettings : SettingsHolder
{
	
	@JvmField
	@Comment("Would you like to listen to NuVotifier for incoming votes?")
	val NUVOTIFIER: Property<Boolean> = newProperty("hooks.nuvotifier", true)
	
	override fun registerComments(conf: CommentsConfiguration)
	{
		conf.setComment("hooks",
		                "The hook part of the config allows you to configure which plugins you would like to hook into for votes.",
		                "By default, the plugin will utilize NuVotifier and listen for it's vote events.",
		                "If you would like to use the plugin without NuVotifier, just disable the hook!",
		                "NOTE: Keep in mind that without being hooked into a vote plugin, the plugin will not automatically handle votes.",
		                "You will be required to do everything manually.",
		                "Over time, more plugins may become supported!")
	}
	
}