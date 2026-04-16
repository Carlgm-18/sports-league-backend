plugins {
	kotlin("jvm") version "2.2.21"
	kotlin("plugin.spring") version "2.2.21"
	id("org.springframework.boot") version "4.0.5"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("plugin.jpa") version "2.2.21"
    id("org.openapi.generator") version "7.21.0"
}

group = "es.uib.tfg"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.2")
	implementation("tools.jackson.module:jackson-module-kotlin")
	runtimeOnly("org.postgresql:postgresql")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

openApiGenerate {
    generatorName.set("kotlin-spring")
    inputSpec.set("$rootDir/src/main/resources/api.yaml")

    // 1. Forma moderna en Gradle 8+ para referenciar la carpeta build
    outputDir.set(layout.buildDirectory.dir("generated/openapi").get().asFile.path)

    // 2. LA MAGIA REAL: En el plugin de Gradle, esto es lo que le dice que NO genere controladores
    globalProperties.set(mapOf(
        "models" to "" // Un string vacío significa "Genera TODOS los modelos y omite las APIs"
    ))

    // 3. Desactivamos la basura extra (tests y documentación autogenerada que no usaremos)
    generateModelTests.set(false)
    generateModelDocumentation.set(false)
    generateApiTests.set(false)
    generateApiDocumentation.set(false)

    // Paquetes
    modelPackage.set("es.uib.tfg.sportsapi.dto")
    apiPackage.set("es.uib.tfg.sportsapi.api")

    configOptions.set(mapOf(
        "dateLibrary" to "java8",
        "useSpringBoot3" to "true",
        "useBeanValidation" to "true",
        "enumPropertyNaming" to "UPPERCASE"
    ))
}

// 4. Actualizar también la forma de decirle a Kotlin dónde están los archivos generados
sourceSets {
    main {
        kotlin {
            srcDir(layout.buildDirectory.dir("generated/openapi/src/main/kotlin"))
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("openApiGenerate")
}
