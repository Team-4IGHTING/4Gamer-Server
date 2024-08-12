package spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.gamereview.repository

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.repository.findByIdOrNull
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.gamereview.dto.request.CreateGameReviewRequest
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.gamereview.model.GameReview
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.member.model.Member
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.member.repository.MemberRepository
import java.util.*

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GameReviewRepositoryTests @Autowired constructor(
    private val gameReviewRepository: GameReviewRepository,
    private val memberRepository: MemberRepository
) {

    @BeforeEach
    fun setUp() {
        memberRepository.saveAndFlush(MEMBER)
        gameReviewRepository.saveAllAndFlush(DEFAULT_GAME_REVIEW_LIST)

        val gameReview1 = gameReviewRepository.findByIdOrNull(1L)!!
        gameReview1.upvotes = 10

        val gameReview2 = gameReviewRepository.findByIdOrNull(2L)!!
        gameReview2.upvotes = 5

        val gameReview3 = gameReviewRepository.findByIdOrNull(3L)!!
        gameReview3.upvotes = 1
    }


    @Test
    fun `게임리뷰 인기 게시글 조회`() {
        val result = gameReviewRepository.findTopGameReviews()

        result.size shouldBe 3
        result[0].upvotes shouldBe 10
        result[1].upvotes shouldBe 5
        result[2].upvotes shouldBe 1
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
            ), GameReview.from(
                request = CreateGameReviewRequest(
                    gameTitle = "Title...6",
                    point = 10,
                    description = "Description...6",
                ),
                memberId = MEMBER_ID
            ), GameReview.from(
                request = CreateGameReviewRequest(
                    gameTitle = "Title...7",
                    point = 10,
                    description = "Description...7",
                ),
                memberId = MEMBER_ID
            ), GameReview.from(
                request = CreateGameReviewRequest(
                    gameTitle = "Title...8",
                    point = 10,
                    description = "Description...8",
                ),
                memberId = MEMBER_ID
            ), GameReview.from(
                request = CreateGameReviewRequest(
                    gameTitle = "Title...9",
                    point = 10,
                    description = "Description...9",
                ),
                memberId = MEMBER_ID
            ), GameReview.from(
                request = CreateGameReviewRequest(
                    gameTitle = "Title...10",
                    point = 10,
                    description = "Description...10",
                ),
                memberId = MEMBER_ID
            )
        )
    }
}