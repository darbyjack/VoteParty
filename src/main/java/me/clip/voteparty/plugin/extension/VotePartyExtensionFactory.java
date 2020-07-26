package me.clip.voteparty.plugin.extension;

import com.djrapitops.plan.extension.DataExtension;
import me.clip.voteparty.VoteParty;

import java.util.Optional;

public class VotePartyExtensionFactory
{
	private final VoteParty voteParty;

	public VotePartyExtensionFactory(final VoteParty voteParty)
	{
		this.voteParty = voteParty;
	}

	private boolean isAvailable()
	{
		try
		{
			Class.forName("me.clip.voteparty.VoteParty");
			return true;
		}
		catch (ClassNotFoundException e)
		{
			return false;
		}
	}

	public Optional<DataExtension> createExtension()
	{
		if (isAvailable())
		{
			return Optional.of(new VotePartyExtension(voteParty));
		}
		return Optional.empty();
	}

}
