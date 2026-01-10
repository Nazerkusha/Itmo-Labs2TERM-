plugins {
    war
    java
    id("org.gretty") version "4.1.2"
}

group = "com.lab4.geometry"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

gretty {
    servletContainer = "jetty11"
    contextPath = "/geometry-service"
    httpPort = 8082
}

dependencies {

    compileOnly("jakarta.platform:jakarta.jakartaee-web-api:10.0.0")
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")

    implementation("org.glassfish.jersey.core:jersey-server:3.1.3")
    implementation("org.glassfish.jersey.containers:jersey-container-servlet:3.1.3")
    implementation("org.glassfish.jersey.inject:jersey-hk2:3.1.3")
    implementation("org.glassfish.jersey.media:jersey-media-json-jackson:3.1.3")

    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    implementation("com.influxdb:influxdb-client-java:6.10.0")
}

tasks.war {
    archiveFileName.set("geometry-service.war")
}
