package me.clip.voteparty.bungee

import com.google.gson.Gson
import com.vexsoftware.votifier.model.Vote
import me.clip.voteparty.base.Addon
import me.clip.voteparty.base.State
import me.clip.voteparty.conf.sections.HookSettings
import me.clip.voteparty.conf.sections.VoteSettings
import me.clip.voteparty.events.VoteReceivedEvent
import me.clip.voteparty.plugin.VotePartyPlugin
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

internal data class BungeeVote(
    val username: String,
    val address: String,
    val timestamp: String,
    val serviceName: String
)

internal class NuVotifierBungeeHandler(override val plugin: VotePartyPlugin) : Addon, State, PluginMessageListener {
    private lateinit var channel: String
    private lateinit var usernameRegex: Pattern
    private val gson = Gson()

    override fun load() {
        this.channel = party.conf().getProperty(HookSettings.NUVOTIFIER).pluginMessageChannel
        usernameRegex = Pattern.compile(party.conf().getProperty(VoteSettings.NAME_REGEX))

        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, channel, this)
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, channel)
    }

    override fun kill() {
        Bukkit.getMessenger().unregisterIncomingPluginChannel(plugin, channel, this)
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(plugin, channel)
    }

    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray?) {
        if (!channel.equals(this.channel, ignoreCase = true)) {
            return
        }

        if (message == null) {
            return
        }

        val content = String(message, StandardCharsets.UTF_8)
        val vote = gson.fromJson(content, BungeeVote::class.java)

        if (party.conf().getProperty(VoteSettings.VALIDATE_NAMES) && !usernameRegex.matcher(vote.username).matches()) {
            plugin.logger.warning("A vote came through NuVotifier (username: ${vote.username}) which did not match the username regex. Throwing away.")
            return
        }

        val player = party.usersHandler[vote.username]?.player() ?: server.getOfflinePlayer(vote.username)
        val event = VoteReceivedEvent(player, vote.serviceName)
        server.pluginManager.callEvent(event)
    }
}