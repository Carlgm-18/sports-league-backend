package es.uib.tfg.sports_league_backend.osstorage.infrastructure.repository

import es.uib.tfg.sports_league_backend.osstorage.application.ports.out.StoragePort
import es.uib.tfg.sports_league_backend.osstorage.domain.StorageUrl
import io.minio.GetPresignedObjectUrlArgs
import io.minio.MinioClient
import io.minio.http.Method
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.TimeUnit

@Service
class MinioStorageAdapter(
    private val minioClient: MinioClient,
    @Value($$"${minio.bucket}") private val bucket: String,
    @Value($$"${minio.endpoint}") private val endpoint: String
) : StoragePort {

    override fun generateUploadUrl(folder: String, extension: String): StorageUrl {
        val fileName = "${UUID.randomUUID()}.$extension"
        val objectPath = "$folder/$fileName"

        val uploadUrl = minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Method.PUT)
                .bucket(bucket)
                .`object`(objectPath)
                .expiry(5, TimeUnit.MINUTES)
                .build()
        )

        return StorageUrl(
            uploadUrl = uploadUrl,
            publicUrl = "$endpoint/$bucket/$objectPath"
        )
    }
}