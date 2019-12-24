package me.clip.voteparty.plugin

import me.clip.voteparty.VoteParty
import me.clip.voteparty.base.State
import me.clip.voteparty.listeners.VoteListener
import me.clip.voteparty.plugin.base.VotePartyListener
import org.bukkit.plugin.java.JavaPlugin

class VotePartyPlugin : JavaPlugin()
{
	
	private val voteParty = VoteParty(this)
	private val listeners = mutableListOf<VotePartyListener>()
	
	
	override fun onLoad()
	{
		listeners += VoteListener(this)
	}
	
	override fun onEnable()
	{
		voteParty.load()
		listeners.forEach(State::load)
	}
	
	override fun onDisable()
	{
		voteParty.kill()
		listeners.forEach(State::kill)
	}
	
}