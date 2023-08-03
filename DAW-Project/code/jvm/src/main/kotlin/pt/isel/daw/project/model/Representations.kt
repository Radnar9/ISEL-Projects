package pt.isel.daw.project.model

import org.springframework.http.*
import pt.isel.daw.project.model.Representations.Comments.getCommentsRepresentation
import pt.isel.daw.project.model.Representations.Issues.getIssuesRepresentation
import pt.isel.daw.project.model.comment.CommentDto
import pt.isel.daw.project.model.issue.IssueDto
import pt.isel.daw.project.model.issue.IssueItemDto
import pt.isel.daw.project.model.issue.removeComments
import pt.isel.daw.project.model.project.*
import pt.isel.daw.project.model.user.UserDto
import java.net.URI
import kotlin.reflect.KFunction2

object Representations {

    fun buildResponse(
        status: HttpStatus,
        headers: HttpHeaders = HttpHeaders(),
        representation: DawJsonModel,
    ): ResponseEntity<Any> {
        return ResponseEntity
            .status(status)
            .headers(headers)
            .body(representation)
    }

    fun setLocationHeader(uri: String): HttpHeaders {
        val headers = HttpHeaders()
        headers.location = URI(uri)
        return headers
    }

    object Projects {
        const val DEFAULT_PAGE_SIZE = 10

        object Actions {
            fun deleteProject(projectId: Int) = DawJsonModel.Action(
                name = "delete-project",
                title = "Delete project",
                method = HttpMethod.DELETE,
                href = Uris.Projects.makeSingle(projectId),
            )

            fun updateProject(projectId: Int) = DawJsonModel.Action(
                name = "update-project",
                title = "Update project",
                method = HttpMethod.PUT,
                href = Uris.Projects.makeSingle(projectId),
                type = MediaType.APPLICATION_JSON.toString(),
                properties = listOf(
                    DawJsonModel.Property(name = "name", type = "string", required = false),
                    DawJsonModel.Property(name = "description", type = "string", required = false),
                )
            )

            fun createProject() = DawJsonModel.Action(
                name = "create-project",
                title = "Create a project",
                method = HttpMethod.POST,
                href = Uris.Projects.PATH,
                type = MediaType.APPLICATION_JSON.toString(),
                properties = listOf(
                    DawJsonModel.Property(name = "name", type = "string"),
                    DawJsonModel.Property(name = "description", type = "string"),
                    DawJsonModel.Property(name = "labels", type = "array", itemsType = "string"),
                    DawJsonModel.Property(name = "states", type = "array", itemsType = "string"),
                    DawJsonModel.Property(name = "statesTransitions", type = "array", itemsType = "string"),
                    DawJsonModel.Property(name = "initialState", type = "string"),
                )
            )
        }

        private fun getProjectItem(project: ProjectItemDto, rel: List<String>?) = DawJsonModel(
            clazz = listOf(Classes.PROJECT.value),
            rel = rel,
            properties = project,
            links = listOf(Links.self(Uris.Projects.makeSingle(project.id))),
        )

        fun getProjectsRepresentation(projectsDto: ProjectsDto, user: UserDto, collection: CollectionModel) = DawJsonModel(
            clazz = listOf(Classes.PROJECT.value, Classes.COLLECTION.value),
            properties = collection,
            entities = mutableListOf<DawJsonModel>().apply {
                if (projectsDto.projects != null)
                    addAll(projectsDto.projects.map { getProjectItem(it, listOf(Rels.ITEM.value)) })
                add(User.getUserRepresentation(user, listOf(Rels.AUTHOR.value)))
            },
            actions = listOf(Actions.createProject()),
            links = buildCollectionLinks(
                collection,
                DEFAULT_PAGE_SIZE,
                Uris::makePagination,
                Uris.Projects.PATH,
            ),
        )

