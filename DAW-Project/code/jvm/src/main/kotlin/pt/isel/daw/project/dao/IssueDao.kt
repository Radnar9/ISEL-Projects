package pt.isel.daw.project.dao
import org.jdbi.v3.core.statement.OutParameters
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.customizer.OutParameter
import org.jdbi.v3.sqlobject.statement.SqlCall
import org.jdbi.v3.sqlobject.statement.SqlQuery
import pt.isel.daw.project.model.PaginationEntity
import pt.isel.daw.project.model.issue.CreateIssueEntity
import pt.isel.daw.project.model.issue.UpdateIssueEntity
import pt.isel.daw.project.model.Representations.Comments
import java.util.UUID

interface IssueDao {

    @SqlQuery("SELECT issues_representation(:projectId, :userId, :limit, :offset);")
    fun getIssues(projectId: Int, userId: UUID, @BindBean pagination: PaginationEntity): String

    @SqlQuery("SELECT issue_representation(:projectId, :issueId, :userId, ${Comments.DEFAULT_PAGE_SIZE}, null);")
    fun getIssue(projectId: Int, issueId: Int, userId: UUID): String?

    @SqlCall("CALL create_issue(:issueRep, :projectId, :userId, :name, :description, :labels);")
    @OutParameter(name = "issueRep", sqlType = java.sql.Types.OTHER)
    fun createIssue(projectId: Int, @BindBean issue: CreateIssueEntity, userId: UUID): OutParameters

    @SqlCall("CALL update_issue(:updatedIssue, :projectId, :id, :userId, :name, :description, :state);")
    @OutParameter(name = "updatedIssue", sqlType = java.sql.Types.OTHER)
    fun updateIssue(projectId: Int, @BindBean issue: UpdateIssueEntity, userId: UUID): OutParameters

    @SqlCall("CALL delete_issue(:deletedIssue, :projectId, :issueId, :userId);")
    @OutParameter(name = "deletedIssue", sqlType = java.sql.Types.OTHER)
    fun deleteIssue(projectId: Int, issueId: Int, userId: UUID): OutParameters
}