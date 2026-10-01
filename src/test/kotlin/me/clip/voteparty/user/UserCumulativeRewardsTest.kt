package me.clip.voteparty.user

import me.clip.voteparty.conf.objects.CumulativeVoteCommands
import me.clip.voteparty.leaderboard.LeaderboardType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

/**
 * The bookkeeping behind cumulative vote rewards: which thresholds a player has reached and have
 * not been handed over yet.
 *
 * A vote appends one timestamp, so the count for a period moves up by exactly one and a threshold
 * sits on that count for exactly one vote. That is what makes a crossing detectable at the moment
 * it happens, which is the only moment it can be, since the count of all the votes keeps moving
 * past it afterwards.
 */
class UserCumulativeRewardsTest
{
	
	private val periods = LeaderboardType.values.toSet()
	
	/** An arbitrary Monday and the Tuesday after it, so a period can end without waiting a day. */
	private val monday: Long = startOf(LocalDate.of(2026, 1, 5))
	private val tuesday: Long = startOf(LocalDate.of(2026, 1, 6))
	
	
	@Test
	fun `a threshold is queued on the vote that reaches it, and not before`() {
		val user = user()
		
		vote(user, monday, 2)
		assertTrue(pending(user).isEmpty(), "queued before the threshold was reached")
		
		vote(user, monday, 1)
		assertEquals(listOf(PendingCumulativeReward(LeaderboardType.DAILY, 3)), pending(user))
	}
	
	@Test
	fun `a threshold is queued once however often the payout path runs`() {
		val user = user()
		
		vote(user, monday, 5)
		
		val delivered = drain(user)
		drain(user)
		
		assertEquals(listOf(PendingCumulativeReward(LeaderboardType.DAILY, 3)), delivered, "handed over more than once")
		assertTrue(pending(user).isEmpty())
	}
	
	@Test
	fun `votes past a threshold queue nothing further`() {
		val user = user()
		
		vote(user, monday, 6)
		
		assertEquals(listOf(PendingCumulativeReward(LeaderboardType.DAILY, 3)), pending(user))
	}
	
	@Test
	fun `every threshold the player goes past is queued`() {
		val user = user()
		val entries = listOf(entry(2), entry(4), entry(6))
		
		vote(user, monday, 6, entries)
		
		assertEquals(
			listOf(2, 4, 6).map { PendingCumulativeReward(LeaderboardType.DAILY, it) },
			pending(user),
		)
	}
	
	@Test
	fun `each period counts only the votes inside it`() {
		val user = user()
		
		user.voted(monday)
		user.voted(monday + 1)
		user.voted(tuesday)
		
		// Three votes since Monday, one since Tuesday. Each period's threshold is judged against
		// its own count, so the same three votes answer to both.
		user.queueCrossedCumulativeRewards(LeaderboardType.DAILY, monday, listOf(entry(3)))
		user.queueCrossedCumulativeRewards(LeaderboardType.WEEKLY, tuesday, listOf(entry(1), entry(3)))
		
		assertEquals(
			listOf(
				PendingCumulativeReward(LeaderboardType.DAILY, 3),
				PendingCumulativeReward(LeaderboardType.WEEKLY, 1)
			),
			pending(user),
		)
	}
	
	@Test
	fun `a queued threshold outlives the period it was reached in`() {
		// 23:59 on Monday, three votes into the day, nobody there to hand it over.
		val user = user()
		
		vote(user, monday, 3)
		assertEquals(listOf(PendingCumulativeReward(LeaderboardType.DAILY, 3)), pending(user))
		
		// 00:01 on Tuesday. The Monday count is gone, and the new day's is empty.
		vote(user, tuesday, 1)
		assertEquals(1, votesSince(user, tuesday), "the fixture did not actually roll the period over")
		assertEquals(listOf(PendingCumulativeReward(LeaderboardType.DAILY, 3)), pending(user), "lost a reward when its period ended")
		
		// Logging back in is what pays it, and Tuesday's own vote is a short way from any threshold.
		assertEquals(listOf(PendingCumulativeReward(LeaderboardType.DAILY, 3)), drain(user))
		assertTrue(pending(user).isEmpty())
	}
	
