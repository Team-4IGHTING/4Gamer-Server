package spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.channeladmin.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.board.dto.response.BoardResponse
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.board.model.Board
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.board.model.toResponse
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.board.repository.BoardRepository
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.channel.dto.response.ChannelResponse
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.channel.model.Channel
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.channel.model.toResponse
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.channel.repository.ChannelRepository
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.channeladmin.dto.request.CreateBoardRequest
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.channeladmin.dto.request.UpdateBoardRequest
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.channeladmin.dto.request.UpdateChannelRequest
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.channeladmin.dto.response.ChannelBlacklistResponse
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.channeladmin.model.ChannelBlacklist
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.channeladmin.model.ChannelBlacklistId
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.channeladmin.model.toResponse
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.channeladmin.repository.ChannelBlacklistRepository
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.comment.repository.CommentRepository
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.member.repository.MemberRepository
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.repository.PostRepository
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.exception.CustomAccessDeniedException
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.exception.ModelNotFoundException
import java.util.*

@Service
class ChannelAdminService(
    private val channelRepository: ChannelRepository,
    private val channelBlacklistRepository: ChannelBlacklistRepository,
    private val commentRepository: CommentRepository,
    private val memberRepository: MemberRepository,
    private val boardRepository: BoardRepository,
    private val postRepository: PostRepository,
) {

    @Transactional
    fun createBoard(
        channelId: Long,
        request: CreateBoardRequest,
        channelAdminId: UUID
    ): BoardResponse =

        doAfterResourceValidation(channelId, null, channelAdminId) { channel, _ ->
            boardRepository.save(
                Board.from(
                    request = request,
                    channel = channel
                )
            ).toResponse()
        }


    @Transactional
    fun updateBoard(
        channelId: Long,
        boardId: Long,
        request: UpdateBoardRequest,
        channelAdminId: UUID
    ): BoardResponse =

        doAfterResourceValidation(channelId, boardId, channelAdminId) { _, targetBoard ->
            targetBoard!!.update(request)
            targetBoard!!.toResponse()
        }


    @Transactional
    fun deleteBoard(
        channelId: Long,
        boardId: Long,
        channelAdminId: UUID
    ) {

        doAfterResourceValidation(channelId, boardId, channelAdminId) { _, targetBoard ->
            val subPosts = postRepository.findAllByBoardId(targetBoard!!.id!!)
            val subComments = commentRepository.findAllByPostIdIn(subPosts.map { it.id!! })

            commentRepository.deleteAllInBatch(subComments)
            postRepository.deleteAllInBatch(subPosts)
            boardRepository.delete(targetBoard)
        }
    }


    @Transactional
    fun doBlack(
        channelId: Long,
        memberId: UUID,
        channelAdminId: UUID
    ): ChannelBlacklistResponse =

        doAfterResourceValidation(channelId, null, channelAdminId) { targetChannel, _ ->
            val member = memberRepository.findByIdOrNull(memberId)
                ?: throw ModelNotFoundException("Member", memberId)

            channelBlacklistRepository.save(
                ChannelBlacklist.from(
                    targetChannel,
                    member,
                )
            ).toResponse()
        }


    @Transactional
    fun unBlack(
        channelId: Long,
        memberId: UUID,
        channelAdminId: UUID
    ) {

        doAfterResourceValidation(channelId, null, channelAdminId) { targetChannel, _ ->
            val channelBlacklistId = ChannelBlacklistId(
                channel = targetChannel,
                member = (memberRepository.findByIdOrNull(memberId)
                    ?: throw ModelNotFoundException("Member", memberId))
            )
            val targetBlacklist = channelBlacklistRepository.findByIdOrNull(channelBlacklistId)
                ?: throw ModelNotFoundException("ChannelBlacklist", channelBlacklistId)

            channelBlacklistRepository.delete(targetBlacklist)
        }
    }


    @Transactional
    fun updateChannel(
        channelId: Long,
        request: UpdateChannelRequest,
        channelAdminId: UUID
    ): ChannelResponse =

        doAfterResourceValidation(channelId, null, channelAdminId) { targetChannel, _ ->
            targetChannel.update(request)
            targetChannel.toResponse()
        }


    @Transactional
    fun deleteChannel(
        channelId: Long,
        channelAdminId: UUID
    ) {

        doAfterResourceValidation(channelId, null, channelAdminId) { targetChannel, _ ->
            val subBoards = boardRepository.findAllByChannelId(channelId)
            val subPosts = postRepository.findAllByBoardIdIn(subBoards.map { it.id!! })
            val subComments = commentRepository.findAllByPostIdIn(subPosts.map { it.id!! })

            commentRepository.deleteAllInBatch(subComments)
            postRepository.deleteAllInBatch(subPosts)
            boardRepository.deleteAllInBatch(subBoards)
            channelRepository.delete(targetChannel)
        }
    }


    private fun <T> doAfterResourceValidation(
        channelId: Long,
        boardId: Long?,
        channelAdminId: UUID,
        func: (channel: Channel, board: Board?) -> T
    ): T {

        val targetChannel = channelRepository.findByIdOrNull(channelId)
            ?: throw ModelNotFoundException("Channel", channelId)
        val targetBoard =
            if (boardId != null)
                boardRepository.findByIdAndChannelId(boardId, channelId)
                    ?: throw ModelNotFoundException("Board", boardId)
            else null

        if (targetChannel.admin != channelAdminId)
            throw CustomAccessDeniedException("해당 채널에 대한 권한이 없습니다.")

        return kotlin.run {
            func.invoke(targetChannel, targetBoard)
        }
    }

    fun getBlacklists(channelId: Long): List<ChannelBlacklistResponse> =

        channelBlacklistRepository.findByChannelId(channelId).map { it.toResponse() }

    fun checkBlack(
        channelId: Long,
        memberId: UUID
    ): Boolean {
        val channel =
            channelRepository.findByIdOrNull(channelId)
                ?: throw ModelNotFoundException("Channel", channelId)
        val member =
            memberRepository.findByIdOrNull(memberId)
                ?: throw ModelNotFoundException("Member", memberId)
        return channelBlacklistRepository
            .existsById(ChannelBlacklistId(channel, member))
    }
}