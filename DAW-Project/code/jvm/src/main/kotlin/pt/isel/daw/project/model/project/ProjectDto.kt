package pt.isel.daw.project.model.project

import pt.isel.daw.project.model.issue.IssueItemDto
import pt.isel.daw.project.model.label.LabelDto
import pt.isel.daw.project.model.state.StateDto

data class ProjectsDto(
    val projects: List<ProjectItemDto>?,
    val projectsCollectionSize: Int,
)

data class ProjectItemDto(
    val id: Int,
    val name: String,
    val description: String
)

data class ProjectDto(
    val id: Int,
    val name: String,
    val description: String,
    val labels: List<LabelDto>?,
    val states: List<StateDto>?,
    val initialState: StateDto?,
    val statesTransitions: List<StateDto>?,
    val issues: List<IssueItemDto>?,
    val issuesCollectionSize: Int?,
)

data class ProjectLabelsDto(
    val id: Int,
    val name: String,
    val description: String,
    val labels: List<LabelDto>,
)

fun ProjectDto.removeIssues() =
    ProjectDto(id, name, description, labels, states, initialState, statesTransitions, null, null)