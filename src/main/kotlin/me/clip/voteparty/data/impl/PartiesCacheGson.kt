package me.clip.voteparty.data.impl

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.logging.Level
import me.clip.voteparty.data.base.PartiesCache
import me.clip.voteparty.plugin.VotePartyPlugin

class PartiesCacheGson(override val plugin: VotePartyPlugin) : PartiesCache {

    private lateinit var gson: Gson
    private lateinit var file: File
    private lateinit var backupFile: File
    private val type = object : TypeToken<MutableList<Long>>() {}.type

    override fun load()
    {
        val builder = GsonBuilder().setPrettyPrinting()
        gson = builder.create()

        file = plugin.dataFolder.resolve("parties.json")
        backupFile = plugin.dataFolder.resolve("parties.json.bak")

        if (!file.exists())
        {
            file.parentFile.mkdirs()
            file.createNewFile()
        }

        try
        {
            if (file.length() == 0L)
            {
                party.partyHandler.setParties(mutableListOf())
            }
            else
            {
                party.partyHandler.setParties(gson.fromJson(file.readText(), type))
            }
        }
        catch (exception: Exception)
        {
            logger.log(
                Level.SEVERE,
                "failed to load voted for party cache. This can end up in data loss!" + System.lineSeparator() +
                        "A backup of the ${file.name} file was saved to: ${backupFile.name}",
                exception
            )

            if (!backupFile.exists())
            {
                backupFile.createNewFile()
            }

            backupFile.writeText(file.readText())
        }
    }

    override fun kill()
    {
        save()
    }

    override fun save()
    {
        try
        {
            file.writeText(gson.toJson(party.partyHandler.getParties(), type))
        }
        catch (exception: Exception)
        {
            logger.log(
                Level.SEVERE,
                "failed to save parties cache. This can end up in data loss!" + System.lineSeparator() +
                        "For backup purposes, the cached data is: " + party.partyHandler.getParties().joinToString(", "),
                exception
            )
        }
    }
}
