package spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.notification.dto

import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.notification.type.PublishType
import java.time.ZonedDateTime

data class MessageSubResponse(
    val type: PublishType,
    val subjectId: String,
    val roomId: String? = null,
    val targetId: String,
    val message: String,
    val createdAt: ZonedDateTime
)