package me.clip.voteparty

import com.google.gson.Gson
import com.sxtanna.korm.Korm
import me.clip.voteparty.base.State
import me.clip.voteparty.conf.ConfigVoteParty
import me.clip.voteparty.plugin.VotePartyPlugin
import me.clip.voteparty.update.UpdateChecker

class VoteParty internal constructor(private val plugin: VotePartyPlugin) : State
{
	
	private var conf = null as? ConfigVoteParty?
	
	
	override fun load()
	{
		loadConfig()
		
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
	
	
	companion object
	{
		internal val GSON = Gson()
		internal val KORM = Korm()
	}
	
	
	// api methods
	
}