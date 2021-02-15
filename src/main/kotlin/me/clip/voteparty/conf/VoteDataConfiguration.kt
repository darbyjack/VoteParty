package me.clip.voteparty.conf

import ch.jalu.configme.SettingsManagerImpl
import ch.jalu.configme.configurationdata.ConfigurationDataBuilder
import ch.jalu.configme.migration.PlainMigrationService
import ch.jalu.configme.resource.YamlFileResource
import me.clip.voteparty.conf.sections.VoteData
import java.io.File

internal class VoteDataConfiguration(file: File) : SettingsManagerImpl(YamlFileResource(file.toPath()), ConfigurationDataBuilder.createConfiguration(SECTIONS), PlainMigrationService())
{
	private companion object
	{
		private val SECTIONS = listOf(
			VoteData::class.java
		                             )
	}
}