	@Test
	fun `a period queues again after rolling over`() {
		val user = user()
		
		vote(user, monday, 3)
		drain(user)
		assertTrue(pending(user).isEmpty())
		
		// The same three votes the next day are a second crossing, and a second payout.
		vote(user, tuesday, 3)
		
		assertEquals(listOf(PendingCumulativeReward(LeaderboardType.DAILY, 3)), pending(user))
	}
	
	@Test
	fun `a reward is handed over once and then forgotten`() {
		val user = user()
		val delivered = mutableListOf<PendingCumulativeReward>()
		
		vote(user, monday, 5)
		
		assertEquals(1, user.drainCumulativeRewards(periods) { delivered.add(it) })
		assertEquals(0, user.drainCumulativeRewards(periods) { delivered.add(it) }, "handed the same reward over twice")
		
		assertEquals(listOf(PendingCumulativeReward(LeaderboardType.DAILY, 3)), delivered)
		assertTrue(pending(user).isEmpty())
	}
	
	@Test
	fun `a payout that throws leaves the reward waiting`() {
		val user = user()
		
		vote(user, monday, 3)
		
		assertThrows<IllegalStateException>
		{
			user.drainCumulativeRewards(periods) { throw IllegalStateException("the command failed") }
		}
		
		// Paying it again is the point: a lost reward is worse than a repeated command.
		assertEquals(listOf(PendingCumulativeReward(LeaderboardType.DAILY, 3)), pending(user))
		assertEquals(listOf(PendingCumulativeReward(LeaderboardType.DAILY, 3)), drain(user))
	}
	
	@Test
	fun `a period that is not being paid out keeps its reward`() {
		val user = user()
		
		vote(user, monday, 3)
		
		// Paying the weekly period, as the compatibility entry points do, leaves the daily one.
		assertEquals(0, user.drainCumulativeRewards(setOf(LeaderboardType.WEEKLY)) { })
		assertEquals(listOf(PendingCumulativeReward(LeaderboardType.DAILY, 3)), pending(user))
	}
	
	@Test
	fun `resetting the votes drops the rewards waiting to be paid`() {
		val user = user()
		
		vote(user, monday, 3)
		assertEquals(1, pending(user).size)
		
		user.reset()
		
		assertTrue(pending(user).isEmpty())
	}
	
	@Test
	fun `nothing is queued for a player who has not voted`() {
		val user = user()
		
		queue(user, LeaderboardType.DAILY, monday)
		
		assertTrue(pending(user).isEmpty())
	}
	
	
	private fun user(): User = User(UUID.randomUUID(), "Tester", mutableListOf(), 0)
	
	private fun entry(votes: Int): CumulativeVoteCommands = CumulativeVoteCommands(votes, listOf("give %player_name% STEAK 10"))
	
	private fun startOf(date: LocalDate): Long = date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
	
	/** Records [amount] votes a millisecond apart, running the crossing check on each one. */
	private fun vote(user: User, epoch: Long, amount: Int, entries: List<CumulativeVoteCommands> = listOf(entry(3)))
	{
		repeat(amount)
		{ index ->
			user.voted(epoch + index)
			queue(user, LeaderboardType.DAILY, epoch, entries)
		}
	}
	
	private fun queue(user: User, period: LeaderboardType, since: Long, entries: List<CumulativeVoteCommands> = listOf(entry(2)))
	{
		user.queueCrossedCumulativeRewards(period, since, entries)
	}
	
	private fun pending(user: User): List<PendingCumulativeReward> = user.pendingCumulativeRewards()
	
	private fun drain(user: User): List<PendingCumulativeReward>
	{
		val delivered = mutableListOf<PendingCumulativeReward>()
		
		user.drainCumulativeRewards(periods) { delivered.add(it) }
		
		return delivered
	}
	
	private fun votesSince(user: User, epoch: Long): Int = user.votes().count { it >= epoch }
	
}