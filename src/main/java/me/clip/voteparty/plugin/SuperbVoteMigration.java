package me.clip.voteparty.plugin;

import io.minimum.minecraft.superbvote.SuperbVote;
import io.minimum.minecraft.superbvote.util.PlayerVotes;
import me.clip.voteparty.VoteParty;
import me.clip.voteparty.user.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;
import java.util.stream.Collectors;

public class SuperbVoteMigration {
    private final VoteParty voteParty;
    private int users = 0;
    private int votes = 0;

    public SuperbVoteMigration(VoteParty voteParty) {
        this.voteParty = voteParty;
    }

    public void convert() {
        final SuperbVote superbVote = (SuperbVote) Bukkit.getPluginManager().getPlugin("SuperbVote");
        for (final OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            final PlayerVotes playerVotes = superbVote.getVoteStorage().getVotes(offlinePlayer.getUniqueId());
            if (playerVotes.getVotes() == 0) {
                continue;
            }
            final User user = voteParty.getUsersHandler().get(playerVotes.getUuid());
            user.setName(playerVotes.getAssociatedUsername());
            for (int i = 0; i < playerVotes.getVotes(); i++) {
                user.voted();
                votes++;
            }
            users++;
        }
    }

    public int getUsers() {
        return users;
    }

    public int getVotes() {
        return votes;
    }
}
