plugins {
    kotlin("jvm") version "1.3.72"
    id("org.jmailen.kotlinter") version "2.4.1"
    id("maven")
    `java-library`
}

java {
    withSourcesJar()
}

group = "com.hitoda"
version = "0.1"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    testImplementation("io.kotest:kotest-runner-junit5-jvm:4.1.1") // for kotest framework
    testImplementation("io.kotest:kotest-runner-console-jvm:4.1.1") // for kotest framework
    testImplementation("io.kotest:kotest-assertions-core-jvm:4.1.1") // for kotest core jvm assertions
    testImplementation("io.kotest:kotest-property-jvm:4.1.1") // for kotest property test
}

apply(plugin = "org.jmailen.kotlinter")

kotlinter {
    ignoreFailures = false
    indentSize = 4
    reporters = arrayOf("checkstyle", "plain")
    experimentalRules = false
    disabledRules = arrayOf("no-wildcard-imports")
}