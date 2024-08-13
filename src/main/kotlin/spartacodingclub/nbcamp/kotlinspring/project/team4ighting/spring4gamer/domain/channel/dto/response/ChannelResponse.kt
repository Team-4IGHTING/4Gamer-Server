package spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.channel.dto.response

import java.util.UUID

data class ChannelResponse(
    val id: Long,
    val title: String,
    val gameTitle: String,
    val introduction: String,
    val admin: UUID,
    val alias: String
)