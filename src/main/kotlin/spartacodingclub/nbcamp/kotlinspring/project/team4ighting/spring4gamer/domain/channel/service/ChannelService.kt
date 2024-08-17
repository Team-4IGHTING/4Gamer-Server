package spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.channel.service

import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.channel.dto.request.CreateChannelRequest
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.channel.dto.response.ChannelResponse
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.channel.model.Channel
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.channel.model.toResponse
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.channel.repository.ChannelRepository
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.member.repository.MemberRepository
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.dto.response.PostResponse
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.model.toResponse
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.exception.CustomAccessDeniedException
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.exception.ModelNotFoundException
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.infra.igdb.SaveAccessToken
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.infra.igdb.service.IgdbService
import java.util.*

@Service
class ChannelService(
    private val channelRepository: ChannelRepository,
    private val memberRepository: MemberRepository,
    private val igdbService: IgdbService,
    private val saveAccessToken: SaveAccessToken,
) {

    fun createChannel(
        request: CreateChannelRequest,
        memberId: UUID
    ): ChannelResponse {

        val member = memberRepository.findByIdOrNull(memberId)
            ?: throw ModelNotFoundException("Member", memberId)
        member.assignChannelAdmin()
        println(igdbService.checkGamesName(request.gameTitle))

        if(!igdbService.checkGamesName(request.gameTitle)) {
            throw CustomAccessDeniedException("다음과 같은 게임의 이름을 찾을 수 없습니다. ${request.gameTitle}")
        }

        memberRepository.save(member)
        return channelRepository.save(
            Channel.from(
                request = request,
                admin = memberId
            )
        ).toResponse()
    }


    @Transactional
    fun getChannelList(pageable: Pageable): Slice<ChannelResponse> =

        channelRepository.findAllBy(pageable)
            .map { it.toResponse() }


    @Transactional
    fun getChannel(channelId: Long): ChannelResponse =

        channelRepository.findByIdOrNull(channelId)
            ?.toResponse()
            ?: throw ModelNotFoundException("Channel", channelId)


    @Cacheable(value = ["TopPosts"], key = "#channelId", sync = true)
    fun getChannelTopPostList(channelId: Long): List<PostResponse> =

        channelRepository.findTopPosts(channelId).map { it.toResponse() }
}