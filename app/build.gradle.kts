import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("application")
    id("jacoco")
    id("se.patrikerdes.use-latest-versions") version "0.2.19"
    id("com.github.ben-manes.versions") version "0.53.0"
    id("org.sonarqube") version "7.2.2.6593"
    id("checkstyle")
    id("com.gradleup.shadow") version "9.3.1"
    id("io.freefair.lombok") version "9.2.0"
}

group = "hexlet.code"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass.set("hexlet.code.App")
}

sonar {
    properties {
        property("sonar.projectKey", "mtvru_java-project-72")
        property("sonar.organization", "mtvru")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.token", System.getenv("SONAR_TOKEN"))
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
    }
}

checkstyle {
    toolVersion = "10.12.4"
}

jacoco {
    toolVersion = "0.8.14"
}

dependencies {
    implementation("io.javalin:javalin:6.7.0")
    implementation("io.javalin:javalin-rendering:6.7.0")
    implementation("io.javalin:javalin-bundle:6.7.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.21.0")
    implementation("gg.jte:jte:3.2.2")
//    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("com.h2database:h2:2.4.240")
    implementation("org.postgresql:postgresql:42.7.9")
    implementation("com.zaxxer:HikariCP:7.0.2")
    implementation("com.konghq:unirest-java:3.14.1")
    implementation("org.jsoup:jsoup:1.18.3")

    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation(platform("org.junit:junit-bom:6.0.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.squareup.okhttp3:mockwebserver3:5.3.0")
    testImplementation("com.squareup.okhttp3:okhttp-java-net-cookiejar:5.3.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    environment("JDBC_DATABASE_URL", "jdbc:h2:mem:test_page_analyzer;DB_CLOSE_DELAY=-1;")
    systemProperty("org.slf4j.simpleLogger.defaultLogLevel", "warn")
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
        events = setOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
        showStandardStreams = true
    }
}

tasks.compileJava {
    options.compilerArgs.add("-Aproject=${project.group}/${project.name}")
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacocoHtml"))
    }
}

tasks.shadowJar {
    archiveBaseName.set("app")
    archiveClassifier.set("all")
    archiveVersion.set("1.0-SNAPSHOT")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    mergeServiceFiles()
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}
