package me.clip.voteparty.user

import java.util.UUID

data class User(val uuid: UUID, var name: String, val data: MutableList<Long>, var claimable: Int)
{
    fun inc()
    {
        claimable = claimable.plus(1)
    }

    fun dec()
    {
        claimable = claimable.minus(1)
    }
}
