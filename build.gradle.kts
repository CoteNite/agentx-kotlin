plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
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

// 定义版本号，如果你的pom.xml中定义了这些变量
val mybatisPlusVersion = "3.5.12" // 替换为你的实际版本
val jjwtVersion = "0.12.6" // 替换为你的实际版本
val httpclientVersion = "4.5.14" // 替换为你的实际版本
val fastjsonVersion = "1.2.80" // 替换为你的实际版本，注意fastjson的安全性问题，建议考虑升级到fastjson2或使用Jackson
val hutoolVersion = "5.8.25" // 替换为你的实际版本



dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    implementation("ch.qos.logback:logback-classic:1.5.18")
    implementation("org.slf4j:slf4j-api:2.0.17")

    // MyBatis Plus 依赖
    implementation("com.baomidou:mybatis-plus-boot-starter:$mybatisPlusVersion")

    // 数据库相关依赖
    runtimeOnly("org.postgresql:postgresql") // 注意这里没有指定版本，Spring Boot 会管理版本

    // JWT认证
    implementation("io.jsonwebtoken:jjwt:$jjwtVersion")

    // HTTP客户端
    implementation("org.apache.httpcomponents:httpclient:$httpclientVersion")

    // 工具库
    implementation("com.alibaba:fastjson:$fastjsonVersion")
    implementation("cn.hutool:hutool-all:$hutoolVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

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
