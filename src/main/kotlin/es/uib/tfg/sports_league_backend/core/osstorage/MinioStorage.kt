package es.uib.tfg.sports_league_backend.core.osstorage

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

    override fun generateUploadUrl(folder: String, extension: String): StorageUrlResponse {
        val fileName = "${UUID.randomUUID()}.$extension"
        val objectPath = "$folder/$fileName" // Ej: avatars/123-456.jpg

        val uploadUrl = minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Method.PUT)
                .bucket(bucket)
                .`object`(objectPath)
                .expiry(5, TimeUnit.MINUTES)
                .build()
        )

        return StorageUrlResponse(
            uploadUrl = uploadUrl,
            publicUrl = "$endpoint/$bucket/$objectPath"
        )
    }
}