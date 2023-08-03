package pt.isel.daw.project.model

import org.springframework.web.util.UriTemplate

object Uris {
    const val PATH = "/v1"

    private const val PAGINATION_PATH = "?page={pageIdx}"
    fun makePagination(page: Int, uri: String) =
        UriTemplate("$uri$PAGINATION_PATH").expand(mapOf("pageIdx" to page)).toString()

    object Users {
        const val LOGIN_PATH = "$PATH/login"
        const val LOGOUT_PATH = "$PATH/logout"
    }

    object Projects {
        const val PATH = "${Uris.PATH}/projects"
        const val SINGLE_PATH = "$PATH/{projectId}"
        const val LABELS_PATH = "$SINGLE_PATH/labels"

        private val SINGLE_TEMPLATE = UriTemplate(SINGLE_PATH)
        fun makeSingle(id: Int) = SINGLE_TEMPLATE.expand(mapOf("projectId" to id)).toString()

        private val LABELS_TEMPLATE = UriTemplate(LABELS_PATH)
        fun makeLabels(id: Int) = LABELS_TEMPLATE.expand(mapOf("projectId" to id)).toString()
    }

    object Issues {
        const val PATH = "${Projects.PATH}/{projectId}/issues"
        const val SINGLE_PATH = "$PATH/{issueId}"

        private val MULTIPLE_TEMPLATE = UriTemplate(PATH)
        fun makeMultiple(projectId: Int) = MULTIPLE_TEMPLATE.expand(mapOf("projectId" to projectId)).toString()

        private val SINGLE_TEMPLATE = UriTemplate(SINGLE_PATH)
        fun makeSingle(projectId: Int, issueId: Int) =
            SINGLE_TEMPLATE.expand(mapOf("projectId" to projectId, "issueId" to issueId)).toString()
    }

    object Comment {
        const val PATH = "${Issues.PATH}/{issueId}/comments"
        const val SINGLE_PATH = "$PATH/{commentId}"
        private val MULTIPLE_TEMPLATE = UriTemplate(PATH)
        fun makeMultiple(projectId: Int, issueId: Int) =
            MULTIPLE_TEMPLATE.expand(
                mapOf("projectId" to projectId, "issueId" to issueId)).toString()
        private val SINGLE_TEMPLATE = UriTemplate(SINGLE_PATH)
        fun makeSingle(projectId: Int, issueId: Int, commentId: Int) =
            SINGLE_TEMPLATE.expand(
                mapOf("projectId" to projectId, "issueId" to issueId, "commentId" to commentId)).toString()
    }

    object User {
        const val PATH = "${Uris.PATH}/user"
    }
}