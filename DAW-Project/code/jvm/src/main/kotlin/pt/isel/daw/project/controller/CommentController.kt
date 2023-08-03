package pt.isel.daw.project.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pt.isel.daw.project.model.*
import pt.isel.daw.project.model.comment.CreateCommentEntity
import pt.isel.daw.project.model.comment.UpdateCommentEntity
import pt.isel.daw.project.service.CommentService
import pt.isel.daw.project.service.UserService
import java.util.*

@CrossOrigin(origins = ["http://localhost:3000"], allowedHeaders = ["*"], allowCredentials = "true")
@RestController
class CommentController(private val service: CommentService, private val userService: UserService) {

    @GetMapping(Uris.Comment.PATH)
    fun getComments(
        @CookieValue(value = "SESSION_KEY") userToken: String,
        @PathVariable projectId: Int,
        @PathVariable issueId: Int,
        pagination: PaginationDto
    ): DawJsonModel {
        val user = userService.getUserInfo(UUID.fromString(userToken))
        val comments = service.getComments(projectId, issueId, user, pagination)
        return Representations.Comments.getCommentsRepresentation(
            comments.comments,
            projectId,
            issueId,
            comments.issueState,
            CollectionModel(pagination.page, PaginationDto.DEFAULT_LIMIT, comments.commentsCollectionSize),
            null,
            user
        )
    }

    @GetMapping(Uris.Comment.SINGLE_PATH)
    fun getComment(
        @CookieValue(value = "SESSION_KEY") userToken: String,
        @PathVariable projectId: Int,
        @PathVariable issueId: Int,
        @PathVariable commentId: Int
    ): DawJsonModel {
        val user = userService.getUserInfo(UUID.fromString(userToken))
        return Representations.Comments.getCommentRepresentation(
            projectId,
            issueId,
            service.getComment(projectId, issueId, commentId, user),
            user)
    }

    @PostMapping(Uris.Comment.PATH)
    fun createComment(
        @CookieValue(value = "SESSION_KEY") userToken: String,
        @PathVariable projectId: Int,
        @PathVariable issueId: Int,
        @RequestBody comment: CreateCommentEntity
    ): ResponseEntity<Any> {
        val user = userService.getUserInfo(UUID.fromString(userToken))
        return Representations.Comments.createCommentRepresentation(
            projectId,
            issueId,
            service.createComment(projectId, issueId, comment, user),
            user)
    }

    @PutMapping(Uris.Comment.SINGLE_PATH)
    fun updateComment(
        @CookieValue(value = "SESSION_KEY") userToken: String,
        @PathVariable issueId: Int,
        @PathVariable projectId: Int,
        @PathVariable commentId: Int,
        @RequestBody comment: UpdateCommentEntity
    ): DawJsonModel {
        val user = userService.getUserInfo(UUID.fromString(userToken))
        return Representations.Comments.updateCommentRepresentation(
            projectId,
            issueId,
            service.updateComment(projectId, issueId, commentId, comment, user),
            user
        )
    }

    @DeleteMapping(Uris.Comment.SINGLE_PATH)
    fun deleteProject(
        @CookieValue(value = "SESSION_KEY") userToken: String,
        @PathVariable projectId: Int,
        @PathVariable issueId: Int,
        @PathVariable commentId: Int
    ): DawJsonModel {
        val user = userService.getUserInfo(UUID.fromString(userToken))
        return Representations.Comments.deleteCommentRepresentation(
            projectId,
            issueId,
            service.deleteComment(projectId, issueId, commentId, user),
            user
        )
    }
}