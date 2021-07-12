package me.clip.voteparty.user

import org.bukkit.Bukkit
import java.util.logging.Level

data class RecentVoters(private val recentSize: Int, val recentVoters: MutableMap<User, Long>) {
    fun voters(): Map<User, Long> {
        return recentVoters
    }

    fun voted(user: User, time: Long) {
        var removeUser: User? = null
        for ((userList, _) in recentVoters) {
            Bukkit.getLogger().log(Level.WARNING, "Loop")
            if (userList.uuid.toString() == user.uuid.toString()) {
                Bukkit.getLogger().log(Level.WARNING, "Equal")
                // If they are, replace the old time with the new time
                removeUser = userList

                recentVoters[user] = time
            }
        }

        // TODO: WHY WHY WHY IS THIS NOT REMOVING!!!!!!!!!!!!!!!!!!!!!!!!
        if (removeUser != null) {
            val output = recentVoters.remove(removeUser)
            Bukkit.getLogger().log(Level.WARNING, "" + output)
        }


        if (recentVoters.count() < recentSize) { // If there's still space in the map
            // Just add the user to the map
            recentVoters[user] = time
            return
        } else {
            // Otherwise, remove the oldest user and replace with the new user
            var oldTime: Long? = null;
            var oldUser: User? = null;

            // Find the oldest user-time pair
            for ((u, t) in recentVoters) {
                if (oldTime == null) {
                    oldTime = t;
                    oldUser = u;
                } else {
                    if (t < oldTime) {
                        oldTime = t;
                        oldUser = u;
                    }
                }
            }

            if (oldUser != null) {
                recentVoters.remove(oldUser);
            }
            recentVoters[user] = time
        }
    }

    fun size(): Int {
        return recentSize
    }
}