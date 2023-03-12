package me.clip.voteparty.conf.sections

import ch.jalu.configme.Comment
import ch.jalu.configme.SettingsHolder
import ch.jalu.configme.configurationdata.CommentsConfiguration
import ch.jalu.configme.properties.Property
import ch.jalu.configme.properties.PropertyInitializer.newBeanProperty
import ch.jalu.configme.properties.PropertyInitializer.newProperty
import me.clip.voteparty.conf.objects.NuVotifier

internal object HookSettings : SettingsHolder {

    @JvmField
    @Comment(
        "This property represents whether or not to listen to NuVotifier for incoming votes.",
        "If the backend is set to true, it assumes that the NuVotifier plugin is already enabled on the backend Spigot server,",
        "and pluginMessaging does not need to be enabled. If the backend is disabled, pluginMessaging should be enabled to allow",
        "NuVotifier to work on the proxy server. Additionally, make sure that the channel specified here matches the one set in",
        "your NuVotifier configuration, and that the \"method\" in your NuVotifier config is set to \"pluginMessaging\"."
    )
    val NUVOTIFIER: Property<NuVotifier> = newBeanProperty(
        NuVotifier::class.java, "hooks.votifier", NuVotifier(
            backend = true,
            pluginMessaging = false,
            pluginMessageChannel = "nuvotifier:votes"
        )
    )

    override fun registerComments(conf: CommentsConfiguration) {
        conf.setComment(
            "hooks",
            "The hook part of the config allows you to configure which plugins you would like to hook into for votes.",
            "By default, the plugin will utilize NuVotifier and listen for it's vote events.",
            "If you would like to use the plugin without NuVotifier, just disable the hook!",
            "NOTE: Keep in mind that without being hooked into a vote plugin, the plugin will not automatically handle votes.",
            "You will be required to do everything manually.",
            "Over time, more plugins may become supported!"
        )
    }

}