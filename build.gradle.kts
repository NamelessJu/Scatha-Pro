plugins {
    // This plugin applies the correct loom variant based on the Minecraft version
    id("dev.kikugie.loom-back-compat")
}

// DO NOT set group = ...!
version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = property("mod.id") as String

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    sc.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    sc.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

stonecutter {
    replacements.string(current.parsed <= "26.1.2") {
        // yeah I shouldn't have switched to TextColor constants yet but I'm stubborn
        // and want to keep using the objectively better option where possible
        replace("import net.minecraft.network.chat.*;", "import net.minecraft.ChatFormatting;\nimport net.minecraft.network.chat.*;")
        replace("import net.minecraft.network.chat.TextColor", "import net.minecraft.ChatFormatting")
        replace("EMPTY.withColor(TextColor", "EMPTY.withColor(ChatFormatting")
        replace("style.withColor(TextColor", "style.withColor(ChatFormatting")
        replace(".withColor(TextColor.", ".withStyle(ChatFormatting.")
        replace(".withColor(TextUtil.contrastableGray", ".withStyle(TextUtil.contrastableGray")
        replace(".withColor(contrastableGray", ".withStyle(contrastableGray")
        replace("TextColor", "ChatFormatting")

        replace("minecraft.gui.screen()", "minecraft.screen")
        replace("Minecraft.getInstance().gui.screen()", "Minecraft.getInstance().screen")
        replace("minecraft.gui.setScreen", "minecraft.setScreen")
        replace("Minecraft.getInstance().gui.setScreen", "Minecraft.getInstance().setScreen")

        replace(".gui.hud.", ".gui/*.hud*/.")
    }
    replacements.regex(current.parsed <= "26.1.2") {
        replace(
            "TextColor.(\\w+).getValue\\(\\)", "ChatFormatting.$1.getColor()",
            "ChatFormatting.(\\w+).getColor\\(\\)", "TextColor.$1.getValue()"
        )
    }
}

loom {
    // Separates client-only code into a dedicated `client` source set
    splitEnvironmentSourceSets()
    mods {
        register(property("mod.id") as String) {
            sourceSet(sourceSets.main.get())
            sourceSet(sourceSets.getByName("client"))
        }
    }
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json") // Useful for interface injection
    accessWidenerPath = sc.process(
        rootProject.file("src/main/resources/scathapro.classtweaker"),
        "build/scathapro.classtweaker"
    )

    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1") // Adds names to lambdas - useful for mixins
    }

    runConfigs.all {
        preferGradleTask = true
        generateRunConfig = true
        runDirectory = rootProject.file("run") // Shares the run directory between versions
        jvmArguments.add("-Dmixin.debug.export=true") // Exports transformed classes for debugging
    }

    runs {
        remove(getByName("server"))
    }

    log4jConfigs.from(rootDir.resolve("log4j.xml"))
}

repositories {
    /**
     * Restricts dependency search of the given [groups] to the [maven URL][url],
     * improving the setup speed.
     */
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1", "DevAuth", "me.djtheredstoner")
    strictMaven("https://maven.terraformersmc.com/", "Terraformers", "com.terraformersmc")
    strictMaven("https://repo.hypixel.net/repository/Hypixel/", "Hypixel", "net.hypixel")
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    // Applies Mojang Mappings on obfuscated versions
    loomx.applyMojangMappings()

    runtimeOnly("me.djtheredstoner:DevAuth-fabric:${property("deps.devauth")}")

    // Use `mod{dependency type}` even on 26.1+ - loom-back-compat converts them
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
    modImplementation("com.terraformersmc:modmenu:${property("deps.modmenu")}")
    modImplementation("net.hypixel:mod-api:${property("deps.hypixel_mod_api")}")
    var hypixelModApiBuildDependency = "maven.modrinth:hypixel-mod-api:${property("deps.hypixel_mod_api_build")}"
    modRuntimeOnly(hypixelModApiBuildDependency)
    include(hypixelModApiBuildDependency)
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

tasks {
    processResources {
        fun MutableMap<String, String>.register(key: String, property: String) {
            val value: String = sc.properties[property]
            inputs.property(key, value)
            set(key, value)
        }

        val props = buildMap {
            set("java", requiredJava.toString())
            register("version", "mod.version")
            register("minecraft", "mod.mc_compat")
            register("hypixel_mod_api", "deps.hypixel_mod_api")
        }

        filesMatching("fabric.mod.json") { expand(props) { escapeBackslash = true } }
    }

    withType<ProcessResources>().configureEach {
        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") {
            expand("java" to mixinJava) { escapeBackslash = true }
        }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies results to `build/libs/{mod version}/`"

        inputs.property("version", project.property("mod.version"))
        // loomx.mod(Sources)Jar returns the jar task for the applied loom variant
        from(loomx.modJar.flatMap { it.archiveFile }, loomx.modSourcesJar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    }
}