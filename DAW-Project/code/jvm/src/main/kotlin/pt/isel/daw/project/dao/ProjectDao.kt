package pt.isel.daw.project.dao

import org.jdbi.v3.core.statement.OutParameters
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.customizer.OutParameter
import org.jdbi.v3.sqlobject.statement.SqlCall
import org.jdbi.v3.sqlobject.statement.SqlQuery
import pt.isel.daw.project.model.PaginationEntity
import pt.isel.daw.project.model.Representations.Issues
import pt.isel.daw.project.model.project.*
import java.util.UUID

interface ProjectDao {

    @SqlQuery("SELECT projects_representation(:userId, :limit, :offset);")
    fun getProjects(userId: UUID, @BindBean pagination: PaginationEntity): String

    @SqlCall("CALL create_project(:name, :description, :userId, :labels, :states, :initialState, :statesTransitions, :projectRep);")
    @OutParameter(name = "projectRep", sqlType = java.sql.Types.OTHER)
    fun createProject(@BindBean project: CreateProjectEntity, userId: UUID): OutParameters

    @SqlQuery("SELECT project_representation(:id, :userId, ${Issues.DEFAULT_PAGE_SIZE}, null);")
    fun getProject(id: Int, userId: UUID): String?

    @SqlCall("CALL update_project(:id, :userId, :updatedProject, :name, :description);")
    @OutParameter(name = "updatedProject", sqlType = java.sql.Types.OTHER)
    fun updateProject(@BindBean project: UpdateProjectEntity, userId: UUID): OutParameters

    @SqlCall("CALL delete_project(:projectId, :userId, :deletedProject);")
    @OutParameter(name = "deletedProject", sqlType = java.sql.Types.OTHER)
    fun deleteProject(projectId: Int, userId: UUID): OutParameters

    @SqlCall("CALL set_project_labels(:id, :userId, :labels, :projectLabels)")
    @OutParameter(name = "projectLabels", sqlType = java.sql.Types.OTHER)
    fun addLabelsToProject(@BindBean updateProjectEntity: UpdateProjectEntity, userId: UUID): OutParameters
}