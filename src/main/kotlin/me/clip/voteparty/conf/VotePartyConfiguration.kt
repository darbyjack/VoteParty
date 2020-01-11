package me.clip.voteparty.config

import ch.jalu.configme.SettingsManagerImpl
import ch.jalu.configme.configurationdata.ConfigurationDataBuilder
import ch.jalu.configme.migration.PlainMigrationService
import ch.jalu.configme.resource.YamlFileResource
import me.clip.voteparty.config.sections.CrateSettings
import me.clip.voteparty.config.sections.EffectsSettings
import me.clip.voteparty.config.sections.HookSettings
import me.clip.voteparty.config.sections.PartySettings
import me.clip.voteparty.config.sections.PluginSettings
import me.clip.voteparty.config.sections.VoteSettings
import java.io.File

internal class VotePartyConfiguration(file: File) : SettingsManagerImpl(YamlFileResource(file), ConfigurationDataBuilder.createConfiguration(SECTIONS), PlainMigrationService())
{
	
	private companion object
	{
		
		private val SECTIONS = listOf(
			PluginSettings::class.java,
			HookSettings::class.java,
			CrateSettings::class.java,
			EffectsSettings::class.java,
			PartySettings::class.java,
			VoteSettings::class.java
		                             )
		
	}
	
}