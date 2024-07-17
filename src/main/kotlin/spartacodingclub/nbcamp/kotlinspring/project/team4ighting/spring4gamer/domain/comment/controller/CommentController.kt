package spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.comment.controller

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.comment.dto.CommentResponse
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.comment.dto.CreateCommentRequest
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.comment.service.CommentService

@RestController
@RequestMapping("/api/v1/channels/{channelId}/boards/{boardId}/posts/{postId}/comments")
class CommentController(
    private val commentService: CommentService
) {

    @PostMapping
    fun createComment(
//        @AuthenticationPrincipal memberId: Long, // TODO: 로그인 구현 후 사용 예정
        @PathVariable("channelId") channelId: Long,
        @PathVariable("boardId") boardId: Long,
        @PathVariable("postId") postId: Long,
        @RequestBody request: CreateCommentRequest
    ): ResponseEntity<CommentResponse> {

        // TODO: 로그인 구현 후 임시 유저 ID인 1L 제거
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(commentService.createComment(channelId, boardId, postId, request, 1L))
    }

    @GetMapping
    fun getCommentList(
        @PathVariable("channelId") channelId: Long,
        @PathVariable("boardId") boardId: Long,
        @PathVariable("postId") postId: Long,
        @PageableDefault(page = 0, size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<Page<CommentResponse>> {

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(commentService.getCommentList(channelId, boardId, postId, pageable))
    }
    // TODO: 수정
    // TODO: 삭제
}