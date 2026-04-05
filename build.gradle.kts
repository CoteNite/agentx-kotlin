plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.spring") version "2.1.0"
    kotlin("plugin.noarg") version "2.1.0"
    id("org.springframework.boot") version "3.5.12"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "cn.cotenite"
version = "0.0.1-SNAPSHOT"
description = "agentx-kotlin"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

noArg{
    annotation("com.baomidou.mybatisplus.annotation.TableName")
    invokeInitializers = true
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.baomidou:mybatis-plus-spring-boot3-starter:3.5.15")
    implementation("com.baomidou:mybatis-plus-extension:3.5.15")
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")
    implementation("cn.hutool:hutool-crypto:5.8.26")
    implementation("cn.hutool:hutool-captcha:5.8.26")
    implementation("org.eclipse.angus:jakarta.mail:2.0.2")


    // LangChain4j with Kotlin support
    implementation("dev.langchain4j:langchain4j:1.12.2")
    implementation("dev.langchain4j:langchain4j-kotlin:1.12.2-beta22")
    implementation("dev.langchain4j:langchain4j-anthropic:1.12.2")
    implementation("dev.langchain4j:langchain4j-embeddings-all-minilm-l6-v2:1.12.2-beta22")
    implementation("dev.langchain4j:langchain4j-mcp:1.12.2-beta22")
    implementation("dev.langchain4j:langchain4j-open-ai:1.12.2")
    
    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.8.1")
    
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    runtimeOnly("org.postgresql:postgresql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict","-Xjvm-default=all")
        javaParameters = true
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
