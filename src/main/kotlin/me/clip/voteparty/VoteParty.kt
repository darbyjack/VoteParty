package me.clip.voteparty

import ch.jalu.configme.SettingsManager
import co.aikar.commands.PaperCommandManager
import com.google.gson.Gson
import me.clip.voteparty.base.State
import me.clip.voteparty.base.color
import me.clip.voteparty.base.runTaskTimer
import me.clip.voteparty.cmds.CommandVoteParty
import me.clip.voteparty.config.ConfigBuilder
import me.clip.voteparty.config.sections.HookSettings
import me.clip.voteparty.config.sections.PartySettings
import me.clip.voteparty.config.sections.PluginSettings
import me.clip.voteparty.handler.PartyHandler
import me.clip.voteparty.handler.VotesHandler
import me.clip.voteparty.listener.CrateListener
import me.clip.voteparty.listener.NuVotifierListener
import me.clip.voteparty.listener.VoteListener
import me.clip.voteparty.placeholders.VotePartyPlaceholders
import me.clip.voteparty.plugin.VotePartyPlugin
import me.clip.voteparty.update.UpdateChecker
import me.clip.voteparty.util.JarFileWalker
import me.clip.voteparty.version.VersionHook
import me.clip.voteparty.version.VersionHookNew
import me.clip.voteparty.version.VersionHookOld
import me.clip.voteparty.voteplayer.VotePlayerHandler
import org.bukkit.Bukkit
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import java.io.File
import java.nio.file.Files
import java.util.Locale
import java.util.logging.Level


class VoteParty internal constructor(internal val plugin: VotePartyPlugin) : State
{
	private var conf = null as? SettingsManager?
	private val cmds = PaperCommandManager(plugin)
	
	private val nuVotifierListener = NuVotifierListener(plugin)
	private val crateListener = CrateListener(plugin)
	private val votifierListener = VoteListener(plugin)
	
	private var hook = null as? VersionHook?
	private var papi = null as? VotePartyPlaceholders?
	
	val votesHandler = VotesHandler(plugin)
	val partyHandler = PartyHandler(plugin)
	
	
	val votePlayerHandler = VotePlayerHandler(plugin)
	
	
	override fun load()
	{
		logo(plugin.server.consoleSender)
		loadConf()
		loadCmds()
		loadHook()
		loadLang()
		registerLang()
		loadPapi()
		loadVotes()
		
		votePlayerHandler.load()
		
		plugin.runTaskTimer(conf().getProperty(PluginSettings.SAVE_INTERVAL).toLong() * 20L)
		{
			saveVotes()
		}
		
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
		
		if (conf().getProperty(HookSettings.NUVOTIFIER))
		{
			nuVotifierListener.load()
		}
		crateListener.load()
		votifierListener.load()
	}
	
	override fun kill()
	{
		saveVotes()
		if (conf().getProperty(HookSettings.NUVOTIFIER))
		{
			nuVotifierListener.kill()
		}
		crateListener.kill()
		votifierListener.kill()
		
		votePlayerHandler.kill()
	}
	
	private fun loadConf()
	{
		if (!Files.exists(File(plugin.dataFolder, "config.yml").toPath()))
		{
			plugin.saveResource("config.yml", false)
		}
		this.conf = ConfigBuilder.create(File(plugin.dataFolder, "config.yml"))
	}
	
	private fun loadLang()
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
	
	fun registerLang()
	{
		plugin.dataFolder.resolve("languages").listFiles()?.forEach()
		{
			if (!it.extension.equals("yml", true))
			{
				return@forEach
			}
			
			val locale = Locale.forLanguageTag(it.nameWithoutExtension)
			cmds.addSupportedLanguage(locale)
			cmds.locales.loadYamlLanguageFile(it, locale)
		}
		
		plugin.logger.info("loaded languages")
	}
	
	private fun loadCmds()
	{
		@Suppress("DEPRECATION")
		cmds.locales.defaultLocale = Locale.forLanguageTag(conf?.getProperty(PluginSettings.LANGUAGE) ?: "en_US")
		
		cmds.commandCompletions.registerCompletion("online")
		{
			plugin.server.onlinePlayers.map(Player::getName)
		}
		
		cmds.commandReplacements.addReplacement("vp", "vp|voteparty")
		
		cmds.registerCommand(CommandVoteParty(this))
		
		plugin.logger.info("loaded commands")
	}
	
	private fun loadHook()
	{
		/**
		 *  MC: 1.8    => '8'
		 *  MC: 1.12.2 => '12'
		 */
		
		this.hook = if (Bukkit.getVersion().substringAfter('.').substringBefore('.').toInt() >= 13)
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
		votesHandler.votes.set(conf().getProperty(PluginSettings.COUNTER))
	}
	
	private fun saveVotes()
	{
		conf().setProperty(PluginSettings.COUNTER, votesHandler.votes.get())
		conf().save()
	}
	
	private fun logo(sender: ConsoleCommandSender)
	{
		val logo =
			"""
				&6 _  _ &d ____
				&6/ )( \&d(  _ \
				&6\ \/ /&d ) __/ &3VoteParty &8v${plugin.description.version}
				&6 \__/ &d(__)   &3Server Version: &8${plugin.server.version}
			"""
		
		sender.sendMessage(color(logo.trimIndent()))
	}
	
	
	fun conf(): SettingsManager
	{
		return checkNotNull(conf)
	}
	
	fun hook(): VersionHook
	{
		return checkNotNull(hook)
	}
	
	fun getVotes(): Int
	{
		return votesHandler.votes.get()
	}
	
	fun getVotesNeeded(): Int
	{
		return conf().getProperty(PartySettings.VOTES_NEEDED) ?: 50
	}
	
	
	companion object
	{
		internal val GSON = Gson()
	}
	
}