plugins {
    java
    // Официальный плагин JavaFX, избавляет от проблем с путями к графическим библиотекам
    id("org.openjfx.javafxplugin") version "0.1.0"
    // Плагин для удобного запуска приложения напрямую из Gradle/IDEA
    application
}

group = "ru.space.monitoring"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        // Утилиту пишем на стабильной LTS Java 17 (можно поменять на 21)
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

javafx {
    version = "17.0.6"
    // Подключаем только те модули JavaFX, которые реально используем в коде
    modules("javafx.controls", "javafx.fxml", "javafx.graphics")
}

dependencies {
    // Официальный JDBC драйвер ClickHouse (содержит все внутренние зависимости HTTP/gRPC)
//    implementation("com.clickhouse:clickhouse-jdbc:0.6.5:all")

    implementation("org.apache.httpcomponents.client5:httpclient5:5.2.1")
    implementation("com.clickhouse:clickhouse-http-client:0.6.5")
    implementation("com.clickhouse:clickhouse-jdbc:0.6.5")

    implementation("org.lz4:lz4-java:1.8.0")

    // Драйвер логирования (ClickHouse использует slf4j, это уберет варнинги в консоли)
    implementation("org.slf4j:slf4j-simple:2.0.9")

    // JUnit для возможных быстрых тестов логики
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.h2database:h2:2.2.224")
}

application {
    // Путь к вашему главному классу с методом main()
    mainClass.set("ru.space.monitoring.MainApp")
}

tasks.test {
    useJUnitPlatform()
}


