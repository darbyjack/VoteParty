package me.clip.voteparty.config

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

}