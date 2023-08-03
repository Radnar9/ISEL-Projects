package pt.isel.daw.project.model.comment

import java.sql.Timestamp

data class CommentsDto(
    val comments: List<CommentDto>?,
    val commentsCollectionSize: Int,
    val issueState: String,
)

data class CommentDto(
    val id: Int,
    val comment: String,
    val timestamp: Timestamp,
    val isArchived: Boolean? = null,
)