package pt.isel.daw.project.dao

import org.jdbi.v3.core.statement.OutParameters
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.customizer.OutParameter
import org.jdbi.v3.sqlobject.statement.SqlCall
import org.jdbi.v3.sqlobject.statement.SqlQuery
import pt.isel.daw.project.model.PaginationEntity
import pt.isel.daw.project.model.comment.CreateCommentEntity
import pt.isel.daw.project.model.comment.UpdateCommentEntity
import java.util.UUID


interface CommentDao {

    @SqlQuery("SELECT comments_representation(:projectId, :issueId, :userId, :limit, :offset);")
    fun getComments(projectId: Int, issueId: Int, userId: UUID, @BindBean pagination: PaginationEntity): String

    @SqlQuery("SELECT comment_representation(:projectId, :issueId, :commentId, :userId);")
    fun getComment(projectId: Int, issueId: Int, commentId: Int, userId: UUID): String?

    @SqlCall("CALL create_comment(:createdComment, :projectId, :issueId, :userId, :comment);")
    @OutParameter(name = "createdComment", sqlType = java.sql.Types.OTHER)
    fun createComment(projectId: Int, issueId: Int, @BindBean issue: CreateCommentEntity, userId: UUID): OutParameters

    @SqlCall("CALL update_comment(:updatedComment, :projectId,:issueId, :commentId, :userId, :comment);")
    @OutParameter(name = "updatedComment", sqlType = java.sql.Types.OTHER)
    fun updateComment(projectId: Int, issueId: Int, commentId: Int, @BindBean issue: UpdateCommentEntity, userId: UUID): OutParameters

    @SqlCall("CALL delete_comment(:deletedComment, :projectId, :issueId, :commentId, :userId);")
    @OutParameter(name = "deletedComment", sqlType = java.sql.Types.OTHER)
    fun deleteComment(projectId: Int, issueId: Int,  commentId: Int, userId: UUID): OutParameters
}