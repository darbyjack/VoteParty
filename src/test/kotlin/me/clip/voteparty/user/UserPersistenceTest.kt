package me.clip.voteparty.user

import com.google.gson.GsonBuilder
import me.clip.voteparty.conf.objects.CumulativeVoteCommands
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * Player data is a per-player JSON file read back through Gson, so a plain Kotlin constructor is
 * not what produces it — Gson allocates the object without calling one. That is what the "settled"
 * marker leans on to tell a file written before VoteParty tracked payouts from a fresh one, and
 * nothing else would notice if that stopped being true.
 *
 * The builder here is the one DatabaseVotePlayerGson uses, settings and all.
 */
class UserPersistenceTest
{
	
	private val gson = GsonBuilder().disableHtmlEscaping().enableComplexMapKeySerialization().setPrettyPrinting().serializeNulls().create()
	
	private val epoch = 1_767_614_400_000L
	
	
	@Test
	fun `a file written before payouts were tracked comes back owed nothing`() {
		// The shape of a file as it was before the settled marker existed.
		val legacy = """
			{
			  "uuid": "${UUID.randomUUID()}",
			  "name": "Tester",
			  "data": [$epoch, ${epoch + 1}, ${epoch + 2}],
			  "claimable": 0
			}
		""".trimIndent()
		
		val user = gson.fromJson(legacy, User::class.java)
		val entries = listOf(CumulativeVoteCommands(1, emptyList()), CumulativeVoteCommands(3, emptyList()))
		
		assertTrue(user.dueCumulativeRewards(epoch, entries).isEmpty(), "a player file from an older build was paid its thresholds again")
	}
	
	@Test
	fun `the settled marker survives a save and a load`() {
		val user = User(UUID.randomUUID(), "Tester", mutableListOf(epoch, epoch + 1), 0)
		val entries = listOf(CumulativeVoteCommands(2, emptyList()))
		
		assertEquals(entries, user.dueCumulativeRewards(epoch, entries))
		user.settleVotes()
		
		val loaded = gson.fromJson(gson.toJson(user, User::class.java), User::class.java)
		
		assertTrue(loaded.dueCumulativeRewards(epoch, entries).isEmpty(), "a settled payout was not remembered across a restart")
	}
	
	@Test
	fun `votes after a save are still owed their threshold`() {
		// The votes are saved before the payout happens, so a restart in between has to leave the
		// threshold owed rather than dropping it.
		val user = User(UUID.randomUUID(), "Tester", mutableListOf(epoch, epoch + 1), 0)
		val entries = listOf(CumulativeVoteCommands(2, emptyList()))
		
		val loaded = gson.fromJson(gson.toJson(user, User::class.java), User::class.java)
		
		assertEquals(entries, loaded.dueCumulativeRewards(epoch, entries))
	}
}