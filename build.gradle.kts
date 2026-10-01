import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import me.drownek.plugwright.local.LocalMode
import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import xyz.jpenilla.runpaper.task.RunServer

plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.shadow) apply false
	alias(libs.plugins.versions)
	alias(libs.plugins.run.paper)
	id("io.github.drownek.plugwright") version "3.0.0"
}

val javaToolchainsService = extensions.getByType<JavaToolchainService>()

allprojects {
	apply(plugin = "org.jetbrains.kotlin.jvm")

	base {
		archivesName.set("VoteParty")
	}

	group = "me.clip"
	version = "2.41-SNAPSHOT"

	repositories {
		mavenCentral()

		maven("https://oss.sonatype.org/content/repositories/snapshots/")
		maven("https://repo.aikar.co/content/groups/aikar/")
		maven("https://repo.papermc.io/repository/maven-public/")
		maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
		maven("https://repo.glaremasters.me/repository/public/")

        // Temporary source-build dependency for unreleased XSeries commits.
        maven("https://jitpack.io")
	}

	plugins.withId("java") {
		extensions.configure<JavaPluginExtension> {
			sourceCompatibility = JavaVersion.VERSION_1_8
			targetCompatibility = JavaVersion.VERSION_1_8
		}
	}

	tasks.withType<JavaCompile>().configureEach {
		options.compilerArgs.add("-parameters")
		sourceCompatibility = JavaVersion.VERSION_1_8.toString()
		targetCompatibility = JavaVersion.VERSION_1_8.toString()

		if (JavaVersion.current().isJava9Compatible) {
			options.release.set(8)
		}
	}

	tasks.withType<KotlinCompile>().configureEach {
		compilerOptions {
			jvmTarget.set(JvmTarget.JVM_1_8)
			javaParameters.set(true)
		}
	}
}

apply(plugin = "com.gradleup.shadow")

val shadowJarTask = tasks.named<ShadowJar>("shadowJar")

tasks.named<ShadowJar>("shadowJar") {
	minimize {
		exclude(project(":particle-api"))
		exclude(project(":particle-legacy"))
		exclude(project(":particle-modern"))

		exclude(dependency("co.aikar:.*:.*"))
		exclude(dependency("ch.jalu:.*:.*"))
		exclude(dependency("org.inventivetalent:.*:.*"))
		exclude(dependency("com.github.cryptomorin:.*:.*"))
		exclude(dependency("net.kyori:.*:.*"))
	}

	relocate("co.aikar.commands", "me.clip.voteparty.libs.acf")
	relocate("co.aikar.locales", "me.clip.voteparty.libs.locales")
	relocate("ch.jalu.configme", "me.clip.voteparty.libs.configme")
	relocate("org.inventivetalent", "me.clip.voteparty.libs.inventivetalent")
	relocate("net.kyori", "me.clip.voteparty.libs.kyori")
	relocate("com.cryptomorin.xseries", "me.clip.voteparty.libs.xseries")
	relocate("kotlin", "me.clip.voteparty.libs.kotlin")
	relocate("org.bstats", "me.clip.voteparty.libs.bstats")

	archiveFileName.set("VoteParty-${project.version}.jar")
}

val plugwrightModernVersion: String =
	providers.environmentVariable("PLUGWRIGHT_MODERN_MC_VERSION").getOrElse("1.21.11")
val plugwrightLatestVersion: String =
	providers.environmentVariable("PLUGWRIGHT_LATEST_MC_VERSION").getOrElse("26.1.2")

plugwright {
	testsDir.set(file("src/test/e2e"))
	primaryEnvironment.set("modern")

	// Mineflayer has protocol data for 1.21.11 and 26.1.2 but none for 26.2 and later, so the
	// newest versions stay on the run-paper tasks above instead of this suite.
	environments {
		create("modern", LocalMode) {
			minecraftVersion.set(plugwrightModernVersion)
			acceptEula.set(true)
			jvmArgs.set(listOf("-Xms1G", "-Xmx2G"))

			// EssentialsX is not optional here. It overrides /give with its own item database,
			// which is what maps the upper case legacy item names the shipped reward config uses
			// (STEAK, GOLDEN_APPLE, DIAMOND, IRON_INGOT), and it supplies /broadcast, which the
			// shipped global_commands entry uses. On a server without it those commands fail.
			downloadPlugins {
				url("https://ci.helpch.at/view/Plugins/job/PlaceholderAPI/266/artifact/build/libs/PlaceholderAPI-2.12.3-DEV-266.jar")
				url("https://cdn.modrinth.com/data/hXiIvTyT/versions/nY6VN1XH/EssentialsX-2.22.0.jar")
			}

			writeFiles {
				file("plugins/VoteParty/config.yml", projectDir.resolve("src/test/e2e/fixtures/voteparty-config.yml"))
				file("server.properties", """
					level-type=minecraft\:flat
					generate-structures=false
					spawn-npcs=false
					spawn-animals=false
					spawn-monsters=false
					view-distance=4
					simulation-distance=4
				""".trimIndent())
			}
		}

		create("latest", LocalMode) {
			minecraftVersion.set(plugwrightLatestVersion)
			acceptEula.set(true)
			jvmArgs.set(listOf("-Xms1G", "-Xmx2G"))

			// EssentialsX is not optional here. It overrides /give with its own item database,
			// which is what maps the upper case legacy item names the shipped reward config uses
			// (STEAK, GOLDEN_APPLE, DIAMOND, IRON_INGOT), and it supplies /broadcast, which the
			// shipped global_commands entry uses. On a server without it those commands fail.
			downloadPlugins {
				url("https://ci.helpch.at/view/Plugins/job/PlaceholderAPI/266/artifact/build/libs/PlaceholderAPI-2.12.3-DEV-266.jar")
				url("https://cdn.modrinth.com/data/hXiIvTyT/versions/nY6VN1XH/EssentialsX-2.22.0.jar")
			}

			writeFiles {
				file("plugins/VoteParty/config.yml", projectDir.resolve("src/test/e2e/fixtures/voteparty-config.yml"))
				file("server.properties", """
					level-type=minecraft\:flat
					generate-structures=false
					spawn-npcs=false
					spawn-animals=false
					spawn-monsters=false
					view-distance=4
					simulation-distance=4
				""".trimIndent())
			}
		}
	}
}

