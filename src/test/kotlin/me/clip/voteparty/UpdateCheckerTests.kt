package me.clip.voteparty

import me.clip.voteparty.util.UpdateChecker
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

object UpdateCheckerTests
{
	
	@Test
	internal fun testUpdate()
	{
		val result = UpdateChecker.check("1.14.0", 987)
		
		Assertions.assertEquals(UpdateChecker.UpdateResult.UNRELEASED, result)
	}
	
}