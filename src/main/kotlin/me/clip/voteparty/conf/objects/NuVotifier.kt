package me.clip.voteparty.conf.objects

internal data class NuVotifier(
    var backend: Boolean = true,
    var pluginMessaging: Boolean = false,
    var pluginMessageChannel: String = "nuvotifier:votes"
)