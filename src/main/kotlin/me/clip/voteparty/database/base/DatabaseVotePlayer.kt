package me.clip.voteparty.database.base

import me.clip.voteparty.voteplayer.VotePlayer
import java.util.UUID

interface DatabaseVotePlayer : Database
{
	
	override fun load()
	
	override fun kill()
	
	
	fun load(uuid: UUID): VotePlayer?
	
	fun save(data: VotePlayer)
	
	
	fun loadBulk(uuid: Collection<UUID>): Map<UUID, VotePlayer?>
	{
		return uuid.associateWith(::load)
	}
	
	fun saveBulk(data: Collection<VotePlayer>)
	{
		data.forEach(::save)
	}
	
}