package spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.chatting.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.notification.dto.MessageSubResponse
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.notification.service.NotificationService
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.notification.service.RedisPublisher

@RestController
class ChatController(
    private val notificationService: NotificationService,
    private val redisPublisher: RedisPublisher
) {

    // 현재 접속중인 유저인지 체크
    @GetMapping("/is-connecting/{memberId}")
    fun isConnecting(@PathVariable memberId: String): ResponseEntity<Boolean> =

        ResponseEntity
            .status(HttpStatus.OK)
            .body(notificationService.isExistsMember(memberId))


    @MessageMapping("/chat")
    fun sendMessage(message: MessageSubResponse) =

        redisPublisher.publish(message)
}