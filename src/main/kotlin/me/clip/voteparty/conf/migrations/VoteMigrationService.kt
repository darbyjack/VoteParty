package me.clip.voteparty.conf.migrations

import ch.jalu.configme.configurationdata.ConfigurationData
import ch.jalu.configme.migration.PlainMigrationService
import ch.jalu.configme.resource.PropertyReader


internal class VoteMigrationService : PlainMigrationService()
{
	override fun performMigrations(reader: PropertyReader, configurationData: ConfigurationData): Boolean
	{
		return hasDeprecatedProperties(reader)
	}
	
	private fun hasDeprecatedProperties(reader: PropertyReader): Boolean
	{
		val deprecatedProperties = arrayOf("settings.counter.votes", "hooks.nuvotifier")
		for (deprecatedPath in deprecatedProperties)
		{
			if (reader.contains(deprecatedPath))
			{
				return true
			}
		}
		return false
	}
}