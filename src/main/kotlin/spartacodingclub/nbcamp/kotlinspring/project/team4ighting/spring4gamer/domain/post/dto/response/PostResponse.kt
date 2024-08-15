package spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.dto.response

import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.board.dto.response.BoardResponse
import java.io.Serializable
import java.time.ZonedDateTime
import java.util.UUID

data class PostResponse(
    val id: Long,
    val title: String,
    val body: String,
    val views: Long, // 조회수
    val upvotes: Long,
    val downvotes: Long,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime,
    val author: String, // 닉네임
    val memberId: UUID,
    val board: BoardResponse,
    val attachment: String?,
): Serializable