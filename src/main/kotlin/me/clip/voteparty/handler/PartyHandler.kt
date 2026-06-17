package me.clip.voteparty.handler

import java.time.LocalDateTime
import java.time.ZoneId
import me.clip.voteparty.base.Addon
import me.clip.voteparty.conf.sections.CrateSettings
import me.clip.voteparty.conf.sections.EffectsSettings
import me.clip.voteparty.conf.sections.PartySettings
import me.clip.voteparty.events.PartyEndEvent
import me.clip.voteparty.events.PartyStartEvent
import me.clip.voteparty.events.PrePartyEvent
import me.clip.voteparty.exte.color
import me.clip.voteparty.exte.formMessage
import me.clip.voteparty.exte.meta
import me.clip.voteparty.exte.name
import me.clip.voteparty.exte.runTaskLater
import me.clip.voteparty.exte.runTaskTimer
import me.clip.voteparty.exte.sendMessage
import me.clip.voteparty.exte.takeRandomly
import me.clip.voteparty.messages.Messages
import me.clip.voteparty.plugin.VotePartyPlugin
import me.clip.voteparty.version.EffectType
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.TimeUnit

class PartyHandler(override val plugin: VotePartyPlugin) : Addon
{

	var voted = mutableListOf<UUID>()
	private var parties = mutableListOf<Long>()

	fun getParties(): MutableList<Long> {
		return parties
	}

	fun setParties(new: MutableList<Long>)
	{
		parties = new
	}

	fun addParties(vararg new: Long)
	{
		new.forEach { parties.add(it) }
	}

	fun getPartiesSince(time: LocalDateTime): Int
	{
		val timeEpoch = time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

		return parties.count { partyEpoch -> partyEpoch > timeEpoch }
	}

	fun giveRandomPartyRewards(player: Player)
	{
		if (player.world.name in party.conf().getProperty(PartySettings.DISABLED_WORLDS)) {
			return
		}

		val settings = party.conf().getProperty(PartySettings.REWARD_COMMANDS)

		if (!settings.enabled || settings.max_possible <= 0 || settings.commands.isEmpty())
		{
			return
		}

		if (player.world.name in party.conf().getProperty(PartySettings.DISABLED_WORLDS)) {
			return
		}

		val iter = settings.commands.takeRandomly(settings.max_possible).iterator()

		plugin.runTaskTimer(party.conf().getProperty(PartySettings.COMMAND_DELAY).toLong() * 20L)
		{
			if (!iter.hasNext())
			{
				return@runTaskTimer cancel()
			}

			runPartyCommandEffects(player)
			iter.next().command.forEach()
			{ command ->
				server.dispatchCommand(server.consoleSender, formMessage (player, command))
			}
		}
	}

	fun giveGuaranteedPartyRewards(player: Player)
	{
		val settings = party.conf().getProperty(PartySettings.GUARANTEED_REWARDS)

		executeCommands(settings.enabled, settings.commands, player)
	}

	fun givePermissionPartyRewards(player: Player)
	{
		if (player.world.name in party.conf().getProperty(PartySettings.DISABLED_WORLDS)) {
			return
		}

		val settings = party.conf().getProperty(PartySettings.PERMISSION_PARTY_REWARDS)

		if (!settings.enabled || settings.permCommands.isEmpty())
		{
			return
		}

		if (player.world.name in party.conf().getProperty(PartySettings.DISABLED_WORLDS)) {
			return
		}

		settings.permCommands.filter { player.hasPermission(it.permission) }.forEach()
		{ perm ->
			perm.commands.forEach()
			{ command ->
				server.dispatchCommand(server.consoleSender, formMessage(player, command))
			}

		}
	}

	fun runAll(player: Player)
	{
		giveRandomPartyRewards(player)
		giveGuaranteedPartyRewards(player)
		givePermissionPartyRewards(player)
	}

	fun runPrePartyCommands()
	{
		val settings = party.conf().getProperty(PartySettings.PRE_PARTY_COMMANDS)

		executeCommands(settings.enabled, settings.commands)
	}

	fun runPartyCommands()
	{
		val settings = party.conf().getProperty(PartySettings.PARTY_COMMANDS)

		executeCommands(settings.enabled, settings.commands)
	}

