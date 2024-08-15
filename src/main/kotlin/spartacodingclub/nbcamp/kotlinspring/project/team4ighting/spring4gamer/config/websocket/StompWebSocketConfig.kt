package spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.config.websocket

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.notification.type.PublishType
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.notification.dto.MessageSubResponse
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.notification.service.NotificationService
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.notification.service.RedisPublisher
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.infra.security.jwt.JwtHelper
import java.time.ZonedDateTime

@Configuration
@EnableWebSocketMessageBroker // WebSocket 메시지 처리 활성화(stomp)
class StompWebSocketConfig(
    private val jwtHelper: JwtHelper,
    private val notificationService: NotificationService,
    private val publisher: RedisPublisher
) : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/sub") // 인메모리 메시지 브로커를 설정
        registry.setApplicationDestinationPrefixes("/pub") // 수신 메시지를 @MessageMapping 메서드에 매핑
    }


    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry
            .addEndpoint("/ws") // http://localhost:8080/ws
            .setAllowedOriginPatterns("*")
            .withSockJS()
    }


    // 연결 요청 시 STOMP 헤더에 JWT 토큰이 있는지 체크
    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(
            object : ChannelInterceptor {

                override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
                    val accessor = StompHeaderAccessor.wrap(message)

                    if (StompCommand.CONNECT == accessor.command) {
                        val token = accessor.getFirstNativeHeader("token")
                            ?: throw IllegalArgumentException("Invalid token")

                        jwtHelper.validateToken(token)
                            .onSuccess { claims -> // 토큰 인증 후 헤더에 roomId 유무에 따라 알림인지 채팅인지 구분

                                val subjectId = claims.payload.subject

                                accessor.getFirstNativeHeader("roomId")?.let { roomId -> // roomId가 있다면 채팅 요청이므로
                                    accessor.getFirstNativeHeader("targetId")?.let { targetId -> // 헤더에서 targetId를 확인
                                        publisher.publish( // 수신자(targetId)에게 알림 전송
                                            MessageSubResponse(
                                                type = PublishType.NOTIFICATION,
                                                subjectId = subjectId,
                                                targetId = targetId,
                                                message = "${subjectId}님이 채팅 요청을 보냈습니다.",
                                                createdAt = ZonedDateTime.now(),
                                                roomId = roomId
                                            )
                                        )
                                    }

                                } ?: run {  // roomId가 없다면 알림 구독 요청
                                    accessor.sessionId?.let { id -> notificationService.save(subjectId) }
                                }
                            }
                            .onFailure {
                                throw IllegalArgumentException("Invalid token")
                            }
                    }

                    return message
                }
            }
        )
    }
}