// The Plugwright tasks are not yet configuration cache compatible, so a graph that contains
// them has to be allowed to skip the cache instead of failing to store an entry.
tasks.configureEach {
	if (name.startsWith("plugwright")) {
		notCompatibleWithConfigurationCache("Plugwright tasks do not support the configuration cache yet")
	}
}

fun RunServer.configureVotePartyRun(
	minecraftVersion: String,
	runDirectoryName: String,
	javaVersion: Int,
	descriptionText: String,
) {
	group = "verification"
	description = descriptionText

	minecraftVersion(minecraftVersion)
	runDirectory.set(layout.projectDirectory.dir("run/$runDirectoryName"))

	javaLauncher.set(
		javaToolchainsService.launcherFor {
			languageVersion.set(JavaLanguageVersion.of(javaVersion))
		}
	)

	dependsOn(shadowJarTask)
	pluginJars(shadowJarTask.flatMap { it.archiveFile })

	downloadPlugins {
		url("https://ci.helpch.at/view/Plugins/job/PlaceholderAPI/266/artifact/build/libs/PlaceholderAPI-2.12.3-DEV-266.jar")
	}
}

tasks {
	runServer {
		configureVotePartyRun(
			minecraftVersion = "1.20.6",
			runDirectoryName = "paper-1.20.6",
			javaVersion = 21,
			descriptionText = "Run a Paper 1.20.6 test server with the shaded VoteParty jar.",
		)
	}

	register<RunServer>("runPaper188") {
		configureVotePartyRun(
			minecraftVersion = "1.8.8",
			runDirectoryName = "paper-1.8.8",
			javaVersion = 8,
			descriptionText = "Run a Paper 1.8.8 test server with the shaded VoteParty jar.",
		)
	}

	register<RunServer>("runPaper1122") {
		configureVotePartyRun(
			minecraftVersion = "1.12.2",
			runDirectoryName = "paper-1.12.2",
			javaVersion = 8,
			descriptionText = "Run a Paper 1.12.2 test server with the shaded VoteParty jar.",
		)
	}

	register<RunServer>("runPaper1132") {
		configureVotePartyRun(
			minecraftVersion = "1.13.2",
			runDirectoryName = "paper-1.13.2",
			javaVersion = 8,
			descriptionText = "Run a Paper 1.13.2 test server with the shaded VoteParty jar.",
		)
	}

	register<RunServer>("runPaper1206") {
		configureVotePartyRun(
			minecraftVersion = "1.20.6",
			runDirectoryName = "paper-1.20.6",
			javaVersion = 21,
			descriptionText = "Run a Paper 1.20.6 test server with the shaded VoteParty jar.",
		)
	}

	register<RunServer>("runPaper2612") {
		configureVotePartyRun(
			minecraftVersion = "26.1.2",
			runDirectoryName = "paper-26.1.2",
			javaVersion = 25,
			descriptionText = "Run the latest stable Paper test server with the shaded VoteParty jar.",
		)
	}

	register<RunServer>("runPaperLatest") {
		configureVotePartyRun(
			minecraftVersion = "26.2",
			runDirectoryName = "paper-26.2",
			javaVersion = 25,
			descriptionText = "Run the latest stable Paper test server with the shaded VoteParty jar.",
		)
	}
}

dependencies {
	implementation(libs.kotlin.stdlib.jdk8)
	implementation(libs.xseries)

	compileOnly(libs.spigot.legacy)

	implementation(libs.configme)

	compileOnly(libs.placeholderapi)
	compileOnly(files("libs/nuvotifier-2.7.3.jar"))

	implementation(libs.acf.paper)
	implementation(libs.bstats.bukkit)

	implementation(libs.adventure.platform.bukkit)
	implementation(libs.adventure.api)
	implementation(libs.adventure.text.minimessage)

	implementation(project(":particle-api"))
	implementation(project(":particle-legacy"))
	implementation(project(":particle-modern"))
}

tasks.processResources {
	filter<ReplaceTokens>(
		"tokens" to mapOf(
			"version" to project.version.toString()
		)
	)
}