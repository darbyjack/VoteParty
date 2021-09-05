package me.clip.voteparty.conf.sections

import ch.jalu.configme.Comment
import ch.jalu.configme.SettingsHolder
import ch.jalu.configme.properties.Property
import ch.jalu.configme.properties.PropertyInitializer.newBeanProperty
import ch.jalu.configme.properties.PropertyInitializer.newProperty
import me.clip.voteparty.conf.objects.SQLData

internal object StorageSettings : SettingsHolder {

    @JvmField
    @Comment("What storage backend would you like to use? We currently support JSON & MySQL")
    val BACKEND: Property<String> = newProperty("storage.backend", "JSON")

    @JvmField
    val SQL : Property<SQLData> = newBeanProperty(SQLData::class.java, "storage.mysql_settings", SQLData("user", "pass", "database", "localhost"))
}
