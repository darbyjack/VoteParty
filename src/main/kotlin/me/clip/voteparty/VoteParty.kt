package me.clip.voteparty

import co.aikar.commands.PaperCommandManager
import com.google.gson.Gson
import com.sxtanna.korm.Korm
import me.clip.voteparty.base.State
import me.clip.voteparty.cmds.CommandVoteParty
import me.clip.voteparty.conf.ConfigVoteParty
import me.clip.voteparty.plugin.VotePartyPlugin
import me.clip.voteparty.update.UpdateChecker
import java.io.File
import java.util.*

class VoteParty internal constructor(private val plugin: VotePartyPlugin) : State
{

	private var conf = null as? ConfigVoteParty?
	private val cmds = PaperCommandManager(plugin)


	override fun load()
	{
		loadConfig()
		saveLanguage()
		loadCommands()

		UpdateChecker.check(plugin, 987)
		{

		}
	}

	override fun kill()
	{

	}


	private fun loadConfig()
	{
		val file = plugin.dataFolder.resolve("conf.korm")

		val conf = (KORM.pull(file).to() ?: ConfigVoteParty.DEF).apply()
		{
			merge(ConfigVoteParty.DEF)
		}

		KORM.push(conf, file)

		this.conf = conf
	}

	private fun loadCommands()
	{
		cmds.enableUnstableAPI("help")

		cmds.registerCommand(CommandVoteParty())

		cmds.locales.loadYamlLanguageFile(File(plugin.dataFolder, "en_US.yml"), Locale.ENGLISH)
	}

	private fun saveLanguage() {
		val name = "en_US.yml"
		val file = File(plugin.dataFolder, name)
		if (!file.exists()) {
			this.plugin.saveResource(name, false)
		}
	}


	companion object
	{
		internal val GSON = Gson()
		internal val KORM = Korm()
	}


	// api methods

}