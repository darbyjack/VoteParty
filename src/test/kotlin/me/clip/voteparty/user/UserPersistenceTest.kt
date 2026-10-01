package me.clip.voteparty.user

import com.google.gson.GsonBuilder
import me.clip.voteparty.conf.objects.CumulativeVoteCommands
import me.clip.voteparty.leaderboard.LeaderboardType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * Player data is a per-player JSON file read back through Gson, so a plain Kotlin constructor is
 * not what produces it. Two things about that shape matter here: a file written before rewards
 * were tracked has no waiting-reward list at all, and a reward queued after that file was read
 * still has to come back out of it.
 *
 * The builder is the one DatabaseVotePlayerGson uses, settings and all.
 */
class UserPersistenceTest
{
	
	private val gson = GsonBuilder().disableHtmlEscaping().enableComplexMapKeySerialization().setPrettyPrinting().serializeNulls().create()
	
	private val epoch = 1_767_614_400_000L
	
	
	private fun entry(votes: Int): CumulativeVoteCommands = CumulativeVoteCommands(votes, listOf("give %player_name% STEAK 10"))
	
	/**
	 * Reads back a player file in the shape it had before waiting rewards were recorded.
	 */
	private fun readLegacy(votes: Int): User
	{
		return gson.fromJson(
			"""
				{
				  "uuid": "${UUID.randomUUID()}",
				  "name": "Tester",
				  "data": [${List(votes) { epoch + it }.joinToString(", ")}],
				  "claimable": 0
				}
			""".trimIndent(),
			User::class.java
		)
	}
	
	@Test
	fun `a file written before rewards were tracked comes back owed nothing`() {
		val user = readLegacy(5)
		
		// Nothing is owed for the votes it already holds. They were either paid under the old code
		// or, where the count had moved past the threshold, never payable at all.
		assertTrue(user.pendingCumulativeRewards().isEmpty())
	}
	
	@Test
	fun `votes after an old file has been read still queue`() {
		// The bug this covers: reading the file back left a settlement baseline that grew with every
		// vote appended afterwards, so those votes counted as reached and nothing was ever queued.
		val user = readLegacy(5)
		
		assertEquals(5, user.votes().size)
		
		repeat(3) { user.voted(epoch + 100 + it) }
		user.queueCrossedCumulativeRewards(LeaderboardType.DAILY, epoch, listOf(entry(8)))
		
		assertEquals(listOf(PendingCumulativeReward(LeaderboardType.DAILY, epoch, 8)), user.pendingCumulativeRewards())
	}
	
	@Test
	fun `a queued reward survives a save and a load`() {
		val user = User(UUID.randomUUID(), "Tester", mutableListOf(epoch, epoch + 1, epoch + 2), 0)

		user.queueCrossedCumulativeRewards(LeaderboardType.DAILY, epoch, listOf(entry(3)))

		val loaded = gson.fromJson(gson.toJson(user, User::class.java), User::class.java)

		assertEquals(listOf(PendingCumulativeReward(LeaderboardType.DAILY, epoch, 3)), loaded.pendingCumulativeRewards())
	}

	/**
	 * The waiting rewards are held in a field in the class body rather than a constructor property,
	 * so that the constructor keeps its four arguments. Gson writes and reads body fields exactly as
	 * it does constructor ones, and this is what says so.
	 */
	@Test
	fun `the waiting rewards are written into the player file`() {
		val user = User(UUID.randomUUID(), "Tester", mutableListOf(epoch, epoch + 1, epoch + 2), 0)

		user.queueCrossedCumulativeRewards(LeaderboardType.DAILY, epoch, listOf(entry(3)))

		val json = gson.toJson(user, User::class.java)

		assertTrue(json.contains("\"pending\""), "nothing was written for the waiting rewards: $json")
		assertTrue(json.contains("DAILY"), "the period is missing from the file: $json")
		// The four properties the file has always carried, and nothing else alongside them.
		assertTrue(json.contains("\"uuid\""), "uuid went missing: $json")
		assertTrue(json.contains("\"name\""), "name went missing: $json")
		assertTrue(json.contains("\"data\""), "data went missing: $json")
		assertTrue(json.contains("\"claimable\""), "claimable went missing: $json")
	}

	@Test
	fun `a file written before rewards were tracked gains them and reads back`() {
		val user = readLegacy(2)

		assertTrue(user.pendingCumulativeRewards().isEmpty(), "an old file came back owing something")

		user.voted(epoch + 100)
		user.queueCrossedCumulativeRewards(LeaderboardType.DAILY, epoch, listOf(entry(3)))

		val loaded = gson.fromJson(gson.toJson(user, User::class.java), User::class.java)

		assertEquals(listOf(PendingCumulativeReward(LeaderboardType.DAILY, epoch, 3)), loaded.pendingCumulativeRewards())
		assertEquals(3, loaded.votes().size, "the votes in an old file went missing")
	}
	
	@Test
	fun `a reward handed over does not come back after a save`() {
		val user = User(UUID.randomUUID(), "Tester", mutableListOf(epoch, epoch + 1, epoch + 2), 0)
		
		user.queueCrossedCumulativeRewards(LeaderboardType.DAILY, epoch, listOf(entry(3)))
		user.drainCumulativeRewards(LeaderboardType.values.toSet()) { true }
		
		val loaded = gson.fromJson(gson.toJson(user, User::class.java), User::class.java)
		
		assertTrue(loaded.pendingCumulativeRewards().isEmpty(), "a payout was handed over again after a restart")
	}
	
	@Test
	fun `a save taken before the payout leaves the reward owed`() {
		// The listener saves when it records a vote, before the payout runs, so a restart in between
		// has to leave the reward waiting rather than dropping it.
		val user = User(UUID.randomUUID(), "Tester", mutableListOf(epoch, epoch + 1, epoch + 2), 0)
		
		user.queueCrossedCumulativeRewards(LeaderboardType.DAILY, epoch, listOf(entry(3)))
		
		val loaded = gson.fromJson(gson.toJson(user, User::class.java), User::class.java)
		
		assertEquals(listOf(PendingCumulativeReward(LeaderboardType.DAILY, epoch, 3)), loaded.pendingCumulativeRewards())
	}
}