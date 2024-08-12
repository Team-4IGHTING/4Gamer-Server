package spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.notification.service.RedisSubscriber

@Configuration
class RedisConfig(
    @Value("\${spring.data.redis.host}")
    private val host: String,
    @Value("\${spring.data.redis.port}")
    private val port: Int,
) {

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory =

        LettuceConnectionFactory(host, port)


    @Bean
    fun redisTemplate(): RedisTemplate<String, Any> {
        val redisTemplate = RedisTemplate<String, Any>()
        redisTemplate.connectionFactory = redisConnectionFactory()
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = GenericJackson2JsonRedisSerializer(
            jacksonObjectMapper()
                .registerModules(JavaTimeModule())
                .activateDefaultTyping(
                    BasicPolymorphicTypeValidator
                        .builder()
                        .allowIfSubType(Object::class.java)
                        .build(),
                    ObjectMapper.DefaultTyping.NON_FINAL
                )
        )

        return redisTemplate
    }


    @Bean
    fun notificationTopic(): ChannelTopic =

        ChannelTopic("notification")


    @Bean
    fun chatTopic(): ChannelTopic =

        ChannelTopic("chat")

    @Bean
    fun messageListenerAdapter(subscriber: RedisSubscriber): MessageListenerAdapter =

        MessageListenerAdapter(subscriber)


    @Bean
    fun redisMessageListenerContainer(
        connectionFactory: RedisConnectionFactory,
        listenerAdapter: MessageListenerAdapter
    ): RedisMessageListenerContainer {

        val container = RedisMessageListenerContainer()
        container.setConnectionFactory(connectionFactory)
        container.addMessageListener(listenerAdapter, ChannelTopic("notification"))
        container.addMessageListener(listenerAdapter, ChannelTopic("chat"))

        return container
    }
}