plugins {
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "2.1.20"
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "cn.cotenite"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

val jjwtVersion = "0.12.6"
val httpclientVersion = "4.5.14"
val fastjsonVersion = "2.0.48"
val hutoolVersion = "5.8.25"
val jacksonVersion = "2.19.0"
val langchain4jVersion = "1.0.0-beta2"
val tinylogVersion = "2.6.2"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    implementation("ch.qos.logback:logback-classic:1.5.18")
    implementation("org.slf4j:slf4j-api:2.0.17")

    // 数据库相关依赖
    implementation("org.postgresql:postgresql")

    // JWT认证
    implementation("io.jsonwebtoken:jjwt:$jjwtVersion")

    // HTTP客户端
    implementation("org.apache.httpcomponents:httpclient:$httpclientVersion")

    // 工具库
    implementation ("com.alibaba.fastjson2:fastjson2:$fastjsonVersion")
    implementation("cn.hutool:hutool-all:$hutoolVersion")

    // Jackson
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")

    // Jakarta JSON Binding
    implementation("jakarta.json.bind:jakarta.json.bind-api:3.0.0")
    implementation("org.eclipse:yasson:3.0.4")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    implementation("dev.langchain4j:langchain4j-mcp:${langchain4jVersion}")
    implementation("dev.langchain4j:langchain4j-open-ai:${langchain4jVersion}")
    implementation("dev.langchain4j:langchain4j:${langchain4jVersion}")
    implementation("dev.langchain4j:langchain4j-embeddings-all-minilm-l6-v2:${langchain4jVersion}")

    implementation("org.tinylog:tinylog-impl:${tinylogVersion}")
    implementation("org.tinylog:slf4j-tinylog:${tinylogVersion}")


    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
