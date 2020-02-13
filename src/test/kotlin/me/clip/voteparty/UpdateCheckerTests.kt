package me.clip.voteparty

import me.clip.voteparty.util.UpdateChecker
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

object UpdateCheckerTests
{
	
	@Test
	internal fun testUpdate()
	{
		val result = UpdateChecker.check("2.20", 987)
		
		Assertions.assertEquals(UpdateChecker.UpdateResult.UNRELEASED, result)
	}
	
}