        fun createProjectRepresentation(project: ProjectDto, user: UserDto) = buildResponse(
            HttpStatus.CREATED,
            setLocationHeader(Uris.Projects.makeSingle(project.id)),
            DawJsonModel(
                clazz = listOf(Classes.PROJECT.value),
                properties = project.removeIssues(),
                entities = listOf(User.getUserRepresentation(user, listOf(Rels.AUTHOR.value))),
                links = listOf(Links.self(Uris.Projects.makeSingle(project.id))),
            )
        )

        fun getProjectRepresentation(project: ProjectDto, user: UserDto) = DawJsonModel(
            clazz = listOf(Classes.PROJECT.value),
            properties = project.removeIssues(),
            entities = listOf(
                getIssuesRepresentation(
                    project.issues,
                    project.id,
                    CollectionModel(
                        0,
                        if (Issues.DEFAULT_PAGE_SIZE < project.issuesCollectionSize!!)
                            Issues.DEFAULT_PAGE_SIZE
                        else
                            project.issuesCollectionSize
                        , project.issuesCollectionSize),
                    listOf(Rels.PROJECT_ISSUES.value)
                ),
                User.getUserRepresentation(user, listOf(Rels.AUTHOR.value)),
            ),
            actions = listOf(
                Actions.deleteProject(project.id),
                Actions.updateProject(project.id),
            ),
            links = listOf(Links.self(Uris.Projects.makeSingle(project.id))),
        )

        fun updateProjectRepresentation(project: ProjectItemDto, user: UserDto) = DawJsonModel(
            clazz = listOf(Classes.PROJECT.value),
            properties = project,
            entities = listOf(User.getUserRepresentation(user, listOf(Rels.AUTHOR.value))),
            links = listOf(Links.self(Uris.Projects.makeSingle(project.id))),
        )

        fun deleteProjectRepresentation(project: ProjectItemDto, user: UserDto) = DawJsonModel(
            clazz = listOf(Classes.PROJECT.value),
            properties = project,
            entities = listOf(User.getUserRepresentation(user, listOf(Rels.AUTHOR.value))),
            links = listOf(
                Links.self(Uris.Projects.makeSingle(project.id)),
                Links.projects(),
            ),
        )

        fun addProjectLabelsRepresentation(project: ProjectLabelsDto, user: UserDto) = DawJsonModel(
            clazz = listOf(Classes.PROJECT.value),
            properties = project,
            entities = listOf(User.getUserRepresentation(user, listOf(Rels.AUTHOR.value))),
            links = listOf(Links.self(Uris.Projects.makeSingle(project.id))),
        )
    }

    object Issues {
        const val DEFAULT_PAGE_SIZE = 10

        object Actions {
            fun createIssue(projectId: Int) = DawJsonModel.Action(
                name = "create-issue",
                title = "Create an issue",
                method = HttpMethod.POST,
                href = Uris.Issues.makeMultiple(projectId),
                type = MediaType.APPLICATION_JSON.toString(),
                properties = listOf(
                    DawJsonModel.Property(name = "name", type = "string"),
                    DawJsonModel.Property(name = "description", type = "string"),
                    DawJsonModel.Property(name = "labels", type = "array", itemsType = "number", required = false),
                )
            )

            fun deleteIssue(projectId: Int, issueId: Int) = DawJsonModel.Action(
                name = "delete-issue",
                title = "Delete issue",
                method = HttpMethod.DELETE,
                href = Uris.Issues.makeSingle(projectId, issueId)
            )
            fun updateIssue(projectId: Int, issueId: Int) = DawJsonModel.Action(
                name = "update-issue",
                title = "Update issue",
                method = HttpMethod.PUT,
                href = Uris.Issues.makeSingle(projectId, issueId),
                type = MediaType.APPLICATION_JSON.toString(),
                properties = listOf(
                    DawJsonModel.Property(name = "name", type = "string", required = false),
                    DawJsonModel.Property(name = "description", type = "string", required = false),
                    DawJsonModel.Property(name = "state", type = "number", required = false),
                )
            )
        }

