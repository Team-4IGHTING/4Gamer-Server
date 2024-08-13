package spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.channel.model

import jakarta.persistence.*
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.common.type.BaseTimeEntity
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.channel.dto.request.CreateChannelRequest
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.channel.dto.response.ChannelResponse
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.channeladmin.dto.request.UpdateChannelRequest
import java.util.*

@Entity
@Table(name = "channel")
class Channel private constructor(
    title: String,
    gameTitle: String,
    introduction: String,
    alias: String,
    admin: UUID
) : BaseTimeEntity() {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(name = "title", nullable = false)
    var title: String = title
        private set

    @Column(name = "game_title", nullable = false, unique = true)
    var gameTitle: String = gameTitle
        private set

    @Column(name = "introduction", nullable = false)
    var introduction: String = introduction
        private set

    @Column(name = "alias", nullable = false)
    var alias: String = alias
        private set

    @Column(name = "admin", nullable = false)
    var admin: UUID = admin
        private set


    companion object {

        fun from(
            request: CreateChannelRequest,
            admin: UUID
        ): Channel =

            Channel(
                title = request.title,
                gameTitle = request.gameTitle,
                introduction = request.introduction,
                alias = request.alias,
                admin = admin
            )
    }


    fun update(updateChannelRequest: UpdateChannelRequest) {

        title = updateChannelRequest.title
        introduction = updateChannelRequest.introduction
        preUpdate()
    }
}

fun Channel.toResponse(): ChannelResponse =

    ChannelResponse(
        id = id!!,
        title = title,
        gameTitle = gameTitle,
        introduction = introduction,
        admin = admin,
        alias = alias
    )