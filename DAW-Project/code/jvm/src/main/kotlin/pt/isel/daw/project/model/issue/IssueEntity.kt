package pt.isel.daw.project.model.issue

data class CreateIssueEntity(
    val name: String,
    val description: String,
    val labels: Array<Int>? = null,
)
data class UpdateIssueEntity(
    var id: Int?,
    val name: String?,
    val description: String?,
    val state: Int?
)