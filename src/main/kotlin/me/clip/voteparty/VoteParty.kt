package me.clip.voteparty

import ch.jalu.configme.SettingsManager
import co.aikar.commands.PaperCommandManager
import com.google.gson.Gson
import me.clip.voteparty.base.State
import me.clip.voteparty.bungee.NuVotifierBungeeHandler
import me.clip.voteparty.cmds.CommandVoteParty
import me.clip.voteparty.conf.VoteDataConfiguration
import me.clip.voteparty.conf.VotePartyConfiguration
import me.clip.voteparty.conf.sections.VoteSettings
import me.clip.voteparty.conf.sections.HookSettings
import me.clip.voteparty.conf.sections.PartySettings
import me.clip.voteparty.conf.sections.PluginSettings
import me.clip.voteparty.conf.sections.VoteData
import me.clip.voteparty.data.impl.PartiesCacheGson
import me.clip.voteparty.data.impl.VotedForPartyCacheGson
import me.clip.voteparty.exte.color
import me.clip.voteparty.exte.runTaskTimer
import me.clip.voteparty.exte.runTaskTimerAsync
import me.clip.voteparty.handler.LeaderboardHandler
import me.clip.voteparty.handler.PartyHandler
import me.clip.voteparty.handler.VotesHandler
import me.clip.voteparty.listener.CrateListener
import me.clip.voteparty.listener.HooksListenerNuVotifier
import me.clip.voteparty.listener.VotesListener
import me.clip.voteparty.placeholders.VotePartyPlaceholders
import me.clip.voteparty.plugin.VotePartyPlugin
import me.clip.voteparty.user.UsersHandler
import me.clip.voteparty.util.JarFileWalker
import me.clip.voteparty.util.UpdateChecker
import me.clip.voteparty.version.VersionHook
import me.clip.voteparty.version.VersionHookNew
import me.clip.voteparty.version.VersionHookOld
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.io.InputStream
import java.util.Locale
import java.util.logging.Level

class VoteParty internal constructor(internal val plugin: VotePartyPlugin) : State
{

	val votesHandler = VotesHandler(plugin)
	val partyHandler = PartyHandler(plugin)
	val usersHandler = UsersHandler(plugin)
	val leaderboardHandler = LeaderboardHandler(plugin)


	private var conf = null as? SettingsManager?
	private var voteData = null as? SettingsManager?
	private var audiences = null as? BukkitAudiences?
	private val cmds = PaperCommandManager(plugin)

	private val crateListener = CrateListener(plugin)
	private val votesListener = VotesListener(plugin)
	private val hooksListener = HooksListenerNuVotifier(plugin)
	private val votedForPartyCache = VotedForPartyCacheGson(plugin)
	private val partiesCache = PartiesCacheGson(plugin)

	private var hook = null as? VersionHook?
	private var papi = null as? VotePartyPlaceholders?


	override fun load()
	{
		logo(plugin.server.consoleSender)

		this.audiences = BukkitAudiences.create(plugin)

		loadConf()
		loadVoteData()
		loadCmds()
		loadHook()
		loadPapi()

		saveLang()
		loadLang()

		checkForUpdates()

		// handlers
		votesHandler.load()
		usersHandler.load()
		leaderboardHandler.load()

		// listeners
		crateListener.load()
		votesListener.load()

		// voted for party cache
		votedForPartyCache.load()

		// parties cache
		partiesCache.load()

		if (conf().getProperty(HookSettings.NUVOTIFIER).backend)
		{
			hooksListener.load()
		}

		if (conf().getProperty(HookSettings.NUVOTIFIER).pluginMessaging) {
			NuVotifierBungeeHandler(plugin).load()
		}

		// votes
		loadVotes()

		plugin.runTaskTimerAsync(conf().getProperty(PluginSettings.SAVE_INTERVAL).toLong() * 20L)
		{
			saveVotes()
		}

		plugin.runTaskTimerAsync(conf().getProperty(PluginSettings.PLAYER_SAVE_INTERVAL).toLong() * 20L)
		{
			usersHandler.saveAll()
			votedForPartyCache.save()
			partiesCache.save()
		}

		if (conf().getProperty(VoteSettings.REMINDER_ENABLED)) {
			plugin.runTaskTimer(conf().getProperty(VoteSettings.REMINDER_INTERVAL_SECONDS).toLong() * 20L)
			{
				votesHandler.sendVoteReminders()
			}
		}
	}

	override fun kill()
	{
		saveVotes()
		if (conf().getProperty(HookSettings.NUVOTIFIER).backend)
		{
			hooksListener.kill()
		}
		crateListener.kill()
		votesListener.kill()

		usersHandler.kill()
		leaderboardHandler.kill()
		votedForPartyCache.kill()
		partiesCache.kill()
	}


	private fun loadConf()
	{
		val file = plugin.dataFolder.resolve("config.yml")

		if (!file.exists())
		{
			file.parentFile.mkdirs()
			file.createNewFile()
		}

		this.conf = VotePartyConfiguration(file)
	}

