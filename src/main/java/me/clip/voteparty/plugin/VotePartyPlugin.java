package me.clip.voteparty.plugin;

import me.clip.voteparty.VoteParty;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public final class VotePartyPlugin extends JavaPlugin
{
	private VoteParty voteParty;

	@Override
	public void onEnable()
	{
		this.voteParty = new VoteParty(this);
		this.voteParty.load();

		new Metrics(this, 17932);

		getServer().getServicesManager().register(VoteParty.class, this.voteParty, this, ServicePriority.Normal);
	}

	@Override
	public void onDisable()
	{
		if (this.voteParty != null)
		{
			this.voteParty.kill();
		}

		this.voteParty = null;

		getServer().getServicesManager().unregisterAll(this);
	}


	@Nullable
	public VoteParty getVoteParty()
	{
		return this.voteParty;
	}

}