	fun runPostPartyCommands()
	{
		val settings = party.conf().getProperty(PartySettings.POST_PARTY_COMMANDS)

		executeCommands(settings.enabled, settings.commands)
	}

	fun runPartyStartEffects()
	{
		val settings = party.conf().getProperty(EffectsSettings.PARTY_START)

		executeEffects(settings.enable, settings.effects, server.onlinePlayers,
		               settings.offsetX, settings.offsetY, settings.offsetZ,
		               settings.speed, settings.count)
	}

	fun runPartyCommandEffects(player: Player)
	{
		val settings = party.conf().getProperty(EffectsSettings.PARTY_COMMAND_EXECUTE)

		executeEffects(settings.enable, settings.effects, listOf(player),
		               settings.offsetX, settings.offsetY, settings.offsetZ,
		               settings.speed, settings.count)
	}


	fun buildCrate(amount: Int): ItemStack
	{
		val item = party.conf().getProperty(CrateSettings.MATERIAL).parseItem() ?: ItemStack(Material.CHEST, 1)
		item.amount = amount

		return item.meta()
		{
			name = party.conf().getProperty(CrateSettings.NAME) ?: "Vote Party Crate"
			lore = party.conf().getProperty(CrateSettings.LORE).map(::color)
		}
	}

	fun startParty()
	{

		val prePartyEvent = PrePartyEvent()
		Bukkit.getPluginManager().callEvent(prePartyEvent)

		if (prePartyEvent.isCancelled) {
			return
		}

		runPrePartyCommands()

		plugin.runTaskLater(party.conf().getProperty(PartySettings.START_DELAY) * 20L)
		{

			val partyStartEvent = PartyStartEvent()
			Bukkit.getPluginManager().callEvent(partyStartEvent)

			if (partyStartEvent.isCancelled) {
				return@runTaskLater
			}

			addParties(System.currentTimeMillis())
			runPartyCommands()
			runPartyStartEffects()

			val targets: Collection<Player> = when(party.conf().getProperty(PartySettings.PARTY_MODE)) {
				"everyone" -> server.onlinePlayers
				"daily" -> {
					val playersWhoVotedSince = party.usersHandler.getPlayersWhoVotedSince(1, TimeUnit.DAYS)

					server.onlinePlayers.filter { playersWhoVotedSince.contains(it.uniqueId) }
				}
				"party" -> server.onlinePlayers.filter { voted.contains(it.uniqueId) }
				else -> server.onlinePlayers
			}

			server.onlinePlayers.filterNot { it in targets }.forEach {
				sendMessage(party.manager().getCommandIssuer(it), Messages.PARTY__REQUIREMENTS_NOT_MET)
			}

			if (party.conf().getProperty(PartySettings.USE_CRATE)) {
				val disabledWorlds = party.conf().getProperty(PartySettings.DISABLED_WORLDS)
				targets.filterNot { it.world.name in disabledWorlds  }.forEach {
					it.inventory.addItem(buildCrate(1))
				}
			}
			else {
				targets.forEach()
				{
					giveGuaranteedPartyRewards(it)
					giveRandomPartyRewards(it)
					givePermissionPartyRewards(it)
				}
			}

			voted.clear()
			val partyEndEvent = PartyEndEvent()
			Bukkit.getPluginManager().callEvent(partyEndEvent)

			runPostPartyCommands()
		}
	}


	private fun executeCommands(enabled: Boolean, cmds: Collection<String>?, player: Player? = null)
	{
		if (!enabled || (player != null && (player.world.name in party.conf().getProperty(PartySettings.DISABLED_WORLDS))))
		{
			return
		}

		cmds?.forEach()
		{
			server.dispatchCommand(server.consoleSender, if (player == null) it else formMessage(player, it))
		}
	}


	private fun executeEffects(enabled: Boolean, effects: Collection<String>, players: Collection<Player>, offsetX: Double, offsetY: Double, offsetZ: Double, speed: Double, count: Int)
	{
		if (!enabled)
		{
			return
		}

		val targets = players.filter()
		{
			it.world.name !in party.conf().getProperty(PartySettings.DISABLED_WORLDS)
		}

		effects.forEach()
		{ effect ->
			targets.forEach()
			{ player ->
				party.hook().display(EffectType.valueOf(effect), player.location, offsetX, offsetY, offsetZ, speed, count)
			}
		}
	}

}
