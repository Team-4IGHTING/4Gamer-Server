package spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.comment.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.comment.model.CommentReaction
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.comment.model.CommentReactionid
import java.util.*

interface CommentReactionRepository : JpaRepository<CommentReaction, CommentReactionid> {

    fun findByIdCommentIdAndIdMemberId(commentId: Long, memberId: UUID): CommentReaction?

    @Query("select cr from CommentReaction cr join fetch cr.id.comment where cr.id.member.id = :memberId")
    fun findByMemberId(memberId: UUID): List<CommentReaction>

    @Query("select cr from CommentReaction cr join fetch cr.id.comment where cr.id.comment.id = :commentId")
    fun findAllByCommentId(commentId: Long): List<CommentReaction>

    @Query("select cr from CommentReaction cr join fetch cr.id.comment where cr.id.comment.id in :commentIds")
    fun findAllByCommentIdIn(commentIds: List<Long>): List<CommentReaction>
}