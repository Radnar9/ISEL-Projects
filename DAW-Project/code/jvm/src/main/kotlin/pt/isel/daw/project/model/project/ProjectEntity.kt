package pt.isel.daw.project.model.project

data class CreateProjectEntity(
    val name: String,
    val description: String,
    val labels: Array<String>,
    val states: Array<String>,
    val initialState: String,
    val statesTransitions: Array<String>,
)

data class UpdateProjectEntity(
    var id: Int?,
    val name: String?,
    val description: String?,
    val labels: Array<String>?,
)