dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.1")
    }
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.kotlin.reflect)
    
    // Service discovery
    implementation(libs.spring.cloud.starter.netflix.eureka.client)
    
    // Kafka for event-driven communication
    implementation(libs.spring.kafka)
    
    // Actuator for health checks
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // PostgreSQL dependency
    runtimeOnly(libs.postgresql)

    // H2 for development and testing
    implementation(libs.h2)

    // Test dependencies
    testImplementation(libs.spring.boot.starter.test) {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Configure the main class for the application
springBoot {
    mainClass.set("com.github.rezaiyan.subscriptionmanager.SubscriptionManagerApplicationKt")
}

// Task to run the PostgreSQL connection checker
tasks.register<JavaExec>("checkPostgreSQL") {
    group = "verification"
    description = "Checks if PostgreSQL is installed and accessible"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.github.rezaiyan.subscriptionmanager.PostgreSQLConnectionChecker")
}