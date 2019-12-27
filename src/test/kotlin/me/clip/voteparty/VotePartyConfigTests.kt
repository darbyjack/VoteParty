package me.clip.voteparty

import com.sxtanna.korm.Korm
import com.sxtanna.korm.writer.KormWriter
import com.sxtanna.korm.writer.base.Options
import me.clip.voteparty.config.VotePartyConfig
import org.bukkit.Material
import org.junit.jupiter.api.Test

object VotePartyConfigTests {

    const val CONFIG =
            """
			effects: {
			  enabled: true
			  effects: [
			    "flames",
			    "hearts"
			  ]
			}
		"""


    private val korm = Korm()


    @Test
    internal fun testConfigI() {
        val config = checkNotNull(korm.pull(CONFIG.trimIndent()).to<VotePartyConfig>())
        {
            "deserialization failed"
        }

        // ACACIA_BOAT("BOAT_ACACIA"),

        println(config)

        if (config.crate == null) {
            println("crate entry not found!")
            config.crate = VotePartyConfig.DEF_CRATE_CONFIG
        }

        if (config.voting == null) {
            println("voting entry not found!")
            config.voting = VotePartyConfig.DEF_VOTE_CONFIG
        }

        if (config.party == null) {
            println("party entry not found!")
            config.party = VotePartyConfig.DEF_PARTY_CONFIG
        }

        if (config.effects == null) {
            println("effects entry not found")
            config.effects = VotePartyConfig.DEF_EFFECTS_CONFIG
        }

        println(config)
    }

    @Test
    internal fun testConfigO() {
        val config = VotePartyConfig(
                VotePartyConfig.EffectsConfig(VotePartyConfig.DEF_VOTE_EFFECTS, VotePartyConfig.DEF_PARTY_START_EFFECTS, VotePartyConfig.DEF_PARTY_COMMAND_EXECUTE_EFFECTS),
                VotePartyConfig.CrateConfig(true, Material.TRAPPED_CHEST),
                VotePartyConfig.VoteConfig(VotePartyConfig.DEF_PER_VOTE_REWARDS, VotePartyConfig.DEF_GUARANTEED_REWARDS, VotePartyConfig.DEF_GLOBAL_COMMANDS),
                VotePartyConfig.PartyConfig(50, listOf("world_nether"), true, 1, 15, VotePartyConfig.DEF_REWARD_COMMANDS, VotePartyConfig.DEF_PRE_PARTY_COMMANDS, VotePartyConfig.DEF_PARTY_COMMANDS)
        )

        println(korm.push(config))
    }

    @Test
    internal fun testCommand() {
        val command0 = VotePartyConfig.Command(10, "say hello")
        val command1 = VotePartyConfig.Command(20, "say hello")
        val command2 = VotePartyConfig.Command(50, "say hello")


        val commands = listOf(
                command0,
                command1,
                command2
        )

        println(korm.push(commands))
    }
}