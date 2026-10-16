plugins {
    java
}

group = "com.kingplayz.herobrine"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly "papermc:papermc-api:1.20.4-R0.1-SNAPSHOT"
    compileOnly "org.xerial:sqlite-jdbc:3.45.1.0"
    compileOnly "org.yaml:snakeyaml:2.0"
}

tasks.named("jar") {
    manifest {
        attributes["Main-Class"] = "com.kingplayz.herobrine.HerobrinePlugin"
    }
    from(sourceSets.main.get().output)
    dependsOn(configurations.compileOnly)
    doFirst {
        from("src/main/resources") {
            include "plugin.yml"
            include "config.yml"
            include "messages.yml"
            include "sounds.yml"
        }
    }
}