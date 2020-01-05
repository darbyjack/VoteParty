package me.clip.voteparty

import co.aikar.commands.PaperCommandManager
import com.google.gson.Gson
import com.sxtanna.korm.Korm
import com.sxtanna.korm.writer.KormWriter
import com.sxtanna.korm.writer.base.Options
import me.clip.voteparty.base.State
import me.clip.voteparty.cmds.CommandVoteParty
import me.clip.voteparty.conf.ConfigVoteParty
import me.clip.voteparty.handler.PartyHandler
import me.clip.voteparty.handler.VotesHandler
import me.clip.voteparty.listener.CrateListener
import me.clip.voteparty.listener.VoteListener
import me.clip.voteparty.placeholders.VotePartyPlaceholders
import me.clip.voteparty.plugin.VotePartyPlugin
import me.clip.voteparty.plugin.XMaterial
import me.clip.voteparty.update.UpdateChecker
import me.clip.voteparty.util.JarFileWalker
import me.clip.voteparty.version.EffectType
import me.clip.voteparty.version.VersionHook
import me.clip.voteparty.version.VersionHookNew
import me.clip.voteparty.version.VersionHookOld
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.Locale
import java.util.logging.Level

class VoteParty internal constructor(internal val plugin: VotePartyPlugin) : State
{
	
	private var conf = null as? ConfigVoteParty?
	private val cmds = PaperCommandManager(plugin)
	
	private val voteListener = VoteListener(plugin)
	private val crateListener = CrateListener(plugin)
	
	private var hook = null as? VersionHook?
	private var papi = null as? VotePartyPlaceholders?
	
	val votesHandler = VotesHandler(plugin)
	val partyHandler = PartyHandler(plugin)
	
	
	override fun load()
	{
		loadConf()
		loadCmds()
		loadHook()
		loadLang()
		loadPapi()
		
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
		
		voteListener.load()
		crateListener.load()
	}
	
	override fun kill()
	{
		voteListener.kill()
		crateListener.kill()
	}
	
	
	fun loadConf()
	{
		val file = plugin.dataFolder.resolve("conf.yml")
		
		val conf = (KORM.pull(file).to() ?: ConfigVoteParty.DEF).apply()
		{
			mergeJ(ConfigVoteParty.DEF)
		}
		
		KORM.push(conf, file)
		
		this.conf = conf
		
		plugin.logger.info("loaded config")
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
		cmds.enableUnstableAPI("help")
		
		cmds.locales.defaultLocale = Locale.forLanguageTag(conf?.settings?.language ?: "en_US")
		
		cmds.commandCompletions.registerCompletion("online")
		{
			plugin.server.onlinePlayers.map(Player::getName)
		}
		
		cmds.registerCommand(CommandVoteParty(this))
		
		plugin.logger.info("loaded commands")
	}
	
	private fun loadHook()
	{
		val hook = if ("MC: 1.8" in Bukkit.getVersion())
		{
			VersionHookOld()
		}
		else
		{
			VersionHookNew()
		}
		
		this.hook = hook
		
		plugin.logger.info("loaded hook: ${hook::class.java.simpleName}")
	}
	
	private fun loadPapi()
	{
		val papi = VotePartyPlaceholders(this)
		papi.register()
		
		this.papi = papi
	}
	
	
	fun conf(): ConfigVoteParty
	{
		return conf ?: ConfigVoteParty.DEF
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
		return conf().party?.votes_needed ?: 50
	}
	
	
	companion object
	{
		internal val GSON = Gson()
		internal val KORM = Korm(writer = KormWriter(2, Options.min(Options.LIST_ENTRY_ON_NEW_LINE)))
		
		init
		{
			KORM.pullWith<XMaterial> { _, types ->
				types.firstOrNull()?.asBase()?.dataAsString()?.let(XMaterial::matchXMaterial)?.orElse(null)
			}
			
			KORM.pullWith<EffectType> { _, types ->
				types.firstOrNull()?.asBase()?.dataAsString()?.let(EffectType.Companion::find)
			}
			
			KORM.codecBy<World, String>(Bukkit::getWorld) {
				it?.name
			}
		}
	}
	
}