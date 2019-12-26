package me.clip.voteparty

import me.clip.voteparty.base.State
import me.clip.voteparty.plugin.VotePartyPlugin

class VoteParty internal constructor(private val plugin: VotePartyPlugin) : State
{
	
	
	override fun load()
	{
		println("VoteParty loaded!!")
	}
	
	override fun kill()
	{
		println("VoteParty killed!!")
	}
	
	
	// api methods
	
}