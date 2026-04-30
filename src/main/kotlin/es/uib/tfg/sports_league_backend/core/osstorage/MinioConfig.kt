package es.uib.tfg.sports_league_backend.core.osstorage

import io.minio.MinioClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MinioConfig(
    // Inyectamos los valores directamente desde el application.yml
    @Value($$"${minio.endpoint}") private val endpoint: String,
    @Value($$"${minio.access-key}") private val accessKey: String,
    @Value($$"${minio.secret-key}") private val secretKey: String
) {

    @Bean
    fun minioClient(): MinioClient {
        // Le enseñamos a Spring cómo construir el cliente
        return MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .build()
    }
}