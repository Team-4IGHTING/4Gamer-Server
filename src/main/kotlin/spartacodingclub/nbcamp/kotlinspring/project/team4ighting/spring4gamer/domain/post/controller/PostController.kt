package spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.dto.request.CreatePostRequest
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.dto.response.PostResponse
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.dto.response.PostSimplifiedResponse
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.dto.request.UpdatePostRequest
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.dto.response.PostReportResponse
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.dto.response.PostTagResponse
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.service.PostService
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.infra.security.MemberPrincipal

@RestController
@RequestMapping("/api/v1/channels/{channelId}/boards/{boardId}/posts")
class PostController(
    private val postService: PostService
) {

    @PostMapping
    fun createPost(
        @AuthenticationPrincipal member: MemberPrincipal,
        @PathVariable channelId: Long,
        @PathVariable boardId: Long,
        @RequestBody @Valid request: CreatePostRequest
    ): ResponseEntity<PostResponse> =

        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(postService.createPost(channelId, boardId, request, member.id))


    @GetMapping
    fun getPostList(
        @PathVariable channelId: Long,
        @PathVariable boardId: Long,
        @PageableDefault(
            page = 0, size = 10,
            sort = ["createdAt"], direction = Sort.Direction.DESC
        ) pageable: Pageable
    ): ResponseEntity<Page<PostSimplifiedResponse>> =

        ResponseEntity
            .status(HttpStatus.OK)
            .body(postService.getPostList(channelId, boardId, pageable))


    @GetMapping("/{postId}")
    fun getPost(
        @PathVariable channelId: Long,
        @PathVariable boardId: Long,
        @PathVariable postId: Long,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<PostResponse> =

        ResponseEntity
            .status(HttpStatus.OK)
            .body(postService.getPost(channelId, boardId, postId, request, response))


    @GetMapping("/{postId}/tags")
    fun getTagsInPost(
        @PathVariable channelId: Long,
        @PathVariable boardId: Long,
        @PathVariable postId: Long,
    ): ResponseEntity<List<PostTagResponse>> =

        ResponseEntity
            .status(HttpStatus.OK)
            .body(postService.getTagsInPost(channelId, boardId, postId))


    @PutMapping("/{postId}")
    fun updatePost(
        @AuthenticationPrincipal member: MemberPrincipal,
        @PathVariable channelId: Long,
        @PathVariable boardId: Long,
        @PathVariable postId: Long,
        @RequestBody @Valid request: UpdatePostRequest
    ): ResponseEntity<PostResponse> =

        ResponseEntity
            .status(HttpStatus.OK)
            .body(postService.updatePost(channelId, boardId, postId, request, member.id))


    @DeleteMapping("/{postId}")
    fun deletePost(
        @AuthenticationPrincipal member: MemberPrincipal,
        @PathVariable channelId: Long,
        @PathVariable boardId: Long,
        @PathVariable postId: Long
    ): ResponseEntity<Unit> =

        ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .body(postService.deletePost(channelId, boardId, postId, member.id))


    /*
     * 반응 관련
     */

    @PutMapping("/{postId}/reaction")
    fun addReaction(
        @AuthenticationPrincipal member: MemberPrincipal,
        @PathVariable channelId: Long,
        @PathVariable boardId: Long,
        @PathVariable postId: Long,
        @RequestParam(name = "is-upvoting", required = true) isUpvoting: Boolean
    ): ResponseEntity<Unit> =

        ResponseEntity
            .status(HttpStatus.OK)
            .body(postService.addReaction(channelId, boardId, postId, member.id, isUpvoting))


    @DeleteMapping("/{postId}/reaction")
    fun deleteREaction(
        @AuthenticationPrincipal member: MemberPrincipal,
        @PathVariable channelId: Long,
        @PathVariable boardId: Long,
        @PathVariable postId: Long
    ): ResponseEntity<Unit> =

        ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .body(postService.deleteReaction(channelId, boardId, postId, member.id))


    @PostMapping("/{postId}/report")
    fun reportPost(
        @AuthenticationPrincipal member: MemberPrincipal,
        @PathVariable channelId: Long,
        @PathVariable boardId: Long,
        @PathVariable postId: Long,
        @RequestBody reason: String
    ): ResponseEntity<PostReportResponse> =

        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(postService.reportPost(channelId, boardId, postId, reason, member.id))
}