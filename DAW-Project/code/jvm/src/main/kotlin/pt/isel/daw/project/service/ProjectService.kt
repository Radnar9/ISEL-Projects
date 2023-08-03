package pt.isel.daw.project.service

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pt.isel.daw.project.dao.ProjectDao
import pt.isel.daw.project.exception.*
import pt.isel.daw.project.exception.Error.InternalServerError.Message.DB_CREATION_ERROR
import pt.isel.daw.project.exception.Error.InternalServerError.Message.INTERNAL_ERROR
import pt.isel.daw.project.exception.Error.NotFound.Message.PROJECT_NOT_FOUND
import pt.isel.daw.project.model.PaginationDto
import pt.isel.daw.project.model.project.*
import pt.isel.daw.project.model.toEntity
import pt.isel.daw.project.model.user.UserDto
import pt.isel.daw.project.utils.deserializeJsonTo
import pt.isel.daw.project.utils.Validator

@Service
class ProjectService(jdbi: Jdbi) {

    private val projectDao = jdbi.onDemand<ProjectDao>()

    fun getProjects(user: UserDto, pagination: PaginationDto): ProjectsDto {
        return projectDao.getProjects(user.id, pagination.toEntity()).deserializeJsonTo(ProjectsDto::class.java)
    }

    fun createProject(project: CreateProjectEntity, user: UserDto): ProjectDto {
        Validator.Project.apply {
            checkIfStatesContainInitialState(project)
            checkTransitionsArrayParity(project)
            checkIfTransitionsBelongToStates(project)
            checkStatesClosedAndArchivedExistance(project)
        }

        val projectRep = projectDao.createProject(project, user.id).getString("projectRep")
            ?: throw InternalServerException(INTERNAL_ERROR, DB_CREATION_ERROR, project)
        return projectRep.deserializeJsonTo(ProjectDto::class.java)
    }

    fun getProject(projectId: Int, user: UserDto): ProjectDto {
        val project = projectDao.getProject(projectId, user.id)
            ?: throw NotFoundException(Error.makeMessage(PROJECT_NOT_FOUND, projectId))
        return project.deserializeJsonTo(ProjectDto::class.java)
    }

    fun updateProject(projectId: Int, project: UpdateProjectEntity, user: UserDto): ProjectItemDto {
        Validator.Project.checkIfBothParametersAreNull(project)
        project.id = projectId
        val updatedProject = projectDao.updateProject(project, user.id).getString("updatedProject")
            ?: throw NotFoundException(Error.makeMessage(PROJECT_NOT_FOUND, projectId))
        return updatedProject.deserializeJsonTo(ProjectItemDto::class.java)
    }

    fun deleteProject(projectId: Int, user: UserDto): ProjectItemDto {
        val deletedProject = projectDao.deleteProject(projectId, user.id).getString("deletedProject")
            ?: throw NotFoundException(Error.makeMessage(PROJECT_NOT_FOUND, projectId))
        return deletedProject.deserializeJsonTo(ProjectItemDto::class.java)
    }

    fun addLabelsToProject(projectId: Int, updateProjectEntity: UpdateProjectEntity, user: UserDto): ProjectLabelsDto {
        Validator.Project.checkLabelsExistance(updateProjectEntity.labels)
        updateProjectEntity.id = projectId
        val projectLabels = projectDao.addLabelsToProject(updateProjectEntity, user.id).getString("projectLabels")
            ?: throw NotFoundException(Error.makeMessage(PROJECT_NOT_FOUND, projectId))
        return projectLabels.deserializeJsonTo(ProjectLabelsDto::class.java)
    }
}