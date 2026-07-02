plugins {
    id("java")
}

group = "com.rssoftware"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    val string = "2.25.2+"
    implementation("org.apache.logging.log4j:log4j-api:" +
            string
    )
    implementation("org.apache.logging.log4j:log4j-core:" +
            string
    )

    // SLF4J 2.x -> Log4j2 bridge
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:" +
            string
    )

    testImplementation(platform("org.junit:junit-bom:5.14.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("com.clickhouse:client-v2:0.9.8")
//    testImplementation("org.testcontainers:clickhouse:1.21.4")
    testImplementation("org.testcontainers:testcontainers:1.21.4")


}

tasks.test {
    useJUnitPlatform()
}