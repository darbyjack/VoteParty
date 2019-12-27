package me.clip.voteparty

import com.sxtanna.korm.Korm
import me.clip.voteparty.conf.ConfigVoteParty
import org.junit.jupiter.api.Test

object DefaultsTest
{
	
	private val korm = Korm()
	
	@Test
	internal fun testThing()
	{
		val text =
			"""
crate: {
 enabled: true
}
effects: {
  party_command_execute: {
    effects: ["smoke", "hearts"]
    enabled: true
  }
  party_start: {
    effects: ["glyph", "hearts"]
    enabled: true
  }
  vote: {
    effects: ["flames", "hearts"]
    enabled: true
  }
}
party: {
  disabledWorlds: ["world_nether"]
  maxRewardsPerPlayer: 1
  offlineVotes: true
  partyCommands: {
    commands: ["broadcast Party Starting!"]
    enabled: false
  }
  prePartyCommands: {
    commands: ["broadcast Party will start soon!"]
    enabled: false
  }
  rewardCommands: {
    commands: ["eco give {player} 100;0", "give {player} diamond 6;2", "give {player} iron_ingot 12;1"]
    delay: 1
  }
  startDelay: 15
  votesNeeded: 50
}
voting: {
  globalCommands: {
    commands: ["broadcast Only {votes} more votes until a VoteParty!"]
    enabled: true
  }
  guaranteedRewards: {
    commands: ["eco give {player} 10;0", "give {player} steak 8;0"]
    enabled: true
  }
  perVoteRewards: {
    commands: ["eco give {player} 10;0", "give {player} steak 8;0"]
    enabled: true
    max_possible: 1
  }
}
			""".trimIndent()
		
		val thing = korm.pull(text).to<ConfigVoteParty>() ?: return
		println(thing)
		
		thing.merge(ConfigVoteParty.DEF)
		
		println(thing)
	}
}