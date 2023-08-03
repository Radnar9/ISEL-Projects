package pt.isel.daw.project.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pt.isel.daw.project.model.*
import pt.isel.daw.project.model.project.*
import pt.isel.daw.project.service.ProjectService
import pt.isel.daw.project.service.UserService
import java.util.*

@RestController
@CrossOrigin(origins = ["http://localhost:3000"], allowedHeaders = ["*"], allowCredentials = "true")
class ProjectController(private val service: ProjectService, private val userService: UserService) {
    @GetMapping(Uris.Projects.PATH)
    fun getProjects(@CookieValue(value = "SESSION_KEY") userToken: String, pagination: PaginationDto): DawJsonModel {
        val user = userService.getUserInfo(UUID.fromString(userToken))
        val projects = service.getProjects(user, pagination)
        return Representations.Projects.getProjectsRepresentation(
            projects,
            user,
            CollectionModel(pagination.page, PaginationDto.DEFAULT_LIMIT, projects.projectsCollectionSize)
        )
    }

    @PostMapping(Uris.Projects.PATH)
    fun createProject(@CookieValue(value = "SESSION_KEY") userToken: String, @RequestBody project: CreateProjectEntity): ResponseEntity<Any> {
        val user = userService.getUserInfo(UUID.fromString(userToken))
        return Representations.Projects.createProjectRepresentation(service.createProject(project, user), user)
    }

    @GetMapping(Uris.Projects.SINGLE_PATH)
    fun getProject(@CookieValue(value = "SESSION_KEY") userToken: String, @PathVariable projectId: Int): DawJsonModel {
        val user = userService.getUserInfo(UUID.fromString(userToken))
        return Representations.Projects.getProjectRepresentation(service.getProject(projectId, user), user)
    }

    @PutMapping(Uris.Projects.SINGLE_PATH)
    fun updateProject(
        @CookieValue(value = "SESSION_KEY") userToken: String,
        @PathVariable projectId: Int,
        @RequestBody project: UpdateProjectEntity
    ): DawJsonModel {
        val user = userService.getUserInfo(UUID.fromString(userToken))
        return Representations.Projects.updateProjectRepresentation(
            service.updateProject(projectId, project, user),
            user
        )
    }

    @DeleteMapping(Uris.Projects.SINGLE_PATH)
        fun deleteProject(@CookieValue(value = "SESSION_KEY") userToken: String, @PathVariable projectId: Int) {
        val user = userService.getUserInfo(UUID.fromString(userToken))
        Representations.Projects.deleteProjectRepresentation(service.deleteProject(projectId, user), user)
    }

    @PutMapping(Uris.Projects.LABELS_PATH)
    fun addProjectLabels(
        @CookieValue(value = "SESSION_KEY") userId: String,
        @PathVariable projectId: Int,
        @RequestBody updateProjectEntity: UpdateProjectEntity
    ): DawJsonModel {
        val user = userService.getUserInfo(UUID.fromString(userId))
        return Representations.Projects.addProjectLabelsRepresentation(
            service.addLabelsToProject(projectId, updateProjectEntity, user),
            user
        )
    }
}