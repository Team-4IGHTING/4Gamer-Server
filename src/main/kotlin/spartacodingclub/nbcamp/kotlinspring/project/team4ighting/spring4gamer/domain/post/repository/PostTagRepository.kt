package spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.model.PostTag
import spartacodingclub.nbcamp.kotlinspring.project.team4ighting.spring4gamer.domain.post.model.PostTagId

interface PostTagRepository : JpaRepository<PostTag, PostTagId> {

    @Query("select pt from PostTag pt join fetch pt.id.post where pt.id.post.id = :postId")
    fun findAllTagsByPostId(postId: Long): List<PostTag>

    @Query("select pt from PostTag pt join fetch pt.id.post where pt.id.post.id in :postIds")
    fun findAllByPostIdIn(postIds: List<Long>): List<PostTag>
}