package spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.util

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

class CookieUtil {

    companion object {

        fun generateMidnightExpiryCookie(
            postId: Long,
            oldCookie: Cookie?,
            response: HttpServletResponse
        ) {

            oldCookie?.let {
                it.value += "[${postId}]"
                it.maxAge = getRemainingTimeUntilMidnight()

                return response.addCookie(oldCookie)
            }

            val newCookieValue = "[${postId}]"
            val viewCountCookie = Cookie("viewCounts", newCookieValue)

            viewCountCookie.maxAge = getRemainingTimeUntilMidnight()
            viewCountCookie.path = "/"
            viewCountCookie.secure = true
            viewCountCookie.isHttpOnly = true

            return response.addCookie(viewCountCookie)
        }


        private fun getRemainingTimeUntilMidnight(): Int {

            val now = LocalDateTime.now()
            val midnight = LocalDateTime
                .of(now.toLocalDate(), LocalTime.MIDNIGHT)
                .plusDays(1)

            return Duration
                .between(now, midnight)
                .seconds
                .toInt()
        }
    }
}