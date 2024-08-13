package spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.model

import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.dto.response.PostTagResponse

@Entity
@Table(name = "post_tag")
class PostTag private constructor(
    post: Post,
    tag: Tag
) {

    @EmbeddedId
    val id: PostTagId = PostTagId(
        post = post,
        tag = tag
    )


    companion object {

        fun from(post: Post, tag: Tag): PostTag =

            PostTag(
                post = post,
                tag = tag
            )
    }
}

fun PostTag.toResponse(): PostTagResponse =

    PostTagResponse(
        name = id.tag.name
    )