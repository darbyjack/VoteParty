package me.clip.voteparty.conf

import ch.jalu.configme.SettingsManagerImpl
import ch.jalu.configme.configurationdata.ConfigurationDataBuilder
import ch.jalu.configme.resource.YamlFileResource
import me.clip.voteparty.conf.migrations.VoteMigrationService
import me.clip.voteparty.conf.sections.CrateSettings
import me.clip.voteparty.conf.sections.EffectsSettings
import me.clip.voteparty.conf.sections.HookSettings
import me.clip.voteparty.conf.sections.PartySettings
import me.clip.voteparty.conf.sections.PluginSettings
import me.clip.voteparty.conf.sections.StorageSettings
import me.clip.voteparty.conf.sections.VoteSettings
import java.io.File

internal class VotePartyConfiguration(file: File) : SettingsManagerImpl(YamlFileResource(file.toPath()), ConfigurationDataBuilder.createConfiguration(SECTIONS), VoteMigrationService())
{

	private companion object
	{

		private val SECTIONS = listOf(
			PluginSettings::class.java,
			StorageSettings::class.java,
			HookSettings::class.java,
			CrateSettings::class.java,
			EffectsSettings::class.java,
			PartySettings::class.java,
			VoteSettings::class.java
		                             )

	}

}
