package pt.isel.daw.project.service

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.springframework.stereotype.Service
import pt.isel.daw.project.dao.CommentDao
import pt.isel.daw.project.exception.InternalServerException
import pt.isel.daw.project.exception.NotFoundException
import pt.isel.daw.project.exception.Error
import pt.isel.daw.project.exception.Error.ArchivedIssue.Message.CREATE_COMMENT
import pt.isel.daw.project.exception.Error.ArchivedIssue.Message.DELETE_COMMENT
import pt.isel.daw.project.exception.Error.ArchivedIssue.Message.UPDATE_COMMENT
import pt.isel.daw.project.exception.Error.NotFound.Message.RESOURCE_NOT_FOUND
import pt.isel.daw.project.model.PaginationDto
import pt.isel.daw.project.model.comment.CreateCommentEntity
import pt.isel.daw.project.model.comment.UpdateCommentEntity
import pt.isel.daw.project.model.comment.CommentsDto
import pt.isel.daw.project.model.comment.CommentDto
import pt.isel.daw.project.model.toEntity
import pt.isel.daw.project.model.user.UserDto
import pt.isel.daw.project.utils.Validator
import pt.isel.daw.project.utils.deserializeJsonTo

@Service
class CommentService(jdbi: Jdbi) {

    private val commentDao = jdbi.onDemand<CommentDao>()

    fun getComments(projectId: Int, issueId: Int, user: UserDto, pagination: PaginationDto): CommentsDto {
        return commentDao.getComments(projectId, issueId, user.id, pagination.toEntity()).deserializeJsonTo(CommentsDto::class.java)
    }

    fun getComment(projectId: Int, issueId: Int, commentId: Int, user: UserDto): CommentDto {
        val commentRep = commentDao.getComment(projectId, issueId, commentId, user.id)
            ?: throw NotFoundException(RESOURCE_NOT_FOUND)
        return commentRep.deserializeJsonTo(CommentDto::class.java)
    }

    fun createComment(projectId: Int, issueId:Int, comment: CreateCommentEntity, user: UserDto): CommentDto {
        val commentRep = commentDao.createComment(projectId, issueId, comment, user.id).getString("createdComment")
            ?: throw InternalServerException(
                Error.InternalServerError.Message.INTERNAL_ERROR,
                Error.InternalServerError.Message.DB_CREATION_ERROR, comment)
        val commentDto = commentRep.deserializeJsonTo(CommentDto::class.java)
        Validator.Comment.checkIfIssueIsArchived(commentDto, CREATE_COMMENT)
        return commentDto
    }

    fun updateComment(projectId: Int, issueId: Int, commentId: Int, comment: UpdateCommentEntity, user: UserDto): CommentDto {
        comment.id = issueId
        val commentRep = commentDao.updateComment(projectId, issueId, commentId, comment, user.id).getString("updatedComment")
            ?: throw NotFoundException(RESOURCE_NOT_FOUND)
        val commentDto = commentRep.deserializeJsonTo(CommentDto::class.java)
        Validator.Comment.checkIfIssueIsArchived(commentDto, UPDATE_COMMENT)
        return commentDto
    }

    fun deleteComment(projectId: Int, issueId: Int, commentId: Int, user: UserDto): CommentDto {
        val commentRep = commentDao.deleteComment(projectId, issueId, commentId, user.id).getString("deletedComment")
            ?: throw NotFoundException(RESOURCE_NOT_FOUND)
        val commentDto = commentRep.deserializeJsonTo(CommentDto::class.java)
        Validator.Comment.checkIfIssueIsArchived(commentDto, DELETE_COMMENT)
        return commentDto
    }
}