        private fun getIssueItem(issue: IssueItemDto, projectId: Int, rel: List<String>?) = DawJsonModel(
            clazz = listOf(Classes.ISSUE.value),
            rel = rel,
            properties = issue,
            links = listOf(Links.self(Uris.Issues.makeSingle(projectId, issue.id)))
        )

        fun getIssuesRepresentation(
            issues: List<IssueItemDto>?,
            projectId: Int,
            collection: CollectionModel,
            rel: List<String>?,
            user: UserDto? = null,
        ) = DawJsonModel(
            clazz = listOf(Classes.ISSUE.value, Classes.COLLECTION.value),
            rel = rel,
            properties = collection,
            entities = mutableListOf<DawJsonModel>().apply {
                if (issues != null) addAll(issues.map { getIssueItem(it, projectId, listOf(Rels.ITEM.value)) })
                if (user != null) add(User.getUserRepresentation(user, listOf(Rels.AUTHOR.value)))
            },
            actions = listOf(Actions.createIssue(projectId)),
            links = buildCollectionLinks(
                collection,
                DEFAULT_PAGE_SIZE,
                Uris::makePagination,
                Uris.Issues.makeMultiple(projectId),
            ),
        )

        fun createIssueRepresentation(projectId: Int, issue: IssueDto, user: UserDto) = buildResponse(
            HttpStatus.CREATED,
            setLocationHeader(Uris.Issues.makeSingle(projectId, issue.id)),
            DawJsonModel(
                clazz = listOf(Classes.ISSUE.value),
                properties = issue.removeComments(),
                entities = listOf(User.getUserRepresentation(user, listOf(Rels.AUTHOR.value))),
                links = listOf(Links.self(Uris.Issues.makeSingle(projectId, issue.id)))
            )
        )

        fun getIssueRepresentation(projectId: Int, issue: IssueDto, user: UserDto) = DawJsonModel(
            clazz = listOf(Classes.ISSUE.value),
            properties = issue.removeComments(),
            entities = listOf(
                getCommentsRepresentation(
                    issue.comments,
                    projectId,
                    issue.id,
                    issue.state.name,
                    CollectionModel(
                        0,
                        if (Comments.DEFAULT_PAGE_SIZE < issue.commentsCollectionSize!!)
                            DEFAULT_PAGE_SIZE
                        else
                            issue.commentsCollectionSize
                        , issue.commentsCollectionSize),
                    listOf(Rels.ISSUE_COMMENTS.value),
                    user
                ),
                User.getUserRepresentation(user, listOf(Rels.AUTHOR.value)),
            ),
            actions = mutableListOf<DawJsonModel.Action>().apply {
                if (issue.state.name != "archived") {
                    add(Actions.deleteIssue(projectId, issue.id))
                    add(Actions.updateIssue(projectId, issue.id))
                }
            },
            links = listOf(Links.self(Uris.Issues.makeSingle(projectId, issue.id)))
        )

        fun updateIssueRepresentation(projectId: Int, issue: IssueItemDto, user: UserDto) = DawJsonModel(
            clazz = listOf(Classes.ISSUE.value),
            properties = issue,
            entities = listOf(User.getUserRepresentation(user, listOf(Rels.AUTHOR.value))),
            links = listOf(Links.self(Uris.Issues.makeSingle(projectId, issue.id)))
        )

        fun deleteIssueRepresentation(projectId: Int, issue: IssueItemDto, user: UserDto) = DawJsonModel(
            clazz = listOf(Classes.ISSUE.value),
            properties = issue,
            entities = listOf(User.getUserRepresentation(user, listOf(Rels.AUTHOR.value))),
            links = listOf(
                Links.self(Uris.Issues.makeSingle(projectId, issue.id)),
                Links.issues(projectId)
            )
        )
    }

