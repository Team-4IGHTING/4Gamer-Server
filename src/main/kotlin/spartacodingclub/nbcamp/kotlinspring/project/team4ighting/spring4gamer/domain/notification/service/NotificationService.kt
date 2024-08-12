package spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.notification.service

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val redisTemplate: RedisTemplate<String, String>
) {

    private val NOTIFICATION_SESSIONS = "NOTIFICATION_SESSIONS"

    fun save(memberId: String) =

        redisTemplate.opsForList().rightPush(NOTIFICATION_SESSIONS, memberId)


    fun isExistsMember(memberId: String): Boolean =

        redisTemplate.opsForList().indexOf(NOTIFICATION_SESSIONS, memberId) != null


    fun delete(memberId: String) =

        redisTemplate.opsForList().remove("NOTIFICATION_SESSIONS", 0, memberId)
}