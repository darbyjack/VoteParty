package me.clip.voteparty.data.impl

import co.aikar.idb.DB
import co.aikar.idb.Database
import co.aikar.idb.DatabaseOptions
import co.aikar.idb.PooledDatabaseOptions
import me.clip.voteparty.conf.sections.StorageSettings
import me.clip.voteparty.data.base.DatabaseVotePlayer
import me.clip.voteparty.plugin.VotePartyPlugin
import me.clip.voteparty.user.User
import java.math.BigInteger
import java.sql.SQLException
import java.util.UUID
import java.util.logging.Level

internal class DatabaseVotePlayerMySQL(override val plugin: VotePartyPlugin) : DatabaseVotePlayer
{

    private lateinit var database: Database

    override fun load()
    {
        val sql = party.conf().getProperty(StorageSettings.SQL)

        val options = DatabaseOptions.builder()
            .mysql(sql.user, sql.password, sql.database, sql.host)
            .logger(plugin.logger)
            .poolName("VoteParty Database")
            .build()

        database = PooledDatabaseOptions.builder().options(options).createHikariDatabase()

        DB.setGlobalDatabase(database)

        try
        {
            database.executeUpdate(
                "CREATE TABLE IF NOT EXISTS `voteparty_players` (" +
                        "  `uuid` varchar(255) NOT NULL," +
                        "  `name` varchar(100) NOT NULL," +
                        "  `claimable` int(9) unsigned NOT NULL," +
                        "  PRIMARY KEY (`uuid`)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8;"
            )

            database.executeUpdate(
                "CREATE TABLE IF NOT EXISTS `voteparty_votes` (" +
                        "  `timestamp` bigint(20) unsigned NOT NULL," +
                        "  `uuid` varchar(255) NOT NULL," +
                        "  PRIMARY KEY (`timestamp`)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8;"
            )

        }
        catch (ex: SQLException)
        {
            throw RuntimeException("Error while creating database table:", ex)
        }
    }

    override fun kill()
    {
        DB.close()
    }

    override fun load(uuid: UUID): User?
    {
        return try
        {
            val row = database.getFirstRow("SELECT name, claimable FROM voteparty_players WHERE uuid = ?", uuid.toString())
            val user = User(uuid, row.getString("name"), mutableListOf(), row.getInt("claimable"))

            database.getResults("SELECT timestamp FROM voteparty_votes WHERE uuid = ?", uuid.toString()).forEach { vote ->
                val stamp : BigInteger = vote.get("timestamp")
                user.voted(stamp.toLong())
            }
            user
        }
        catch (ex: SQLException)
        {
            logger.log(Level.SEVERE, "failed to load player:$uuid", ex)
            null
        }
    }

    override fun save(data: User)
    {
        database.executeInsert("INSERT INTO voteparty_players (uuid, name, claimable) VALUES(?, ?, ?) ON DUPLICATE " +
                "KEY UPDATE name = ?, claimable = ?", data.uuid.toString(), data.name, data.claimable, data.name, data.claimable)

        data.votes().forEach { vote ->
            database.executeInsert("INSERT IGNORE INTO voteparty_votes (timestamp, uuid) VALUES(?, ?)", vote, data.uuid.toString())
        }
    }

    override fun reset(data: User)
    {
        database.executeUpdate("DELETE FROM voteparty_votes WHERE UUID = ?", data.uuid.toString())
    }

    override fun load(uuid: Collection<UUID>): Map<UUID, User?>
    {
        if (uuid.isNotEmpty())
        {
            return super.load(uuid)
        }

        val data = mutableListOf<UUID>()

        database.getResults("SELECT uuid from voteparty_players").forEach { results ->
            data.add(UUID.fromString(results.getString("uuid")))
        }

        return data.associateWith(::load)
    }
}