    object User {
        fun getUserRepresentation(user: UserDto, rel: List<String>?) = DawJsonModel(
            clazz = listOf(Classes.USER.value),
            rel = rel,
            properties = user,
            links = listOf(Links.self(Uris.User.PATH)),
        )
    }

    object Comments {
        const val DEFAULT_PAGE_SIZE = 10

        object Actions {
            fun deleteComment(projectId: Int, issueId: Int, commentId: Int) = DawJsonModel.Action(
                name = "delete-comment",
                title = "Delete a comment",
                method = HttpMethod.DELETE,
                href = Uris.Comment.makeSingle(projectId, issueId, commentId)
            )

            fun updateComment(projectId: Int, issueId: Int, commentId: Int) = DawJsonModel.Action(
                name = "update-comment",
                title = "Update a comment",
                method = HttpMethod.PUT,
                href = Uris.Comment.makeSingle(projectId, issueId, commentId),
                type = MediaType.APPLICATION_JSON.toString(),
                properties = listOf(
                    DawJsonModel.Property(name = "comment", type = "string")
                )
            )

            fun createComment(projectId: Int, issueId: Int) = DawJsonModel.Action(
                name = "create-comment",
                title = "Create a comment",
                method = HttpMethod.POST,
                href = Uris.Comment.makeMultiple(projectId, issueId),
                type = MediaType.APPLICATION_JSON.toString(),
                properties = listOf(
                    DawJsonModel.Property(name = "comment", type = "string")
                )
            )
        }

        private fun getCommentItem(
            comment: CommentDto,
            projectId: Int,
            issueId: Int,
            issueState: String,
            rel: List<String>?,
            user: UserDto?
        ) = DawJsonModel(
            clazz = listOf(Classes.COMMENT.value),
            rel = rel,
            properties = comment,
            entities = mutableListOf<DawJsonModel>().apply {
                if (user != null) add(User.getUserRepresentation(user, listOf(Rels.AUTHOR.value)))
            },
            links = listOf(Links.self(Uris.Comment.makeSingle(projectId, issueId, comment.id))),
            actions = mutableListOf<DawJsonModel.Action>().apply {
                if (issueState != "archived") {
                    add(Actions.deleteComment(projectId, issueId, comment.id))
                    add(Actions.updateComment(projectId, issueId, comment.id))
                }
            }
        )

        fun getCommentsRepresentation(
            comments: List<CommentDto>?,
            projectId: Int,
            issueId: Int,
            issueState: String,
            collection: CollectionModel,
            rel: List<String>?,
            user: UserDto? = null
        ) = DawJsonModel(
            clazz = listOf(Classes.COMMENT.value, Classes.COLLECTION.value),
            rel = rel,
            properties = collection,
            entities = mutableListOf<DawJsonModel>().apply {
                if (comments != null) addAll(comments.map {
                    getCommentItem(it, projectId, issueId, issueState, listOf(Rels.ITEM.value), user)
                })
            },
            actions = mutableListOf<DawJsonModel.Action>().apply {
                if (issueState != "archived")
                    add(Actions.createComment(projectId, issueId))
            },
            links = buildCollectionLinks(
                collection,
                DEFAULT_PAGE_SIZE,
                Uris::makePagination,
                Uris.Comment.makeMultiple(projectId, issueId),
            ),
        )

        fun getCommentRepresentation(projectId: Int, issueId: Int, comment: CommentDto, user: UserDto) = DawJsonModel(
            clazz = listOf(Classes.COMMENT.value),
            properties = comment,
            entities = listOf(User.getUserRepresentation(user, listOf(Rels.AUTHOR.value))),
            actions = listOf(
                Actions.deleteComment(projectId, issueId, comment.id),
                Actions.updateComment(projectId, issueId, comment.id),
            ),
            links = listOf(Links.self(Uris.Comment.makeSingle(projectId, issueId, comment.id)))
        )

