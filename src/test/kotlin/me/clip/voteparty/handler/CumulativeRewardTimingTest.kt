package me.clip.voteparty.handler

import me.clip.voteparty.conf.objects.CumulativeVoteCommands
import me.clip.voteparty.leaderboard.LeaderboardType
import me.clip.voteparty.user.PendingCumulativeReward
import me.clip.voteparty.user.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

/**
 * A vote is stamped, and then worked out. Midnight can fall between the two.
 *
 * The vote belongs to the day it was cast on. Working the period out from the clock afterwards puts
 * a vote taken at 23:59:59.999 into the day that midnight started, where the count it is measured
 * against does not contain it, so the threshold it reached is never queued.
 *
 * Every timestamp here is fixed, so the case is reached without waiting for a clock to move.
 */
class CumulativeRewardTimingTest
{
	
	private val zone = ZoneId.systemDefault()
	
	private val monday: LocalDate = LocalDate.of(2026, 1, 5)
	private val tuesday: LocalDate = monday.plusDays(1)
	
	private val mondayStart: Long = startOf(monday)
	private val tuesdayStart: Long = startOf(tuesday)
	
	/** One millisecond before the Monday is over. */
	private val lateMonday: Long = tuesdayStart - 1
	
	private val daily = mapOf(LeaderboardType.DAILY to listOf(entry(3)))
	
	
	@Test
	fun `the fixture really does straddle midnight`() {
		assertEquals(monday, localDate(lateMonday))
		assertEquals(tuesday, localDate(tuesdayStart))
		assertEquals(1, tuesdayStart - lateMonday, "the two timestamps are not a millisecond apart")
	}
	
	@Test
	fun `a vote taken a millisecond before midnight is queued against the day it was cast on`() {
		val user = user()
		
		votes(user, lateMonday, 3)
		recordCrossedCumulativeRewards(user, lateMonday, daily)
		
		assertEquals(listOf(PendingCumulativeReward(LeaderboardType.DAILY, mondayStart, 3)), user.pendingCumulativeRewards())
	}
	
	@Test
	fun `working the period out from the new day loses the vote that crossed it`() {
		val user = user()
		
		votes(user, lateMonday, 3)
		
		// The vote that reached the threshold sits a millisecond before this period begins, so
		// measuring it against this period finds nothing at all. Nothing queues, and the threshold
		// is gone for good, which is the whole bug.
		user.queueCrossedCumulativeRewards(LeaderboardType.DAILY, LeaderboardType.DAILY.startAt(tuesdayStart), listOf(entry(3)))
		assertTrue(user.pendingCumulativeRewards().isEmpty(), "the fixture did not miss, so it proves nothing")
		
		// Measured against the day the vote was cast on, it is found.
		user.queueCrossedCumulativeRewards(LeaderboardType.DAILY, LeaderboardType.DAILY.startAt(lateMonday), listOf(entry(3)))
		assertEquals(listOf(PendingCumulativeReward(LeaderboardType.DAILY, mondayStart, 3)), user.pendingCumulativeRewards())
	}
	
	@Test
	fun `a reward earned a millisecond before midnight survives into the next day`() {
		val user = user()
		
		votes(user, lateMonday, 3)
		recordCrossedCumulativeRewards(user, lateMonday, daily)
		
		// The Monday period is over, so its window is empty. Looking at the count again finds
		// nothing; the reward is only known about because it was recorded when the vote landed.
		assertEquals(0, user.votes().count { it >= tuesdayStart })
		
		assertEquals(listOf(PendingCumulativeReward(LeaderboardType.DAILY, mondayStart, 3)), drain(user))
		assertTrue(user.pendingCumulativeRewards().isEmpty())
	}
	
	@Test
	fun `the next day's threshold is its own reward`() {
		val user = user()
		
		votes(user, lateMonday, 3)
		recordCrossedCumulativeRewards(user, lateMonday, daily)
		
		// Three votes on the Tuesday cross the same threshold again, and Monday's reward still
		// waiting must not stand in the way of that.
		votes(user, tuesdayStart + 300, 3)
		recordCrossedCumulativeRewards(user, tuesdayStart + 300, daily)
		
		val expected = listOf(
			PendingCumulativeReward(LeaderboardType.DAILY, mondayStart, 3),
			PendingCumulativeReward(LeaderboardType.DAILY, tuesdayStart, 3)
		)
		
		assertEquals(expected, user.pendingCumulativeRewards(), "the Tuesday reward was swallowed by the Monday one")
		assertEquals(expected, drain(user), "only one of the two reached the player")
		assertTrue(user.pendingCumulativeRewards().isEmpty())
	}
	
	@Test
	fun `a period that does not move is unaffected by the day changing`() {
		// The all-time period starts at the epoch whatever the date, so both readings agree and a
		// vote taken a millisecond before midnight is inside it either way.
		assertEquals(LeaderboardType.ALLTIME.startAt(lateMonday), LeaderboardType.ALLTIME.startAt(tuesdayStart))
	}

	/**
	 * Leaderboards and placeholders are read as of now, and that is what [LeaderboardType.start] is
	 * for. Splitting the rule out to answer for a given day must not have moved it off today.
	 */
	@Test
	fun `a period read now still begins today`() {
		for (period in LeaderboardType.values)
		{
			val start = period.start.invoke()

			assertTrue(
				start <= LocalDateTime.now().plusMinutes(1),
				"${period.name} starts after now: $start",
			)
			assertEquals(period.startOn(LocalDate.now()), start, "${period.name} does not read today")
		}
	}
	
	
	private fun user(): User = User(UUID.randomUUID(), "Tester", mutableListOf(), 0)
	
	private fun entry(votes: Int): CumulativeVoteCommands = CumulativeVoteCommands(votes, listOf("say CUMULATIVE"))
	
	private fun startOf(date: LocalDate): Long = date.atStartOfDay().atZone(zone).toInstant().toEpochMilli()
	
	private fun localDate(epoch: Long): LocalDate = Instant.ofEpochMilli(epoch).atZone(zone).toLocalDate()
	
	/** Records [amount] votes a millisecond apart, the last of them landing exactly on [last]. */
	private fun votes(user: User, last: Long, amount: Int): Long
	{
		var at = last - amount

		repeat(amount)
		{
			at = user.voted(at + 1)
		}

		return at
	}
	
	private fun drain(user: User): List<PendingCumulativeReward>
	{
		val delivered = mutableListOf<PendingCumulativeReward>()
		
		user.drainCumulativeRewards(LeaderboardType.values.toSet()) { delivered.add(it); true }
		
		return delivered
	}
	
}