package me.clip.voteparty.handler

import me.clip.voteparty.VoteParty
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class VoteHandler(private val plugin: VoteParty) {

    val conf = plugin.conf()

    fun giveGuarenteedVoteRewards(player: Player) {
        if (conf.voting?.guaranteedRewards?.enabled == false) {
            return
        }
        val cmds = conf.voting?.guaranteedRewards?.commands ?: return
        cmds.forEach {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), it.replace("{player}", player.name))
        }
    }
}