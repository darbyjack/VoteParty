package me.clip.voteparty

import com.sxtanna.korm.Korm
import me.clip.voteparty.base.Addon
import me.clip.voteparty.base.State
import me.clip.voteparty.config.VotePartyConfig
import me.clip.voteparty.plugin.VotePartyPlugin

class VoteParty(override val plugin: VotePartyPlugin) : Addon, State
{
	
	private val korm = Korm()
	private var conf = null as VotePartyConfig?
	
	
	override fun load()
	{
	
	}
	
	override fun kill()
	{
	
	}
	
	
	// api methods
	
}