package me.clip.voteparty.config

import ch.jalu.configme.SettingsManagerImpl
import ch.jalu.configme.configurationdata.ConfigurationData
import ch.jalu.configme.configurationdata.ConfigurationDataBuilder
import ch.jalu.configme.migration.MigrationService
import ch.jalu.configme.migration.PlainMigrationService
import ch.jalu.configme.resource.YamlFileResource
import me.clip.voteparty.config.sections.CrateSettings
import me.clip.voteparty.config.sections.EffectsSettings
import me.clip.voteparty.config.sections.HookSettings
import me.clip.voteparty.config.sections.PartySettings
import me.clip.voteparty.config.sections.PluginSettings
import me.clip.voteparty.config.sections.VoteSettings
import java.io.File

class ConfigBuilder private constructor(resource: YamlFileResource, configurationData: ConfigurationData, migrater: MigrationService) : SettingsManagerImpl(resource, configurationData, migrater)
{
	companion object {
		
		private val SECTION_HOLDERS = arrayOf(
				PluginSettings::class.java,
				HookSettings::class.java,
				CrateSettings::class.java,
				EffectsSettings::class.java,
				PartySettings::class.java,
				VoteSettings::class.java
		                                     )
		
		fun create(file: File): ConfigBuilder {
			val file = YamlFileResource(file)
			val data = ConfigurationDataBuilder.createConfiguration(*SECTION_HOLDERS)
			val migrater = PlainMigrationService()
			
			return ConfigBuilder(file, data, migrater)
		}
	}
}