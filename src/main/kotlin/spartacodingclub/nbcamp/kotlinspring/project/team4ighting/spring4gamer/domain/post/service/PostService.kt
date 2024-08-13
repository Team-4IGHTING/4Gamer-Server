package spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.service

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.board.model.Board
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.board.repository.BoardRepository
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.channel.repository.ChannelRepository
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.channeladmin.model.ChannelBlacklistId
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.channeladmin.repository.ChannelBlacklistRepository
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.comment.repository.CommentRepository
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.member.model.Member
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.member.repository.MemberRepository
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.dto.request.CreatePostRequest
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.dto.response.PostResponse
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.dto.response.PostSimplifiedResponse
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.dto.request.UpdatePostRequest
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.dto.response.PostReportResponse
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.dto.response.PostTagResponse
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.model.*
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.repository.*
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.exception.CustomAccessDeniedException
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.exception.ModelNotFoundException
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.util.CookieUtil
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.util.RedissonLockUtility
import java.util.*

@Service
class PostService(
    private val channelRepository: ChannelRepository,
    private val boardRepository: BoardRepository,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val memberRepository: MemberRepository,
    private val postReactionRepository: PostReactionRepository,
    private val postReportRepository: PostReportRepository,
    private val tagRepository: TagRepository,
    private val postTagRepository: PostTagRepository,
    private val redissonLockUtility: RedissonLockUtility,
    private val channelBlacklistRepository: ChannelBlacklistRepository
) {

    @Transactional
    fun createPost(
        channelId: Long,
        boardId: Long,
        request: CreatePostRequest,
        memberId: UUID
    ): PostResponse =

        doAfterResourceValidation(channelId, boardId, null, memberId) { board, _, member ->
            val newPost = postRepository.save(
                Post.from(
                    request = request,
                    board = board,
                    memberId = memberId,
                    author = member!!.nickname
                )
            )

            for (tagName in request.tags) {
                var tag = tagRepository.findByIdOrNull(tagName)

                if (tag == null) {
                    tag = Tag.from(name = tagName)
                    tagRepository.save(tag)
                } else
                    tag.refresh()

                postTagRepository.save(
                    PostTag.from(
                        post = newPost,
                        tag = tag
                    )
                )
            }

            postRepository.save(newPost).toResponse()
        }


    fun getPostList(
        channelId: Long,
        boardId: Long,
        pageable: Pageable
    ): Page<PostSimplifiedResponse> =

        doAfterResourceValidation(channelId, boardId, null, null) { _, _, _ ->
            postRepository.findByBoard(
                board = (boardRepository.findByIdAndChannelId(boardId, channelId)
                    ?: throw ModelNotFoundException("Board", boardId)),
                pageable = pageable
            ).map { it.toPostSimplifiedResponse() }
        }


    @Transactional
    fun getPost(
        channelId: Long,
        boardId: Long,
        postId: Long,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): PostResponse =

        doAfterResourceValidation(channelId, boardId, postId, null) { _, targetPost, _ ->
            viewCountUp(targetPost!!, request, response)

            targetPost.toResponse()
        }


    fun getTagsInPost(
        channelId: Long,
        boardId: Long,
        postId: Long
    ): List<PostTagResponse> =

        doAfterResourceValidation(channelId, boardId, postId, null) { _, targetPost, _ ->
            postTagRepository.findAllTagsByPostId(targetPost!!.id!!)
                .map { it.toResponse() }
        }


    @Transactional
    fun updatePost(
        channelId: Long,
        boardId: Long,
        postId: Long,
        request: UpdatePostRequest,
        memberId: UUID
    ): PostResponse =

        doAfterResourceValidation(channelId, boardId, postId, memberId) { _, targetPost, member ->
            checkResourceOwnership(targetPost!!, member!!)

            targetPost.update(
                title = request.title,
                body = request.body,
                attachment = request.attachment
            )

            val previousTags = postTagRepository.findAllTagsByPostId(targetPost.id!!).toSet()
            val previousTagsString = previousTags.map { it.id.tag.name }

            val oldTagsName = previousTags.filter { it.id.tag.name !in request.tags }
            val newTagsName = request.tags.filter { it !in previousTagsString }

            postTagRepository.deleteAllInBatch(oldTagsName)
            for (tagName in newTagsName) {

                var tag = tagRepository.findByIdOrNull(tagName)

                if (tag == null) {
                    tag = Tag.from(name = tagName)
                    tagRepository.save(tag)
                } else
                    tag.refresh()

                postTagRepository.save(
                    PostTag.from(
                        post = targetPost,
                        tag = tag
                    )
                )
            }

            targetPost.toResponse()
        }


    @Transactional
    fun deletePost(
        channelId: Long,
        boardId: Long,
        postId: Long,
        memberId: UUID
    ) {

        doAfterResourceValidation(channelId, boardId, postId, memberId) { _, targetPost, member ->
            checkResourceOwnership(targetPost!!, member!!)

            val comments = commentRepository.findAllByPostId(targetPost.id!!)
            val tags = postTagRepository.findAllTagsByPostId(targetPost.id!!)

            commentRepository.deleteAllInBatch(comments)
            postTagRepository.deleteAllInBatch(tags)
            postRepository.delete(targetPost)
        }
    }


    @Transactional
    fun addReaction(
        channelId: Long,
        boardId: Long,
        postId: Long,
        memberId: UUID,
        isUpvoting: Boolean
    ) {

        doAfterResourceValidation(channelId, boardId, postId, memberId) { _, targetPost, member ->
            val reaction = postReactionRepository.findByIdPostIdAndIdMemberId(postId, memberId)

            if (reaction == null) {
                val newReaction = PostReaction.from(member!!, targetPost!!, isUpvoting)

                redissonLockUtility.runExclusive("$postId") {
                    targetPost.increaseReaction(isUpvoting)
                }
                postReactionRepository.save(newReaction)
            } else {
                if (reaction.isUpvoting == isUpvoting)
                    throw IllegalArgumentException("같은 대상에 같은 반응을 중복하여 넣을 수 없습니다.")

                redissonLockUtility.runExclusive("$postId") {
                    targetPost!!.applySwitchedReaction(isUpvoting)
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
        memberId: UUID
    ) {

        doAfterResourceValidation(channelId, boardId, postId, memberId) { _, targetPost, _ ->
            val reaction = postReactionRepository.findByIdPostIdAndIdMemberId(postId, memberId)
                ?: throw ModelNotFoundException("PostReaction", "${postId}/${memberId}")

            redissonLockUtility.runExclusive("$postId") {
                targetPost!!.decreaseReaction(reaction.isUpvoting)
            }
            postReactionRepository.delete(reaction)
        }
    }


    fun reportPost(
        channelId: Long,
        boardId: Long,
        postId: Long,
        reason: String,
        memberId: UUID
    ): PostReportResponse =

        doAfterResourceValidation(channelId, boardId, postId, memberId) { _, targetPost, member ->
            postReportRepository.save(
                PostReport.from(
                    post = targetPost!!,
                    reason = reason,
                    subject = member!!
                )
            ).toResponse()
        }


    private fun viewCountUp(
        post: Post,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {

        request.cookies?.map {
            if (it.name == "viewCounts") {
                if (it.value.contains("[${post.id}]")) {
                    return
                } else {
                    CookieUtil.generateMidnightExpiryCookie(post.id!!, it, response)
                    return post.updateViews()
                }
            }
        }

        CookieUtil.generateMidnightExpiryCookie(post.id!!, null, response)

        return post.updateViews()
    }


    private fun <T> doAfterResourceValidation(
        channelId: Long,
        boardId: Long,
        postId: Long?,
        memberId: UUID?,
        func: (board: Board, post: Post?, member: Member?) -> T
    ): T {

        val channel =
            channelRepository.findByIdOrNull(channelId)
                ?: throw ModelNotFoundException("Channel", channelId)
        val board = boardRepository.findByIdAndChannelId(boardId, channelId)
            ?: throw ModelNotFoundException("Board", boardId)
        val post =
            if (postId != null)
                postRepository.findByIdAndBoard(postId, board)
                    ?: throw ModelNotFoundException("Post", postId)
            else null
        val member =
            if (memberId != null)
                memberRepository.findByIdOrNull(memberId)
                    ?: throw ModelNotFoundException("Member", memberId)
            else null
        if (channelBlacklistRepository.existsById(ChannelBlacklistId(channel, member)))
            throw CustomAccessDeniedException("해당 채널에 대한 권한이 없습니다.")

        return kotlin.run { func.invoke(board, post, member) }
    }

    private fun checkResourceOwnership(post: Post, member: Member) {

        if (post.memberId != member.id)
            throw CustomAccessDeniedException("해당 댓글에 대한 권한이 없습니다.")
    }
}