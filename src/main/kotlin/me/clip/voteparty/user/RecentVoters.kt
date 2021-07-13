package me.clip.voteparty.user

import org.bukkit.Bukkit
import java.util.*
import java.util.logging.Level

data class RecentVoters(private val recentSize: Int, val recentVoters: MutableMap<UUID, UserTime>) {
    fun voters(): Map<UUID, UserTime> {
        return recentVoters
    }

    fun voted(user: User, time: Long) {
        val userTime = recentVoters[user.uuid]

        if (userTime != null) {
            // It exists, remove it and add the new element
            val output = recentVoters.remove(user.uuid)

            recentVoters[user.uuid] = UserTime(user, time)
            return
        }


        if (recentVoters.count() < recentSize) { // If there's still space in the map
            // Just add the user to the map
            recentVoters[user.uuid] = UserTime(user, time)
            return
        } else {
            // Otherwise, remove the oldest user and replace with the new user
            var oldTime: Long? = null
            var oldUser: User? = null
            var oldUUID: UUID? = null

            // Find the oldest user-time pair
            for ((u, t) in recentVoters) {
                if (oldTime == null) {
                    oldUUID = u
                    oldTime = t.time
                    oldUser = t.user
                } else {
                    if (t.time < oldTime) {
                        oldTime = t.time;
                        oldUser = t.user;
                    }
                }
            }

            if (oldUser != null) {
                recentVoters.remove(oldUUID);
            }
            recentVoters[user.uuid] = UserTime(user, time)
        }
    }

    fun size(): Int {
        return recentSize
    }
}