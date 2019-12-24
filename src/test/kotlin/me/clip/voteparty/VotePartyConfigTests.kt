package me.clip.voteparty

import com.sxtanna.korm.Korm
import com.sxtanna.korm.writer.KormWriter
import com.sxtanna.korm.writer.base.Options
import me.clip.voteparty.config.VotePartyConfig
import org.bukkit.Material
import org.junit.jupiter.api.Test

object VotePartyConfigTests
{
	
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
	
	
	private val korm = Korm(writer = KormWriter(2, Options.min(Options.LIST_ENTRY_ON_NEW_LINE)))
	
	
	@Test
	internal fun testConfigI()
	{
		val config = checkNotNull(korm.pull(CONFIG.trimIndent()).to<VotePartyConfig>())
		{
			"deserialization failed"
		}
		
		// ACACIA_BOAT("BOAT_ACACIA"),
		
		println(config)
		
		if (config.crate == null)
		{
			println("crate entry not found!")
			config.crate = VotePartyConfig.CrateConfig.DEF
		}
		
		println(config)
	}
	
	@Test
	internal fun testConfigO()
	{
		val config = VotePartyConfig(
			VotePartyConfig.EffectsConfig(true, listOf("flames", "hearts")),
			VotePartyConfig.CrateConfig(true, Material.TRAPPED_CHEST)
		                            )
		
		println(korm.push(config))
	}
	
}