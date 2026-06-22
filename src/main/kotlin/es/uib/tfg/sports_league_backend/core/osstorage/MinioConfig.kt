package es.uib.tfg.sports_league_backend.core.osstorage

import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.SetBucketPolicyArgs
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MinioConfig(
    @Value($$"${minio.endpoint}") private val endpoint: String,
    @Value($$"${minio.access-key}") private val accessKey: String,
    @Value($$"${minio.secret-key}") private val secretKey: String,
    @Value($$"${minio.bucket}") private val bucket: String
) {

    @Bean
    fun minioClient(): MinioClient {
        val client = MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .build()
        try {
            val exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build())
                
                val policyJson = """
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Effect": "Allow",
                      "Principal": "*",
                      "Action": ["s3:GetObject"],
                      "Resource": ["arn:aws:s3:::$bucket/*"]
                    }
                  ]
                }
                """.trimIndent()
                
                client.setBucketPolicy(
                    SetBucketPolicyArgs.builder().bucket(bucket).config(policyJson).build()
                )
            }
        } catch (e: Exception) {
            println("Warning: Could not verify/create MinIO bucket: ${e.message}")
        }
        return client
    }
}