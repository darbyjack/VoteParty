package me.clip.voteparty.user

import me.clip.voteparty.conf.objects.CumulativeVoteCommands
import me.clip.voteparty.leaderboard.LeaderboardType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.lang.reflect.Modifier
import java.util.UUID

/**
 * The signatures [User] has always had.
 *
 * It sits on the Bukkit services manager and its files are read by anything holding on to a player,
 * so a change here is a change every dependent has to be recompiled against. Keeping track of what
 * a player is owed was not worth a constructor argument and a return type for it.
 */
class UserApiTest
{
	
	@Test
	fun `the constructor still takes the four properties it always did`() {
		val constructor = User::class.java.getDeclaredConstructor(
			UUID::class.java,
			String::class.java,
			List::class.java,
			Integer.TYPE
		)
		
		assertEquals(1, User::class.java.declaredConstructors.size, "the four-argument constructor is no longer the only one")
		assertTrue(Modifier.isPublic(constructor.modifiers))
	}
	
	@Test
	fun `voted still records a vote and returns nothing`() {
		// votedNow() is the one that hands the timestamp back, and it is internal. voted() is the one
		// anything outside this plugin can see, and its JVM descriptor has to stay void.
		val voted = User::class.java.getDeclaredMethod("voted")
		
		assertEquals(Void.TYPE, voted.returnType)
		assertTrue(Modifier.isPublic(voted.modifiers))
		
		val user = user()
		
		user.voted()
		
		assertEquals(1, user.votes().size)
	}
	
	@Test
	fun `the data class still has four properties`() {
		// equals, hashCode, toString, copy and componentN are all built out of the constructor
		// properties, so a field outside the constructor cannot reach any of them.
		val components = User::class.java.declaredMethods.filter { it.name.startsWith("component") }
		val copy = User::class.java.declaredMethods.single { it.name == "copy" }
		
		assertEquals(4, components.size, "a fifth component turned up")
		assertEquals(4, copy.parameterCount, "copy takes an argument the class does not have")
	}
	
	@Test
	fun `waiting rewards are not part of what makes two players the same`() {
		val uuid = UUID.randomUUID()
		val waiting = User(uuid, "Tester", mutableListOf(), 0)
		val plain = User(uuid, "Tester", mutableListOf(), 0)
		
		// A count of nothing and a threshold of nothing is the only way to get something waiting
		// without also adding a vote, which is what the equality check is not about.
		waiting.queueCrossedCumulativeRewards(LeaderboardType.DAILY, Long.MAX_VALUE, listOf(CumulativeVoteCommands(0, emptyList())))
		
		assertEquals(1, waiting.pendingCumulativeRewards().size, "nothing was queued to compare")
		assertEquals(plain, waiting, "waiting rewards changed the player's identity")
		assertEquals(plain.hashCode(), waiting.hashCode())
	}
	
	@Test
	fun `the timestamp handed back is the one that was stored`() {
		// This is what the listener works the cumulative periods out from, so it has to be the same
		// reading that stamped the vote rather than a second one taken afterwards.
		val user = user()
		
		val stamped = user.votedNow()
		
		assertEquals(1, user.votes().size)
		assertEquals(stamped, user.votes().single())
	}
	
	
	private fun user(): User = User(UUID.randomUUID(), "Tester", mutableListOf(), 0)
	
}