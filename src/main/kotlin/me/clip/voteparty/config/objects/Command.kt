package me.clip.voteparty.config.objects

import java.util.concurrent.ThreadLocalRandom

data class Command(var chance: Int, var command: String)
{
	fun randomChance(): Boolean
	{
		return chance <= ThreadLocalRandom.current().nextInt(100)
	}
}