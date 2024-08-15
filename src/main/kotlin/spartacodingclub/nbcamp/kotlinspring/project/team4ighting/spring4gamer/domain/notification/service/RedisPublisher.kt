package spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.notification.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Service
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.notification.dto.MessageSubResponse
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.notification.type.PublishType

@Service
class RedisPublisher(
    @Qualifier("notificationTopic") private val notificationTopic: ChannelTopic,
    @Qualifier("chatTopic") private val chatTopic: ChannelTopic,
    private val redisTemplate: RedisTemplate<String, Any>
) {

    fun publish(message: MessageSubResponse) {

        when (message.type) {
            PublishType.NOTIFICATION ->
                redisTemplate.convertAndSend(notificationTopic.topic, message)

            PublishType.CHAT ->
                redisTemplate.convertAndSend(chatTopic.topic, message)

            PublishType.ENTER ->
                redisTemplate.convertAndSend(chatTopic.topic, message)

            PublishType.EXIT ->
                redisTemplate.convertAndSend(chatTopic.topic, message)
        }
    }
}