package pt.isel.daw.project.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pt.isel.daw.project.model.*
import pt.isel.daw.project.model.issue.UpdateIssueEntity
import pt.isel.daw.project.model.issue.CreateIssueEntity
import pt.isel.daw.project.service.IssueService
import pt.isel.daw.project.model.user.UserDto
import pt.isel.daw.project.service.UserService
import java.util.*

@RestController
@CrossOrigin(origins = ["http://localhost:3000"], allowedHeaders = ["*"], allowCredentials = "true")
class IssueController(private val service: IssueService, private val userService: UserService) {

    @GetMapping(Uris.Issues.PATH)
    fun getIssues(@CookieValue(value = "SESSION_KEY") userToken: String, @PathVariable projectId: Int, pagination: PaginationDto): DawJsonModel {
        val user = userService.getUserInfo(UUID.fromString(userToken))
        val issues = service.getIssues(projectId, user, pagination)
        return Representations.Issues.getIssuesRepresentation(
            issues.issues,
            projectId,
            CollectionModel(pagination.page, PaginationDto.DEFAULT_LIMIT, issues.issuesCollectionSize),
            null,
            user,
        )
    }

    @GetMapping(Uris.Issues.SINGLE_PATH)
    fun getIssue(@CookieValue(value = "SESSION_KEY") userToken: String, @PathVariable projectId: Int, @PathVariable issueId: Int): DawJsonModel {
        val user = userService.getUserInfo(UUID.fromString(userToken))
        return Representations.Issues.getIssueRepresentation(projectId, service.getIssue(projectId, issueId, user), user)
    }

    @PostMapping(Uris.Issues.PATH)
    fun createIssue(@CookieValue(value = "SESSION_KEY") userToken: String, @PathVariable projectId: Int, @RequestBody issue: CreateIssueEntity): ResponseEntity<Any> {
        val user = userService.getUserInfo(UUID.fromString(userToken))
        return Representations.Issues.createIssueRepresentation(projectId, service.createIssue(projectId, issue, user), user)
    }

    @PutMapping(Uris.Issues.SINGLE_PATH)
    fun updateIssue(
        @CookieValue(value = "SESSION_KEY") userToken: String,
        @PathVariable projectId: Int,
        @PathVariable issueId: Int,
        @RequestBody issue: UpdateIssueEntity
    ): DawJsonModel {
        val user = userService.getUserInfo(UUID.fromString(userToken))
        return Representations.Issues.updateIssueRepresentation(
            projectId,
            service.updateIssue(projectId, issueId, issue, user),
            user,
        )
    }

    @DeleteMapping(Uris.Issues.SINGLE_PATH)
    fun deleteIssue(@CookieValue(value = "SESSION_KEY") userToken: String, @PathVariable projectId: Int, @PathVariable issueId: Int): DawJsonModel {
        val user = userService.getUserInfo(UUID.fromString(userToken))
        return Representations.Issues.deleteIssueRepresentation(projectId, service.deleteIssue(projectId, issueId, user), user)
    }
}