package pt.isel.daw.project.model.issue

import pt.isel.daw.project.model.comment.CommentDto
import pt.isel.daw.project.model.label.LabelDto
import pt.isel.daw.project.model.state.StateDto
import java.sql.Timestamp

data class IssuesDto(
    val issues: List<IssueItemDto>?,
    val issuesCollectionSize: Int,
)

data class IssueItemDto(
    val id: Int,
    val name: String,
    val description: String,
    val labels: List<LabelDto>?,
    val state: StateDto,
)


data class IssueDto(
    val id: Int,
    val name: String,
    val description: String,
    val creationTimestamp: Timestamp,
    val closeTimestamp: Timestamp?,
    val labels: List<LabelDto>?,
    val state: StateDto,
    val possibleTransitions: List<StateDto>?,
    val comments: List<CommentDto>?,
    val commentsCollectionSize: Int?
)
fun IssueDto.removeComments() =
    IssueDto(id, name, description, creationTimestamp, closeTimestamp, labels, state, possibleTransitions, null, null)