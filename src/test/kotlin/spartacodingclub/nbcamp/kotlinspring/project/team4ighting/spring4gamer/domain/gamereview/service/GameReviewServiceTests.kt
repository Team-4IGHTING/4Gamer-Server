package spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.gamereview.repository

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.gamereview.dto.request.CreateGameReviewRequest
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.gamereview.dto.request.UpdateGameReviewRequest
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.gamereview.dto.response.GameReviewResponse
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.gamereview.service.GameReviewService
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.infra.igdb.service.IgdbService
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class GameReviewServiceTests(
    @MockkBean
    val igdbService: IgdbService,
    @MockkBean
    val gameReviewService: GameReviewService
) : BehaviorSpec({

    val gameTitle = "Stardew Valley"
    val memberId = UUID.fromString("b3a77697-c0f6-4163-94e4-3a985c551989")

    val savedGameReview = GameReviewResponse(
        id = 1L,
        gameTitle = gameTitle,
        description = "Description...",
        point = 10,
        memberId = memberId,
        upvotes = 0,
        downvotes = 0,
        createdAt = ZonedDateTime.of(2024, 8, 15, 12, 12, 12, 12, ZoneId.of("Asia/Seoul")),
        updatedAt = ZonedDateTime.of(2024, 8, 15, 12, 12, 12, 12, ZoneId.of("Asia/Seoul")),
    )

    val updatedGameReview = GameReviewResponse(
        id = 1L,
        gameTitle = gameTitle,
        description = "Updated Description...",
        point = 5,
        memberId = memberId,
        upvotes = 0,
        downvotes = 0,
        createdAt = ZonedDateTime.of(2024, 8, 15, 12, 12, 12, 12, ZoneId.of("Asia/Seoul")),
        updatedAt = ZonedDateTime.of(2024, 8, 15, 12, 12, 12, 12, ZoneId.of("Asia/Seoul")),
    )


    every { igdbService.checkGamesName(any()) } returns true
    every { igdbService.searchGamesByName(any()) } returns ResponseEntity.status(HttpStatus.OK).body(gameTitle)
    every { gameReviewService.createGameReview(any(), any()) } returns savedGameReview
    every { gameReviewService.updateGameReview(any(), any(), any()) } returns updatedGameReview
    every { gameReviewService.deleteGameReview(any(), any()) } returns Unit


    Given("로그인한 유저가") {
        When("게임리뷰 등록 요청을 하면") {
            val result = gameReviewService.createGameReview(
                CreateGameReviewRequest(
                    gameTitle = gameTitle,
                    point = 10,
                    description = "Description...",
                ),
                memberId = UUID.fromString("b3a77697-c0f6-4163-94e4-3a985c551989")
            )

            Then("등록된 게임리뷰 정보가 반환된다.") {
                result.id shouldBe savedGameReview.id
                result.gameTitle shouldBe savedGameReview.gameTitle
                result.point shouldBe savedGameReview.point
                result.description shouldBe savedGameReview.description
            }
        }

        When("게임리뷰 수정 요청을 하면") {

            val result = gameReviewService.updateGameReview(
                gameReviewId = 1L,
                request = UpdateGameReviewRequest(
                    point = 5,
                    description = "Updated Description...",
                ),
                memberId = UUID.fromString("b3a77697-c0f6-4163-94e4-3a985c551989")
            )

            Then("수정된 게임리뷰 정보가 반환된다.") {
                result.id shouldBe updatedGameReview.id
                result.point shouldBe updatedGameReview.point
                result.description shouldBe updatedGameReview.description
            }
        }

        When("게임리뷰 삭제 요청을 하면") {
            val gameReviewId = 1L

            val result = gameReviewService.deleteGameReview(
                gameReviewId = gameReviewId,
                memberId = memberId
            )

            Then("게임리뷰가 삭제된다.") {
                result shouldBe Unit
            }
        }
    }
})