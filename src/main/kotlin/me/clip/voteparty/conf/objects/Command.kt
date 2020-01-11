package me.clip.voteparty.config.objects

import java.util.concurrent.ThreadLocalRandom

internal data class Command(var chance: Int = 50,
                   var command: String = "")
{
	
	fun shouldExecute(): Boolean
	{
		return chance >= ThreadLocalRandom.current().nextInt(100)
	}
	
}