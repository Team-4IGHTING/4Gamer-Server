package spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.util

import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class RedissonLockUtility(
    private val redissonClient: RedissonClient
) {

    fun <T> runExclusive(lockKey: String, func: () -> T): T {

        val lock: RLock = redissonClient.getFairLock(lockKey)

        return kotlin.runCatching {
            if (lock.tryLock(10, 1, TimeUnit.SECONDS))
                func.invoke()
            else throw RuntimeException("Request timed out")
        }
            .onSuccess { lock.unlock() }
            .onFailure {
                if (lock.isHeldByCurrentThread) {
                    lock.unlock()
                }
            }
            .getOrThrow()
    }
}