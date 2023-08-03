package pt.isel.daw.project.exception

import org.springframework.http.HttpStatus
import pt.isel.daw.project.auth.Authentication
import java.net.URI

object Error {
    object Unauthorized {
        val TYPE = URI("/probs/unauthorized")
        val STATUS = HttpStatus.UNAUTHORIZED
        const val WWW_AUTH_HEADER = "WWW-Authenticate"
        const val WWW_AUTH_HEADER_VALUE = "${Authentication.Basic.SCHEME} realm=\"daw-project\", charset=\"UTF-8\""

        object Message {
            const val REQUIRES_AUTH = "The resource requires authentication to access."
            const val INVALID_SCHEME = "Invalid authentication scheme."
            const val INVALID_TOKEN_FORMAT = "Invalid authentication token format."
            const val INVALID_CREDENTIALS = "Invalid authentication credentials."
        }
    }

    object NotFound {
        val TYPE = URI("/probs/not-found")
        val STATUS = HttpStatus.NOT_FOUND

        object Message {
            const val PROJECT_NOT_FOUND = "The project with the id {} was not found."
            const val RESOURCE_NOT_FOUND = "The resource was not found."
        }
    }

    object BadRequest {
        val TYPE = URI("/probs/validation-error")
        val STATUS = HttpStatus.BAD_REQUEST

        object Locations {
            const val PATH = "path"
            const val HEADERS = "headers"
            const val QUERY_STRING = "query_string"
            const val BODY = "body"
        }

        object Message {
            const val INVALID_REQ_PARAMS = "One or more request parameters are not valid."
            const val MISSING_MISMATCH_REQ_BODY = "One or more request body parameters are missing or have a type mismatch."
            const val TYPE_MISMATCH_REQ_PATH = "Type mismatch of request path parameter."
            const val TYPE_MISMATCH_REQ_QUERY = "Type mismatch of request query parameter."
            const val MUST_HAVE_TYPE = "The value must be of the {} type."
            object Project {
                const val INITIAL_STATE_NOT_FOUND = "The initial state doesn't exist in the states array."
                const val INVALID_TRANSITIONS_ARRAY_SIZE = "The size of the transitions array must be pair."
                const val STATE_IN_TRANSITIONS_NOT_FOUND = "At least one entry in transitions array doesn't belong in states array."
                const val UPDATE_NULL_PARAMS = "All updatable parameters can't be null."
                const val UPDATE_NULL_PARAMS_DETAIL = " Please insert one of the parameters in order to update."
                const val MISSING_LABELS_ARRAY = "You need to insert a labels array with at least one element."
                const val CLOSED_ARCH_STATES_MISSING = "The states 'closed' and 'archived' must always be included in the project states and transitions."
                const val CLOSED_ARCH_NO_TRANSITION = "A transition from the state closed to archived must always be included."
            }

            object Pagination {
                const val PAGE_TYPE_MISMATCH = "The value must be an integer >= 0"
            }
        }
    }

    object InternalServerError {
        val TYPE = URI("/probs/internal-server-error")
        val STATUS = HttpStatus.INTERNAL_SERVER_ERROR

        object Message {
            const val INTERNAL_ERROR = "An internal server error occurred."
            const val DB_CREATION_ERROR = "It was not possible to create the requested resource, please try again later."
            const val UNKNOWN_ERROR = "An error occurred, please verify if your passing the right values in the request."
        }
    }

    object MethodNotAllowed {
        val TYPE = URI("/probs/method-not-allowed")
        val STATUS = HttpStatus.METHOD_NOT_ALLOWED

        object Message {
            const val METHOD_NOT_ALLOWED = "The request method is not supported for the requested instance."
        }
    }

    object ArchivedIssue {
        val TYPE = URI("/probs/archived-issue")
        val STATUS = HttpStatus.CONFLICT

        object Message {
            const val CANNOT_CHANGE_ISSUE = "It is not possible to change an archived issue."
            const val CREATE_COMMENT = "You can't create an issue which has the current state set to archived."
            const val DELETE_COMMENT = "You can't delete an issue which has the current state set to archived."
            const val UPDATE_COMMENT = "You can't update an issue which has the current state set to archived."
        }
    }

    object Database {
        object UniqueConstraint {
            val TYPE = URI("/probs/unique-constraint")
            val STATUS = HttpStatus.CONFLICT
            val SQL_STATE = "23505"

            object Message {
                const val RESOURCE_ALREADY_EXISTS = "The resource already exists."
            }
        }
    }


    fun makeMessage(message: String, value: Any): String {
        return message.replace("{}", value.toString())
    }
}