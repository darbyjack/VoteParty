package me.clip.voteparty.user

import java.util.UUID

data class User(val uuid: UUID, var name: String, private val data: MutableList<Long>, var claimable: Int)
{
	
	fun voted()
	{
		data += System.currentTimeMillis()
	}
	
	fun votes(): List<Long>
	{
		return data
	}
	
	fun reset()
	{
		data.clear()
	}

}
