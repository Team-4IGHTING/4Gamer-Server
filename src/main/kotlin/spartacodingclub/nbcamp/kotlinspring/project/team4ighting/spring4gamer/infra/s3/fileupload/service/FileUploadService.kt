package spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.infra.s3.fileupload.service

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.DeleteObjectRequest
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.ListObjectsRequest
import com.amazonaws.services.s3.model.ObjectListing
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.infra.s3.fileupload.dto.response.S3GetResponseDto
import java.util.*

@Service
class FileUploadService(
    private val s3Client: AmazonS3Client,
    @Value("\${spring.cloud.aws.s3.bucket}")
    private val bucket: String
) {

    @Transactional
    fun preSignedUrl(file: String): String {

        val imageTypes = listOf("jpg", "jpeg", "png", "gif", "bmp")

        val fileExtension = file.substringAfterLast(".", "")

        if (!imageTypes.contains(fileExtension.lowercase())) {
            throw IllegalArgumentException("이미지 파일만 업로드가 가능합니다.")
        }
        val expiration = Date(System.currentTimeMillis() + 60 * 60 * 1000)
        val url = GeneratePresignedUrlRequest(bucket, file)
            .withMethod(com.amazonaws.HttpMethod.PUT)
            .withExpiration(expiration)

        return s3Client.generatePresignedUrl(url).toString()
    }

    @Transactional
    fun getFile(file: String): String =

        s3Client.getUrl(
            bucket,
            file).toString()


    fun find(
        request: HttpServletRequest
    ): S3GetResponseDto {

        val split = request.requestURI.split("/s3/list")
        val prefix = if (split.size < 2) "" else split[1].substring(1)

        val fileNames = mutableListOf<String>()
        var listObjectsRequest = ListObjectsRequest().withBucketName(bucket)

        if (prefix.isNotEmpty()) {
            listObjectsRequest = listObjectsRequest.withPrefix(prefix)
        }

        var s3Objects: ObjectListing?

        do {
            s3Objects = s3Client.listObjects(listObjectsRequest)
            s3Objects.objectSummaries.forEach { s3ObjectSummary ->
                fileNames.add(s3ObjectSummary.key)
            }
            listObjectsRequest.marker = s3Objects.nextMarker
        } while (s3Objects!!.isTruncated)

        return S3GetResponseDto.from(fileNames)
    }

    @Transactional
    fun delete(file: String) {

        val toDelete = DeleteObjectRequest(bucket, file)

        s3Client.deleteObject(toDelete)
    }
}