import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
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
		exclude(dependency("com.github.CryptoMorin:.*:.*"))
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