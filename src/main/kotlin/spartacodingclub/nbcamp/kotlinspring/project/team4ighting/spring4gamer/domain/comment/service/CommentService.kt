package spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.comment.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.board.repository.BoardRepository
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.channel.repository.ChannelRepository
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.channeladmin.model.ChannelBlacklistId
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.channeladmin.repository.ChannelBlacklistRepository
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.comment.dto.response.CommentResponse
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.comment.dto.request.CreateCommentRequest
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.comment.dto.request.UpdateCommentRequest
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.comment.dto.response.CommentReportResponse
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.comment.model.Comment
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.comment.model.CommentReaction
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.comment.model.CommentReport
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.comment.model.toResponse
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.comment.repository.CommentReactionRepository
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.comment.repository.CommentReportRepository
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.comment.repository.CommentRepository
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.member.model.Member
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.member.repository.MemberRepository
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.model.Post
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.repository.PostRepository
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.util.RedissonLockUtility
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.exception.CustomAccessDeniedException
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.exception.ModelNotFoundException
import java.util.*

@Service
class CommentService(
    private val channelRepository: ChannelRepository,
    private val boardRepository: BoardRepository,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val memberRepository: MemberRepository,
    private val commentReactionRepository: CommentReactionRepository,
    private val commentReportRepository: CommentReportRepository,
    private val redissonLockUtility: RedissonLockUtility,
    private val channelBlacklistRepository: ChannelBlacklistRepository
) {

    @Transactional
    fun createComment(
        channelId: Long,
        boardId: Long,
        postId: Long,
        request: CreateCommentRequest,
        memberId: UUID
    ): CommentResponse =

        doAfterResourceValidation(channelId, boardId, postId, null, memberId) { targetPost, _, member ->
            commentRepository.save(
                Comment.from(
                    content = request.content,
                    memberId = member!!.id,
                    post = targetPost,
                    author = member!!.nickname
                )
            ).toResponse()
        }


    fun getCommentList(
        channelId: Long,
        boardId: Long,
        postId: Long,
        pageable: Pageable
    ): Page<CommentResponse> =

        doAfterResourceValidation(channelId, boardId, postId, null, null) { targetPost, _, _ ->
            commentRepository.findByPost(targetPost, pageable)
                .map { it.toResponse() }
        }


    @Transactional
    fun updateComment(
        channelId: Long,
        boardId: Long,
        postId: Long,
        commentId: Long,
        request: UpdateCommentRequest,
        memberId: UUID
    ): CommentResponse =

        doAfterResourceValidation(channelId, boardId, postId, commentId, memberId) { _, targetComment, member ->
            checkCommentOwnership(targetComment!!, member!!)
            targetComment.update(
                content = request.content
            )

            targetComment.toResponse()
        }


    @Transactional
    fun deleteComment(
        channelId: Long,
        boardId: Long,
        postId: Long,
        commentId: Long,
        memberId: UUID
    ) {

        doAfterResourceValidation(channelId, boardId, postId, commentId, memberId) { _, targetComment, member ->
            checkCommentOwnership(targetComment!!, member!!)

            val commentReactions = commentReactionRepository.findAllByCommentId(targetComment.id!!)
            commentReactionRepository.deleteAllInBatch(commentReactions)
            commentRepository.delete(targetComment)
        }
    }


    @Transactional
    fun addReaction(
        channelId: Long,
        boardId: Long,
        postId: Long,
        commentId: Long,
        memberId: UUID,
        isUpvoting: Boolean
    ) {

        doAfterResourceValidation(channelId, boardId, postId, commentId, memberId) { _, targetComment, member ->
            val reaction = commentReactionRepository.findByIdCommentIdAndIdMemberId(commentId, memberId)

            if (reaction == null) {
                val newReaction = CommentReaction.from(member!!, targetComment!!, isUpvoting)

                redissonLockUtility.runExclusive("$commentId") {
                    targetComment!!.increaseReaction(isUpvoting)
                }
                commentReactionRepository.save(newReaction)
            } else {
                if (reaction.isUpvoting == isUpvoting)
                    throw IllegalArgumentException("같은 대상에 같은 반응을 중복하여 넣을 수 없습니다.")

                redissonLockUtility.runExclusive("$commentId") {
                    targetComment!!.applySwitchedReaction(isUpvoting)
                }
                reaction.isUpvoting = isUpvoting
            }
        }
    }


    @Transactional
    fun deleteReaction(
        channelId: Long,
        boardId: Long,
        postId: Long,
        commentId: Long,
        memberId: UUID
    ) {

        doAfterResourceValidation(channelId, boardId, postId, commentId, memberId) { _, targetComment, member ->
            val reaction = commentReactionRepository.findByIdCommentIdAndIdMemberId(commentId, member!!.id)
                ?: throw ModelNotFoundException("CommentReaction", "${commentId}/${memberId}")

            redissonLockUtility.runExclusive("$commentId") {
                targetComment!!.decreaseReaction(reaction.isUpvoting)
            }
            commentReactionRepository.delete(reaction)
        }
    }


    fun reportComment(
        channelId: Long,
        boardId: Long,
        postId: Long,
        commentId: Long,
        memberId: UUID,
        reason: String
    ): CommentReportResponse =

        doAfterResourceValidation(channelId, boardId, postId, commentId, memberId) { _, targetComment, member ->
            commentReportRepository.save(
                CommentReport.from(
                    comment = targetComment!!,
                    reason = reason,
                    subject = member!!
                )
            ).toResponse()
        }


    private fun <T> doAfterResourceValidation(
        channelId: Long,
        boardId: Long,
        postId: Long,
        commentId: Long?,
        memberId: UUID?,
        func: (post: Post, comment: Comment?, member: Member?) -> T
    ): T {

        val channel = channelRepository.findByIdOrNull(channelId)
            ?: throw ModelNotFoundException("Channel", channelId)
        val board = boardRepository.findByIdAndChannelId(boardId, channelId)
            ?: throw ModelNotFoundException("Board", boardId)
        val post = postRepository.findByIdAndBoard(postId, board)
            ?: throw ModelNotFoundException("Post", postId)
        val comment =
            if (commentId != null)
                commentRepository.findByIdAndPost(commentId, post)
                    ?: throw ModelNotFoundException("Comment", commentId)
            else null
        val member =
            if (memberId != null)
                memberRepository.findByIdOrNull(memberId)
                    ?: throw ModelNotFoundException("Member", memberId)
            else null
        if (channelBlacklistRepository.existsById(ChannelBlacklistId(channel, member)))
            throw CustomAccessDeniedException("해당 채널에 대한 권한이 없습니다.")

        return kotlin.run { func.invoke(post, comment, member) }
    }

    private fun checkCommentOwnership(comment: Comment, member: Member) {

        if (comment.memberId != member.id)
            throw CustomAccessDeniedException("해당 댓글에 대한 권한이 없습니다.")
    }
}