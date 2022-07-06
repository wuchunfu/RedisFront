plugins {
    `java-library`
}

group = "com.redisfront"
version = "1.0-SNAPSHOT"

var flatlafVersion = "2.3"
var hutoolVersion = "5.8.3"
var jedisVersion = "4.2.3"
var fifesoftVersion = "3.2.0"

repositories {
    mavenCentral()
}

// check required Java version
if (JavaVersion.current() < JavaVersion.VERSION_17)
    throw RuntimeException("Java required (running ${JavaVersion.VERSION_17})")

// log version, Gradle and Java versions
println()
println("-------------------------------------------------------------------------------")
println("RedisFront Version: ${version}")
println("Gradle ${gradle.gradleVersion} at ${gradle.gradleHomeDir}")
println("Java ${System.getProperty("java.version")}")
println()

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    implementation("io.lettuce:lettuce-core:6.1.8.RELEASE")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("com.formdev:flatlaf:${flatlafVersion}")
    implementation("com.formdev:flatlaf-swingx:${flatlafVersion}")
    implementation("com.formdev:flatlaf-intellij-themes:${flatlafVersion}")
    implementation("com.formdev:flatlaf-extras:${flatlafVersion}")
    implementation("at.swimmesberger:swingx-core:1.6.8")
    implementation("com.jgoodies:jgoodies-forms:1.9.0")
    implementation("redis.clients:jedis:${jedisVersion}")
    implementation("cn.hutool:hutool-extra:${hutoolVersion}")
    implementation("com.jcraft:jsch:0.1.55")
    implementation("org.apache.derby:derby:10.15.2.0")
    implementation("commons-net:commons-net:3.8.0")
    implementation("org.bouncycastle:bcpkix-jdk15on:1.70")
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")
    implementation("com.fifesoft:rsyntaxtextarea:${fifesoftVersion}")
    implementation("com.fifesoft:rstaui:${fifesoftVersion}")
    implementation("com.intellij:forms_rt:7.0.3")
    implementation("ch.qos.logback:logback-classic:1.2.11")
}

tasks {

    getByName<Test>("test") {
        useJUnitPlatform()
    }

    withType<JavaCompile>().configureEach {
        sourceCompatibility = "17"
        targetCompatibility = "17"

        options.encoding = "ISO-8859-1"
        options.isDeprecation = false
    }

    withType<Jar>().configureEach {

        manifest {
            attributes("Main-Class" to "com.redisfront.RedisFrontApplication")
            attributes("Implementation-Vendor" to "www.redisfront.com")
            attributes("Main-Class" to project.version)
            attributes("Multi-Release" to "true")
        }

        exclude("module-info.class")
        exclude("META-INF/versions/*/module-info.class")

        // include all dependencies in jar
        from({
            configurations.runtimeClasspath.get()
                .filter { it.name.endsWith("jar") }
                .map {
                    zipTree(it).matching {
                        exclude("META-INF/LICENSE")
                    }
                }
        })

        // add META-INF/LICENSE to all created JARs
        from("${rootDir}/LICENSE") {
            into("META-INF")
        }
    }

    withType<Javadoc>().configureEach {
        options {
            this as StandardJavadocDocletOptions
            title = "${project.name} $version"
            header = title
            isUse = true
            tags = listOf("uiDefault", "clientProperty")
            addStringOption("Xdoclint:all,-missing", "-Xdoclint:all,-missing")
            links("https://docs.oracle.com/en/java/javase/11/docs/api/")
        }
        isFailOnError = false
    }
}