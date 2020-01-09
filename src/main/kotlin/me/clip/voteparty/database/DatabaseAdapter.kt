package me.clip.voteparty.database

import me.clip.voteparty.VoteParty
import me.clip.voteparty.database.voteplayer.VotePlayerAdapter
import java.io.IOException

class DatabaseAdapter(voteParty: VoteParty)
{
	val voteParty: VoteParty = voteParty
	var votePlayerAdapter: VotePlayerAdapter? = null
	
	@Throws(IOException::class)
	private fun setUpBackend()
	{
		try
		{
			votePlayerAdapter = VotePlayerAdapter(voteParty)
			votePlayerAdapter?.createContainer()
		} catch (ex: Exception)
		{
			ex.printStackTrace()
		}
	}
	
	init
	{
		setUpBackend()
	}
}
