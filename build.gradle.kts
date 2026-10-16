plugins {
    java
    id("org.jetbrains.kotlin.jvm") version "2.0.0" apply false
}

group = "com.kingplayz.herobrine"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
    flatDir {
        dirs 'libs'
    }
}

dependencies {
    compileOnly(name: 'spigot-api-1.20.4-R0.1-SNAPSHOT')
    runtimeOnly("org.xerial:sqlite-jdbc:3.45.1.0")
    runtimeOnly("org.yaml:snakeyaml:2.0")
}

tasks.named<Jar>("jar") {
    manifest {
        attributes["Main-Class"] = "com.kingplayz.herobrine.HerobrinePlugin"
    }
    from(sourceSets.main.get().output)
    dependsOn(configurations.compileOnly)
    doFirst {
        from("src/main/resources") {
            include("plugin.yml")
            include("config.yml")
            include("messages.yml")
            include("sounds.yml")
        }
    }
}