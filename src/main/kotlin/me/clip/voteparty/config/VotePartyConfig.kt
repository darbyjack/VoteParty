package me.clip.voteparty.config

import com.sxtanna.korm.data.custom.KormList
import org.bukkit.Material

data class VotePartyConfig(var effects: EffectsConfig?,
                           var crate: CrateConfig?)
{
	
	
	data class EffectsConfig(
			val enabled: Boolean,
			val effects: List<String>
	                        )
	
	data class CrateConfig(
			val enabled: Boolean,
			val material: Material
	                      )
	{
		
		companion object
		{
			val DEF = CrateConfig(true, Material.CHEST)
		}
		
	}
	
	
	// [1, "eco give %player_name%"]
	
	@KormList(["chance", "command"])
	data class Command(val chance: Int, val command: String)

}