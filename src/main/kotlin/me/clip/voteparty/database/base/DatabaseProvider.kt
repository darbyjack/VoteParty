package me.clip.voteparty.database.base

import me.clip.voteparty.base.Addon
import me.clip.voteparty.base.State

interface DatabaseProvider<C : Any> : State, Addon, DatabaseVotePlayer
{
	
	fun get() : C?
	
}