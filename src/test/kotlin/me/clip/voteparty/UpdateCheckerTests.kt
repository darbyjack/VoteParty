package me.clip.voteparty

import me.clip.voteparty.util.UpdateChecker
import org.junit.jupiter.api.Test

object UpdateCheckerTests
{
	
	@Test
	internal fun testUpdate()
	{
		UpdateChecker.check("1.14.0", 987)
		{ result ->
			println("Result: $result")
		}
	}
}