        fun createCommentRepresentation(projectId: Int, issueId: Int, comment: CommentDto, user: UserDto) = buildResponse(
            HttpStatus.CREATED,
            setLocationHeader(Uris.Comment.makeMultiple(projectId, issueId)),
            DawJsonModel(
                clazz = listOf(Classes.COMMENT.value),
                properties = comment,
                entities = listOf(User.getUserRepresentation(user, listOf(Rels.AUTHOR.value))),
                links = listOf(
                    Links.self(Uris.Comment.makeSingle(projectId, issueId, comment.id)),
                    Links.comments(projectId, issueId)
                )
            )
        )

        fun updateCommentRepresentation(projectId: Int, issueId: Int, comment: CommentDto, user: UserDto) = DawJsonModel(
            clazz = listOf(Classes.COMMENT.value),
            properties = comment,
            entities = listOf(User.getUserRepresentation(user, listOf(Rels.AUTHOR.value))),
            links = listOf(Links.self(Uris.Comment.makeSingle(projectId, issueId, comment.id)))
        )

        fun deleteCommentRepresentation(projectId: Int, issueId: Int, comment: CommentDto, user: UserDto) = DawJsonModel(
            clazz = listOf(Classes.COMMENT.value),
            properties = comment,
            entities = listOf(User.getUserRepresentation(user, listOf(Rels.AUTHOR.value))),
            links = listOf(
                Links.self(Uris.Comment.makeSingle(projectId, issueId, comment.id))
            )
        )
    }

    object Links {
        fun self(href: String) = DawJsonModel.Link(listOf(Rels.SELF.value), href)
        fun projects() = DawJsonModel.Link(listOf(Rels.PROJECTS.value), Uris.Projects.PATH)
        fun issues(projectId: Int) = DawJsonModel.Link(listOf(Rels.ISSUES.value), Uris.Issues.makeMultiple(projectId))
        fun comments(projectId: Int, issueId: Int) = DawJsonModel.Link(listOf(Rels.COMMENTS.value), Uris.Comment.makeMultiple(projectId, issueId))
    }

    enum class Classes(val value: String) {
        COLLECTION("collection"),
        PROJECT("project"),
        ISSUE("issue"),
        COMMENT("comment"),
        USER("user")
    }

    enum class Rels(val value: String) {
        SELF("self"),
        NEXT("next"),
        FIRST("first"),
        LAST("last"),
        PREV("prev"),
        ITEM("item"),
        PROJECT_ISSUES("project-issues"),
        ISSUE_COMMENTS("issue-comments"),
        PROJECTS("projects"),
        ISSUES("issues"),
        COMMENTS("comments"),
        AUTHOR("author"),
    }

    fun buildCollectionLinks(
        collection: CollectionModel,
        maxPageSize: Int,
        makeUri: KFunction2<Int, String, String>,
        uri: String,
        otherLinks: List<DawJsonModel.Link>? = null)
    : MutableList<DawJsonModel.Link> {
        val links = mutableListOf<DawJsonModel.Link>()
        links.add(DawJsonModel.Link(listOf(Rels.SELF.value), makeUri(collection.pageIndex, uri)))
        if (collection.pageIndex * maxPageSize + maxPageSize <= collection.collectionSize) {
            links.add(DawJsonModel.Link(listOf(Rels.NEXT.value), makeUri(collection.pageIndex + 1, uri)))
            links.add(DawJsonModel.Link(listOf(Rels.LAST.value), makeUri(collection.collectionSize / maxPageSize, uri)))
        }
        if (collection.pageIndex > 1) {
            links.add(DawJsonModel.Link(listOf(Rels.PREV.value), makeUri(collection.pageIndex - 1, uri)))
            links.add(DawJsonModel.Link(listOf(Rels.FIRST.value), makeUri(0, uri)))
        }
        if (otherLinks != null) {
            links.addAll(otherLinks)
        }
        return links
    }

}