	private fun loadVoteData()
	{
		val file = plugin.dataFolder.resolve("votes.yml")

		if (!file.exists()) {
			file.parentFile.mkdirs()
			file.createNewFile()
		}

		this.voteData = VoteDataConfiguration(file)
	}

	private fun saveLang()
	{
		JarFileWalker.walk("/languages")
		{ path, stream ->

			if (stream == null)
			{
				return@walk // do nothing if the stream couldn't be opened
			}

			val file = plugin.dataFolder.resolve(path.toString().drop(1)).absoluteFile
			if (file.exists())
			{
				mergeLanguage(stream, file)
				return@walk // language file was already created
			}

			file.parentFile.mkdirs()
			file.createNewFile()

			file.outputStream().use()
			{
				stream.copyTo(it)
				stream.close()
			}
		}
	}

	private fun mergeLanguage(stream: InputStream, outside: File)
	{
		val new = YamlConfiguration.loadConfiguration(stream.reader())
		val old = YamlConfiguration.loadConfiguration(outside)

		for (path in new.getKeys(true))
		{
			if (!old.contains(path)) {
				old.set(path, old.get(path, new.get(path)))
			}
		}
		old.save(outside)
	}

	private fun loadCmds()
	{
		if (conf().getProperty(PluginSettings.BRIGADIER))
		{
			cmds.enableUnstableAPI("brigadier")
		}

		cmds.locales.defaultLocale = Locale.forLanguageTag(conf().getProperty(PluginSettings.LANGUAGE) ?: "en-US")
		cmds.usePerIssuerLocale(false, false)

		cmds.commandCompletions.registerCompletion("online")
		{
			plugin.server.onlinePlayers.map(Player::getName)
		}

		cmds.commandReplacements.addReplacement("vp", "vp|voteparty")

		cmds.registerCommand(CommandVoteParty(plugin))
	}

	private fun loadHook()
	{
		this.hook = if (Bukkit.getBukkitVersion().substringBefore('-').substringAfter('.').substringBefore('.').toInt() >= 13)
		{
			VersionHookNew()
		}
		else
		{
			VersionHookOld()
		}
	}

	private fun loadPapi()
	{
		val papi = VotePartyPlaceholders(this)
		papi.register()

		this.papi = papi
	}


	private fun loadVotes()
	{
		votesHandler.setVotes(voteData().getProperty(VoteData.COUNTER))
	}

	private fun saveVotes()
	{
		voteData().setProperty(VoteData.COUNTER, votesHandler.getVotes())
		voteData().save()
	}


	private fun logo(sender: ConsoleCommandSender)
	{
		val logo = LOGO.replace("{plugin_version}", plugin.description.version).replace("{server_version}", plugin.server.version)
		sender.sendMessage(color(logo))
	}

	private fun checkForUpdates()
	{
		UpdateChecker.check(plugin, 987)
		{
			when (it)
			{
				is UpdateChecker.UpdateResult.UP_TO_DATE ->
				{
					plugin.logger.info(it.message)
				}
				is UpdateChecker.UpdateResult.UNRELEASED ->
				{
					plugin.logger.warning(it.message)
				}
				is UpdateChecker.UpdateResult.NEW_UPDATE ->
				{
					plugin.logger.info("${it.message}: ${it.version}")
				}
				is UpdateChecker.UpdateResult.EXCEPTIONS ->
				{
					plugin.logger.log(Level.WARNING, it.message, it.throwable)
				}
			}
		}
	}


	fun loadLang()
	{
		plugin.dataFolder.resolve("languages").listFiles()?.filter()
		{
			it.extension.equals("yml", true)
		}?.forEach()
		{
			val locale = Locale.forLanguageTag(it.nameWithoutExtension)

			cmds.addSupportedLanguage(locale)
			cmds.locales.loadYamlLanguageFile(it, locale)
		}
	}


	fun conf(): SettingsManager
	{
		return checkNotNull(conf)
	}

	fun voteData(): SettingsManager
	{
		return checkNotNull(voteData)
	}

	fun hook(): VersionHook
	{
		return checkNotNull(hook)
	}

	fun manager(): PaperCommandManager
	{
		return cmds
	}

	fun audiences(): BukkitAudiences
	{
		return checkNotNull(audiences)
	}

	fun getVotes(): Int
	{
		return votesHandler.getVotes()
	}

	fun getVotesNeeded(): Int
	{
		return conf().getProperty(PartySettings.VOTES_NEEDED)
	}

	@Deprecated("This was only used in the PlaceholderAPI Placeholders. A new and improved method has been implemented", ReplaceWith("UserHandler#getVotesWithinRange"))
	fun getPlayerVotes(player: OfflinePlayer): Int
	{
		return usersHandler[player].votes().size
	}


	internal companion object
	{
		internal val GSON = Gson()
		internal val LOGO =
			"""
				
				&6 _  _ &d ____
				&6/ )( \&d(  _ \
				&6\ \/ /&d ) __/ &3VoteParty &8v{plugin_version}
				&6 \__/ &d(__)   &3Server Version: &8{server_version}
				
			""".trimIndent()
	}

}
