package me.clip.voteparty.voteplayer

import java.util.UUID

data class User(val uuid: UUID, var name: String, val data: MutableList<Long>)
