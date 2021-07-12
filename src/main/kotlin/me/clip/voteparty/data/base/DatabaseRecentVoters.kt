package me.clip.voteparty.data.base

import me.clip.voteparty.base.Addon
import me.clip.voteparty.base.State
import me.clip.voteparty.user.RecentVoters
import java.util.*

internal interface DatabaseRecentVoters : Addon, State {
    override fun load()

    override fun kill()


    fun loadVoters(): RecentVoters?

    fun save(data: RecentVoters)
}