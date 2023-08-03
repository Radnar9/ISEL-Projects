package pt.isel.daw.project.service
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.springframework.stereotype.Service
import pt.isel.daw.project.dao.IssueDao
import pt.isel.daw.project.exception.InternalServerException
import pt.isel.daw.project.exception.NotFoundException
import pt.isel.daw.project.exception.Error
import pt.isel.daw.project.exception.Error.NotFound.Message.RESOURCE_NOT_FOUND
import pt.isel.daw.project.model.PaginationDto
import pt.isel.daw.project.model.issue.CreateIssueEntity
import pt.isel.daw.project.model.issue.UpdateIssueEntity
import pt.isel.daw.project.model.issue.IssueItemDto
import pt.isel.daw.project.model.issue.IssuesDto
import pt.isel.daw.project.model.issue.IssueDto
import pt.isel.daw.project.model.toEntity
import pt.isel.daw.project.model.user.UserDto
import pt.isel.daw.project.utils.deserializeJsonTo
import pt.isel.daw.project.utils.Validator
@Service
class IssueService(jdbi: Jdbi) {
    private val issueDao = jdbi.onDemand<IssueDao>()
    fun getIssues(projectId: Int, user: UserDto, pagination: PaginationDto): IssuesDto {
        return issueDao.getIssues(projectId, user.id, pagination.toEntity()).deserializeJsonTo(IssuesDto::class.java)
    }

    fun getIssue(projectId: Int, issueId: Int, user: UserDto): IssueDto {
        val issueRep = issueDao.getIssue(projectId, issueId, user.id)
            ?: throw NotFoundException(RESOURCE_NOT_FOUND)
        return issueRep.deserializeJsonTo(IssueDto::class.java)
    }
    fun createIssue(projectId: Int, issue: CreateIssueEntity, user: UserDto): IssueDto {
        val issueRep = issueDao.createIssue(projectId, issue, user.id).getString("issueRep")
            ?: throw InternalServerException(
                Error.InternalServerError.Message.INTERNAL_ERROR,
                Error.InternalServerError.Message.DB_CREATION_ERROR, issue)
        return issueRep.deserializeJsonTo(IssueDto::class.java)
    }
    fun updateIssue(projectId: Int, issueId: Int, issue: UpdateIssueEntity, user: UserDto): IssueItemDto {
        Validator.Issue.checkIfAllParametersAreNull(issue)
        issue.id = issueId
        val issueRep = issueDao.updateIssue(projectId, issue, user.id).getString("updatedIssue")
            ?: throw NotFoundException(RESOURCE_NOT_FOUND)
        return issueRep.deserializeJsonTo(IssueItemDto::class.java)
    }

    fun deleteIssue(projectId: Int, issueId: Int, user: UserDto): IssueItemDto {
        val issueRep = issueDao.deleteIssue(projectId, issueId, user.id).getString("deletedIssue")
            ?: throw NotFoundException(RESOURCE_NOT_FOUND)
        return issueRep.deserializeJsonTo(IssueItemDto::class.java)
    }
}