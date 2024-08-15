package spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.dto.response

import java.time.ZonedDateTime
import java.util.UUID

data class PostSimplifiedResponse(
    val id: Long,
    val title: String,
    val view: Long,
    val upvotes: Long,
    val downvotes: Long,
    val author: String,
    val memberId: UUID,
    val createdAt: ZonedDateTime,
    val attachment: String?,
)