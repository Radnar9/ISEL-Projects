package pt.isel.daw.project.model.comment

data class CreateCommentEntity(
    val comment: String
)
data class UpdateCommentEntity(
    var id: Int?,
    val comment: String
)