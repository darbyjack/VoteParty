package me.clip.voteparty.conf.objects

import java.util.concurrent.ThreadLocalRandom

internal data class Command(var chance: Double = 50.0,
                            var command: List<String> = listOf(""))
{

	fun shouldExecute(): Boolean
	{
		return chance >= ThreadLocalRandom.current().nextDouble(100.0)
	}

}
