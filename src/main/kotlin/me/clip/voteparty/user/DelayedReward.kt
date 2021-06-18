package me.clip.voteparty.user

/*
Types:
0 = daily
1 = weekly
2 = monthly
3 = yearly
4 = total
 */
data class DelayedReward(var type: Int, var votes: Int)
