package spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.gamereview.repository

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.gamereview.dto.request.CreateGameReviewRequest
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.gamereview.model.GameReview
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.gamereview.model.GameReviewReaction
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.member.model.Member
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.member.repository.MemberRepository
import java.util.*

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GameReviewReactionRepositoryTests @Autowired constructor(
    private val gameReviewReactionRepository: GameReviewReactionRepository,
    private val gameReviewRepository: GameReviewRepository,
    private val memberRepository: MemberRepository
) {

    private lateinit var member: Member

    @BeforeEach
    fun setUp() {
        member = memberRepository.saveAndFlush(MEMBER)

        gameReviewRepository.saveAllAndFlush(DEFAULT_GAME_REVIEW_LIST)
        gameReviewReactionRepository.saveAllAndFlush(GAME_REVIEW_REACTION_LIST)
    }


    @Test
    fun `사용자의 추천, 비추천 이력 조회`() {
        val result = gameReviewReactionRepository.findByMemberId(member.id)

        result.size shouldBe 5
        result[0].isUpvoting shouldBe true
        result[1].isUpvoting shouldBe false
    }


    companion object {
        private val MEMBER = Member.from(
            email = "test1234@test.com",
            nickname = "tester",
            password = "123456789"
        )
        private val MEMBER_ID = UUID.fromString("b3a77697-c0f6-4163-94e4-3a985c551989")
        private val DEFAULT_GAME_REVIEW_LIST = listOf(
            GameReview.from(
                request = CreateGameReviewRequest(
                    gameTitle = "Title...1",
                    point = 10,
                    description = "Description...1",
                ),
                memberId = MEMBER_ID
            ),
            GameReview.from(
                request = CreateGameReviewRequest(
                    gameTitle = "Title...2",
                    point = 10,
                    description = "Description...2",
                ),
                memberId = MEMBER_ID
            ), GameReview.from(
                request = CreateGameReviewRequest(
                    gameTitle = "Title...3",
                    point = 10,
                    description = "Description...3",
                ),
                memberId = MEMBER_ID
            ), GameReview.from(
                request = CreateGameReviewRequest(
                    gameTitle = "Title...4",
                    point = 10,
                    description = "Description...4",
                ),
                memberId = MEMBER_ID
            ), GameReview.from(
                request = CreateGameReviewRequest(
                    gameTitle = "Title...5",
                    point = 10,
                    description = "Description...5",
                ),
                memberId = MEMBER_ID
            )
        )
        private val GAME_REVIEW_REACTION_LIST = listOf(
            GameReviewReaction.from(
                MEMBER,
                gameReview = DEFAULT_GAME_REVIEW_LIST[0],
                isUpvoting = true
            ),
            GameReviewReaction.from(
                MEMBER,
                gameReview = DEFAULT_GAME_REVIEW_LIST[1],
                isUpvoting = false
            ),
            GameReviewReaction.from(
                MEMBER,
                gameReview = DEFAULT_GAME_REVIEW_LIST[2],
                isUpvoting = true
            ),
            GameReviewReaction.from(
                MEMBER,
                gameReview = DEFAULT_GAME_REVIEW_LIST[3],
                isUpvoting = false
            ),
            GameReviewReaction.from(
                MEMBER,
                gameReview = DEFAULT_GAME_REVIEW_LIST[4],
                isUpvoting = true
            )
        )
    }
}