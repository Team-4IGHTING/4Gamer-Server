package spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.comment.model

import jakarta.persistence.*
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.comment.dto.response.CommentResponse
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.common.type.ReactableEntity
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.model.Post
import java.util.*

@Entity
@Table(name = "comment")
class Comment private constructor(
    content: String,
    memberId: UUID,
    post: Post,
    author: String
) : ReactableEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(name = "content", nullable = false)
    var content: String = content
        private set

    @Column(name = "member_id", nullable = false)
    val memberId: UUID = memberId

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    val post: Post = post

    @Column(name = "author", nullable = false)
    val author: String = author


    companion object {

        fun from(
            content: String,
            memberId: UUID,
            post: Post,
            author: String
        ): Comment =

            Comment(
                content = content,
                memberId = memberId,
                post = post,
                author = author
            )
    }

    fun update(content: String) {

        this.content = content
        preUpdate()
    }
}

fun Comment.toResponse(): CommentResponse =

    CommentResponse(
        id = id!!,
        content = content,
        author = author,
        memberId = memberId,
        upvotes = upvotes,
        downvotes = downvotes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )