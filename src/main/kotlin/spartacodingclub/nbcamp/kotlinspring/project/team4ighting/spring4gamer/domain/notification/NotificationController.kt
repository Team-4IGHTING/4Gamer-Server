package spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.notification

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.web.bind.annotation.RestController
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.notification.dto.MessageSubResponse
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.notification.service.RedisPublisher

@RestController
class NotificationController(
    private val redisPublisher: RedisPublisher
) {

    @MessageMapping("/notification")
    fun sendMessage(message: MessageSubResponse) =

        redisPublisher.publish(message)
}