package me.clip.voteparty.data.impl

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.clip.voteparty.data.base.VotedForPartyCache
import me.clip.voteparty.plugin.VotePartyPlugin
import java.io.File
import java.util.UUID
import java.util.logging.Level

internal class VotedForPartyCacheGson(override val plugin: VotePartyPlugin) : VotedForPartyCache
{

    private lateinit var gson: Gson
    private lateinit var file: File
    private val type = object : TypeToken<MutableList<UUID>>() {}.type

    override fun load() {
        val builder = GsonBuilder().setPrettyPrinting()
        gson = builder.create()

        file = plugin.dataFolder.resolve("voted-for-party-cache.json")

        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }

        try
        {
            party.partyHandler.voted = gson.fromJson(file.readText(), type)
        }
        catch (ex: Exception)
        {
            party.partyHandler.voted = mutableListOf()
        }
    }

    override fun kill() {
        save()
    }

    override fun save() {
        try
        {
            file.writeText(gson.toJson(party.partyHandler.voted, type))
        }
        catch (ex: Exception)
        {
            logger.log(Level.SEVERE, "failed to save voted for party cache.", ex)
        }
    }
}
