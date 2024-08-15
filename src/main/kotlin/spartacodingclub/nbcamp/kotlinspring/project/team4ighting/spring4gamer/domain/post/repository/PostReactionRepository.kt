package spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.model.PostReaction
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.model.PostReactionId
import java.util.*

interface PostReactionRepository : JpaRepository<PostReaction, PostReactionId> {

    fun findByIdPostIdAndIdMemberId(postId: Long, memberId: UUID): PostReaction?

    @Query("select pr from PostReaction pr join fetch pr.id.post where pr.id.member.id = :memberId")
    fun findByMemberId(memberId: UUID): List<PostReaction>

    @Query("select pr from PostReaction pr join fetch pr.id.post where pr.id.post.id = :postId")
    fun findByPostId(postId: Long): List<PostReaction>

    @Query("select pr from PostReaction pr join fetch pr.id.post where pr.id.post.id in :postIds")
    fun findAllByPostIdIn(postIds: List<Long>): List<PostReaction>
}