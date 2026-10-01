package me.clip.voteparty.user

import me.clip.voteparty.conf.objects.CumulativeVoteCommands
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

/**
 * The bookkeeping behind cumulative vote rewards: which thresholds a player has reached but has
 * not been paid for yet.
 *
 * These are the cases the plugin gets wrong if it looks at the vote count on its own. A threshold
 * comes due once, so an exact match on the count loses it the moment the count is not sitting on
 * it — which is what happens whenever the vote that crossed it could not be paid out where it
 * was cast.
 */
class UserCumulativeRewardsTest
{
	
	/** The start of an arbitrary day, so votes can be placed in one period without waiting for it. */
	private val monday: Long = startOf(LocalDate.of(2026, 1, 5))
	
	private val tuesday: Long = startOf(LocalDate.of(2026, 1, 6))
	
	
	@Test
	fun `a threshold is owed once the vote count reaches it, and not before`() {
		val user = user()
		val entry = entry(3)
		
		user.voted(monday)
		user.voted(monday + 1)
		assertTrue(user.dueCumulativeRewards(monday, listOf(entry)).isEmpty(), "owed before the threshold was reached")
		
		user.voted(monday + 2)
		assertEquals(listOf(entry), user.dueCumulativeRewards(monday, listOf(entry)))
	}
	
	@Test
	fun `a settled threshold is never owed again`() {
		val user = user()
		val entry = entry(3)
		
		vote(user, monday, 3)
		assertEquals(listOf(entry), user.dueCumulativeRewards(monday, listOf(entry)))
		
		user.settleVotes()
		assertTrue(user.dueCumulativeRewards(monday, listOf(entry)).isEmpty(), "repaid after being settled")
		
		// Votes past the threshold are the case an exact match got wrong: the count is no longer 3,
		// but the threshold was already paid for and nothing more is owed.
		user.voted(monday + 3)
		user.voted(monday + 4)
		assertTrue(user.dueCumulativeRewards(monday, listOf(entry)).isEmpty(), "repaid on a later vote")
	}
	
	@Test
	fun `every threshold the count ran past is owed, including several at once`() {
		val user = user()
		val entries = listOf(entry(2), entry(4), entry(6))
		
		// Nothing settled, standing in for votes cast where the rewards could not be handed over.
		vote(user, monday, 5)
		
		assertEquals(entries.take(2), user.dueCumulativeRewards(monday, entries), "skipped over the thresholds it ran past")
		
		user.voted(monday + 5)
		assertEquals(entries, user.dueCumulativeRewards(monday, entries), "dropped a threshold on the way past")
	}
	
	@Test
	fun `a threshold crossed where it could not be paid is still owed afterwards`() {
		val user = user()
		val entry = entry(2)
		
		vote(user, monday, 2)
		assertEquals(listOf(entry), user.dueCumulativeRewards(monday, listOf(entry)))
		
		// No settleVotes(): the payout did not happen, so the threshold has to stay owed. The votes
		// carried on in the meantime, which is what used to step over it.
		vote(user, monday, 3)
		
		assertEquals(listOf(entry), user.dueCumulativeRewards(monday, listOf(entry)))
		
		user.settleVotes()
		assertTrue(user.dueCumulativeRewards(monday, listOf(entry)).isEmpty())
	}
	
	@Test
	fun `each period counts its own votes`() {
		val user = user()
		val entry = entry(2)
		
		vote(user, monday, 2)
		user.settleVotes()
		
		// The player had already been paid for two votes in the first period. Two more in the next
		// one put them back on the threshold, which is a second payout of the same entry.
		vote(user, tuesday, 2)
		
		assertEquals(listOf(entry), user.dueCumulativeRewards(tuesday, listOf(entry)))
		assertTrue(user.dueCumulativeRewards(monday, listOf(entry)).isEmpty(), "paid a settled period again")
	}
	
	@Test
	fun `a period that rolls over starts owing nothing`() {
		val user = user()
		val entry = entry(3)
		
		vote(user, monday, 5)
		user.settleVotes()
		
		// One vote into the next period: under the threshold, so nothing comes due even though the
		// player has five votes recorded.
		user.voted(tuesday)
		assertTrue(user.dueCumulativeRewards(tuesday, listOf(entry)).isEmpty())
		
		vote(user, tuesday, 2)
		assertEquals(listOf(entry), user.dueCumulativeRewards(tuesday, listOf(entry)))
	}
	
	@Test
	fun `votes outside the period are left out of it`() {
		val user = user()
		val entry = entry(2)
		
		vote(user, monday, 4)
		user.settleVotes()
		
		// Two votes after the period ended and one before it: the count since the period started is
		// two, which is on the threshold.
		user.voted(tuesday)
		user.voted(tuesday + 1)
		user.voted(monday)
		
		assertEquals(listOf(entry), user.dueCumulativeRewards(tuesday, listOf(entry)))
	}
	
	@Test
	fun `resetting the votes forgets what was settled`() {
		val user = user()
		val entry = entry(2)
		
		vote(user, monday, 2)
		user.settleVotes()
		assertTrue(user.dueCumulativeRewards(monday, listOf(entry)).isEmpty())
		
		user.reset()
		vote(user, monday, 2)
		
		assertEquals(listOf(entry), user.dueCumulativeRewards(monday, listOf(entry)), "a reset left the old high-water mark behind")
	}
	
	@Test
	fun `nothing is owed to a player who has not voted`() {
		val user = user()
		val entry = entry(1)
		
		assertTrue(user.dueCumulativeRewards(monday, listOf(entry)).isEmpty())
	}
	
	@Test
	fun `a player file written before this was tracked is owed nothing`() {
		// The null is what such a file deserialises to: it carries no record of what has been paid,
		// and every vote in it predates the tracking. A null therefore counts as fully settled,
		// otherwise upgrading would pay out every threshold a returning player has already crossed.
		val user = User(UUID.randomUUID(), "Tester", mutableListOf(monday, monday + 1, monday + 2), 0, null)
		val entries = listOf(entry(1), entry(2), entry(3))
		
		assertTrue(user.dueCumulativeRewards(monday, entries).isEmpty())
		
		// Once a payout has happened the mark is real, and the next threshold behaves normally.
		user.settleVotes()
		user.voted(monday + 3)
		
		assertEquals(listOf(entry(4)), user.dueCumulativeRewards(monday, listOf(entry(4))))
	}
	
	
	private fun user(): User = User(UUID.randomUUID(), "Tester", mutableListOf(), 0)
	
	private fun entry(votes: Int): CumulativeVoteCommands = CumulativeVoteCommands(votes, listOf("give %player_name% STEAK 10"))
	
	private fun startOf(date: LocalDate): Long = date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
	
	/** Records [amount] votes, a millisecond apart so they are distinct but all in the same period. */
	private fun vote(user: User, epoch: Long, amount: Int)
	{
		repeat(amount) { user.voted(epoch + it) }
	}
	
}