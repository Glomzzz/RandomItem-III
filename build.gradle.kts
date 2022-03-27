plugins {
    `java-library`
    id("io.izzel.taboolib") version "1.34"
    id("org.jetbrains.kotlin.jvm") version "1.6.10"
    id("org.jetbrains.dokka") version "1.6.10"
}



tasks.dokkaJavadoc.configure {
    val dokkaPath = projectDir.absolutePath.replace(rootDir.absolutePath, "")
    outputDirectory.set(File(rootDir.absolutePath + File.separator + "dokka" + dokkaPath))
    dokkaSourceSets {
        named("main") {
            noJdkLink.set(true)
            noStdlibLink.set(true)
            noAndroidSdkLink.set(true)
            suppressInheritedMembers.set(true)
            suppressObviousFunctions.set(false)
            sourceRoots.from(file("src/main/kotlin/com/skillw/randomitem/api"))
        }
    }
}


taboolib {

    description {
        contributors {
            name("Glom_")
        }
        dependencies {
            name("Pouvoir")
            name("PlaceholderAPI").optional(true).loadafter(true)
            name("MythicMobs").optional(true).loadafter(true)
        }
    }

    install("common")
    install("module-configuration")
    install("common-5")
    install("module-lang")
    install("module-nms")
    install("module-nms-util")
    install("module-metrics")
    install("platform-bukkit", "expansion-command-helper")
    install("module-chat")
    classifier = null
    version = "6.0.7-44"

}

repositories {
    mavenCentral()
}

dependencies {

    compileOnly("ink.ptms.core:v11605:11605")
    compileOnly("ink.ptms.core:v11200:11200")
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
}



tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
