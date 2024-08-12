package spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.infra

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.member.dto.response.OAuth2Response
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.member.dto.response.SigninResponse
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.member.model.Member
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.member.repository.MemberRepository
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.infra.security.jwt.JwtHelper


@Service
class OAuth2Service(
    private val memberRepository: MemberRepository,
    private val jwtHelper: JwtHelper,
    @Value("\${front.server}")
    private val front: String
) : DefaultOAuth2UserService() {

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {

        val oAuth2User = super.loadUser(userRequest)
        val oAuth2Response = OAuth2Response(oAuth2User.attributes)
        val username = oAuth2Response.provider + " " + oAuth2Response.providerId
        val existData = memberRepository.findByEmail(oAuth2Response.email)
        val role: String

        if (existData == null) {
            val member = Member.from(
                email = oAuth2Response.email,
                password = "",
                nickname = username
            )
            memberRepository.save(member)
            role = "ROLE_${member.role.name}"
        } else {
            role = "ROLE_${existData.role.name}"
        }

        return CustomOAuth2User(oAuth2Response, role)
    }


    fun googleSignin(authentication: Authentication): String {

        val oauthToken = authentication as OAuth2AuthenticationToken
        val internalToken = oauthToken.principal as CustomOAuth2User
        val member = memberRepository.findByEmail(internalToken.email())
            ?: throw IllegalArgumentException("회원정보를 찾을 수 없습니다.")
        val token = SigninResponse(
            accessToken = jwtHelper.generateAccessToken(
                subject = member.id.toString(),
                email = member.email,
                role = member.role.name
            )
        )
        return "${front}/login/google?token=${token.accessToken}"

    }
}
