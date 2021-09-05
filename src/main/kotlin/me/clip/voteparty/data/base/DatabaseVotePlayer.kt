package me.clip.voteparty.data.base

import me.clip.voteparty.base.Addon
import me.clip.voteparty.base.State
import me.clip.voteparty.user.User
import java.util.UUID

internal interface DatabaseVotePlayer : Addon, State
{

	override fun load()

	override fun kill()


	fun load(uuid: UUID): User?

	fun save(data: User)


	fun load(uuid: Collection<UUID>): Map<UUID, User?>
	{
		return uuid.associateWith(::load)
	}

	fun save(data: Collection<User>)
	{
		data.forEach(::save)
	}

	fun reset(data: User)

}
