package me.clip.voteparty.plugin;

import me.clip.voteparty.VoteParty;
import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

public final class VotePartyPlugin extends JavaPlugin {

    private static final List<Library> LIBRARIES = new ArrayList<>();

    static {
        // kotlin
        LIBRARIES.add(Library.builder()
                .groupId("org.jetbrains.kotlin")
                .artifactId("kotlin-stdlib")
                .version("1.3.61")
                .build());

        LIBRARIES.add(Library.builder()
                .groupId("org.jetbrains.kotlin")
                .artifactId("kotlin-stdlib-jdk8")
                .version("1.3.61")
                .build());

        LIBRARIES.add(Library.builder()
                .groupId("org.jetbrains.kotlin")
                .artifactId("kotlin-stdlib-jdk7")
                .version("1.3.61")
                .build());

        LIBRARIES.add(Library.builder()
                .groupId("org.jetbrains.kotlin")
                .artifactId("kotlin-stdlib-common")
                .version("1.3.61")
                .build());

        LIBRARIES.add(Library.builder()
                .groupId("org.jetbrains.kotlin")
                .artifactId("kotlin-reflect")
                .version("1.3.61")
                .build());

        // korn fields
        LIBRARIES.add(Library.builder()
                .groupId("com.sxtanna.korm")
                .artifactId("Korm")
                .version("1.9")
                .build());


        // update checker dependency
        LIBRARIES.add(Library.builder()
                .groupId("com.konghq")
                .artifactId("unirest-java")
                .version("3.3.00")
                .classifier("standalone")
                .build());
    }

    private final AtomicReference<VoteParty> reference = new AtomicReference<>();

    @Override
    public void onLoad() {
        try {
            final BukkitLibraryManager library = new BukkitLibraryManager(this);
            library.addMavenCentral();
            library.addRepository("https://repo.glaremasters.me/repository/public/");

            LIBRARIES.forEach(library::loadLibrary);
        } catch (final Exception ex) {
            getLogger().log(Level.SEVERE, "Failed to load runtime libraries: ", ex);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onEnable() {
        final VoteParty voteParty = new VoteParty(this);
        voteParty.load();

        reference.set(voteParty);
    }

    @Override
    public void onDisable() {
        final VoteParty voteParty = reference.getAndSet(null);
        if (voteParty == null) {
            return;
        }

        voteParty.kill();
    }

    @Nullable
    public VoteParty getVoteParty() {
        return reference.get();
    }

}
