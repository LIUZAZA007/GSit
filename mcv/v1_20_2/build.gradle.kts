plugins {
    `java-library`
    id("io.papermc.paperweight.userdev")
}

dependencies {
    compileOnly(project(":core"))
    paperweight.paperDevBundle("1.20.2-R0.1-SNAPSHOT")
}

configurations {
    compileClasspath {
        exclude(group = "io.netty", module = "netty-transport-native-epoll")
    }
}

paperweight {
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.REOBF_PRODUCTION

java {
    toolchain.languageVersion = JavaLanguageVersion.of(20)
}

tasks.compileJava {
    options.release = 17
}

tasks.assemble {
    dependsOn(tasks.reobfJar)
}
