package me.clip.voteparty.plugin.extension;

import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.annotation.PluginInfo;
import com.djrapitops.plan.extension.annotation.TableProvider;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.table.Table;
import me.clip.voteparty.VoteParty;
import me.clip.voteparty.leaderboard.LeaderboardType;
import org.bukkit.Bukkit;

import java.util.UUID;

@PluginInfo(name = "VoteParty", iconName = "user-alt", iconFamily = Family.SOLID, color = Color.RED)
public class VotePartyExtension implements DataExtension
{
	private final VoteParty voteParty;

	public VotePartyExtension(final VoteParty voteParty)
	{
		this.voteParty = voteParty;
	}

	@TableProvider(tableColor = Color.DEEP_PURPLE)
	public Table voteTable(UUID uuid) {
		Table.Factory table = Table.builder()
				.columnOne("Vote Time", Icon.called("history").build())
				.columnTwo("Amount", Icon.called("plus").build());

		for (final LeaderboardType type : LeaderboardType.values()) {
			table.addRow(type.name(), voteParty.getUsersHandler().getVotesSince(Bukkit.getOfflinePlayer(uuid), type.getTime().invoke()));
		}
		return table.build();
	}

}
