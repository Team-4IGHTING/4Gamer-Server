package spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.dto.request

import jakarta.validation.constraints.Size

data class UpdatePostRequest (
    @field:Size(min = 1, max = 128, message = "최소 1자에서 최대 128자까지 입력 가능합니다.")
    val title: String,

    @field:Size(min = 10, max = 8192, message = "최소 1자에서 최대 8192자까지 입력 가능합니다.")
    val body: String,

    val tags: List<String>,

    val attachment: String?,
)