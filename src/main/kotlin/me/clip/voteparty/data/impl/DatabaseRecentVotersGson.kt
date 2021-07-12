package me.clip.voteparty.data.impl

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import me.clip.voteparty.data.base.DatabaseRecentVoters
import me.clip.voteparty.plugin.VotePartyPlugin
import me.clip.voteparty.user.RecentVoters
import java.util.logging.Level

internal class DatabaseRecentVotersGson(override val plugin: VotePartyPlugin) : DatabaseRecentVoters{

    private lateinit var gson: Gson

    override fun load() {
        val builder = GsonBuilder().disableHtmlEscaping().enableComplexMapKeySerialization().setPrettyPrinting().serializeNulls()
        gson = builder.create()
    }

    override fun kill() {
    }

    override fun loadVoters(): RecentVoters? {
        return try
        {
            gson.fromJson(plugin.dataFolder.resolve("recents.json").readText(), RecentVoters::class.java)
        }
        catch (ex: Exception)
        {
            logger.log(Level.WARNING, "failed to load recent votes", ex)
            null
        }
    }


    override fun save(data: RecentVoters) {
        try
        {
            plugin.dataFolder.resolve("recents.json").writeText(gson.toJson(data, RecentVoters::class.java))
        }
        catch (ex: Exception)
        {
            logger.log(Level.SEVERE, "failed to save recent votes", ex)
        }
    }

}