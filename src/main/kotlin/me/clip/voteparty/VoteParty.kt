package me.clip.voteparty

import co.aikar.commands.PaperCommandManager
import com.google.gson.Gson
import com.sxtanna.korm.Korm
import me.clip.voteparty.base.State
import me.clip.voteparty.cmds.CommandVoteParty
import me.clip.voteparty.conf.ConfigVoteParty
import me.clip.voteparty.handler.PartyHandler
import me.clip.voteparty.handler.VotesHandler
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
import java.util.Locale

class VoteParty internal constructor(private val plugin: VotePartyPlugin) : State
{
	
	private var conf = null as? ConfigVoteParty?
	private val cmds = PaperCommandManager(plugin)
	
	private val voteListener = VoteListener(plugin)
	
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
		
		}
		
		voteListener.load()
	}
	
	override fun kill()
	{
		voteListener.kill()
	}
	
	
	private fun loadConf()
	{
		val file = plugin.dataFolder.resolve("conf.korm")
		
		val conf = (KORM.pull(file).to() ?: ConfigVoteParty.DEF).apply()
		{
			merge(ConfigVoteParty.DEF)
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
		
		cmds.registerCommand(CommandVoteParty())
		
		cmds.registerDependency(VotesHandler::class.java, votesHandler)
		cmds.registerDependency(PartyHandler::class.java, partyHandler)
		
		// Temp for now
		cmds.locales.defaultLocale = Locale.forLanguageTag(conf?.settings?.language ?: "en_US")
		
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
		val papi = VotePartyPlaceholders()
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
	
	
	companion object
	{
		internal val GSON = Gson()
		internal val KORM = Korm()
		
		init
		{
			KORM.pullWith<XMaterial> { _, types ->
				types.firstOrNull()?.asBase()?.dataAsString()?.let(XMaterial::matchXMaterial)?.orElse(null)
			}
			
			KORM.pullWith<EffectType> { _, types ->
				types.firstOrNull()?.asBase()?.dataAsString()?.let(EffectType.Companion::find)
			}
		}
	}
	
}