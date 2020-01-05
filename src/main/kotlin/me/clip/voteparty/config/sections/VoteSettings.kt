package me.clip.voteparty.config.sections

import ch.jalu.configme.SettingsHolder
import ch.jalu.configme.properties.Property
import ch.jalu.configme.properties.PropertyInitializer.newBeanProperty
import me.clip.voteparty.config.objects.Command
import me.clip.voteparty.config.objects.Commands
import me.clip.voteparty.config.objects.RewardsPerEvent

object VoteSettings : SettingsHolder
{
	@JvmField
	val PER_VOTE_REWARDS: Property<RewardsPerEvent> = newBeanProperty(RewardsPerEvent::class.java, "voting.per_vote_rewards", RewardsPerEvent(true, 1, listOf(Command(50, "eco give %player_name% 100"), Command(70, "give %player_name% STEAK 10"))))
	
	@JvmField
	val GUARANTEED_REWARDS: Property<Commands> = newBeanProperty(Commands::class.java, "voting.guaranteed_rewards", Commands(true, listOf("eco give %player_name% 10", "give %player_name% STEAK 8")))
	
	@JvmField
	val GLOBAL_COMMANDS: Property<Commands> = newBeanProperty(Commands::class.java, "voting.global_commands", Commands(true, listOf("broadcast Only %voteparty_votes_required_party% more votes until a VoteParty!")))
}