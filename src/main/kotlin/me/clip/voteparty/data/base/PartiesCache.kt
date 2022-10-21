package me.clip.voteparty.data.base

import me.clip.voteparty.base.Addon
import me.clip.voteparty.base.State

internal interface PartiesCache : Addon, State {

    override fun load()

    override fun kill()

    fun save()
}
