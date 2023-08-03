package pt.isel.daw.project.utils

import pt.isel.daw.project.auth.Authentication.Basic
import pt.isel.daw.project.exception.ArchivedIssueException
import pt.isel.daw.project.exception.Error.ArchivedIssue.Message.CANNOT_CHANGE_ISSUE
import pt.isel.daw.project.exception.Error.BadRequest.Locations.BODY
import pt.isel.daw.project.exception.Error.BadRequest.Message.INVALID_REQ_PARAMS
import pt.isel.daw.project.exception.Error.BadRequest.Message.Project.CLOSED_ARCH_NO_TRANSITION
import pt.isel.daw.project.exception.Error.BadRequest.Message.Project.CLOSED_ARCH_STATES_MISSING
import pt.isel.daw.project.exception.Error.BadRequest.Message.Project.INITIAL_STATE_NOT_FOUND
import pt.isel.daw.project.exception.Error.BadRequest.Message.Project.INVALID_TRANSITIONS_ARRAY_SIZE
import pt.isel.daw.project.exception.Error.BadRequest.Message.Project.MISSING_LABELS_ARRAY
import pt.isel.daw.project.exception.Error.BadRequest.Message.Project.STATE_IN_TRANSITIONS_NOT_FOUND
import pt.isel.daw.project.exception.Error.BadRequest.Message.Project.UPDATE_NULL_PARAMS
import pt.isel.daw.project.exception.Error.BadRequest.Message.Project.UPDATE_NULL_PARAMS_DETAIL
import pt.isel.daw.project.exception.Error.Unauthorized.Message.INVALID_SCHEME
import pt.isel.daw.project.exception.Error.Unauthorized.Message.REQUIRES_AUTH
import pt.isel.daw.project.exception.InvalidParameter
import pt.isel.daw.project.exception.InvalidParameterException
import pt.isel.daw.project.exception.UnauthorizedException
import pt.isel.daw.project.model.comment.CommentDto
import pt.isel.daw.project.model.issue.UpdateIssueEntity
import pt.isel.daw.project.model.project.CreateProjectEntity
import pt.isel.daw.project.model.project.UpdateProjectEntity

object Validator {

    object BasicAuthentication {

        /**
         *  Validates the authorization header
         *  @throws UnauthorizedException otherwise
         */
        fun validateAuthorizationHeader(authorizationHeader: List<String>?) {
            if (authorizationHeader == null || authorizationHeader.size < 2) {
                throw UnauthorizedException(REQUIRES_AUTH)
            } else if (authorizationHeader[0] != Basic.SCHEME) {
                throw UnauthorizedException(INVALID_SCHEME)
            }
        }
    }

    object Project {
        fun checkIfStatesContainInitialState(project: CreateProjectEntity) {
            if (!project.states.contains(project.initialState)) {
                throw InvalidParameterException(
                    INVALID_REQ_PARAMS,
                    listOf(InvalidParameter("initialState", BODY, INITIAL_STATE_NOT_FOUND)),
                )
            }
        }

        fun checkTransitionsArrayParity(project: CreateProjectEntity) {
            if (project.statesTransitions.size % 2 != 0) {
                throw InvalidParameterException(
                    INVALID_REQ_PARAMS,
                    listOf(InvalidParameter("statesTransitions", BODY, INVALID_TRANSITIONS_ARRAY_SIZE)),
                )
            }
        }

        fun checkIfTransitionsBelongToStates(project: CreateProjectEntity) {
            project.statesTransitions.forEach {
                if (!project.states.contains(it)) {
                    throw InvalidParameterException(
                        INVALID_REQ_PARAMS,
                        listOf(InvalidParameter("statesTransitions",BODY, STATE_IN_TRANSITIONS_NOT_FOUND)),
                    )
                }
            }
        }

        fun checkIfBothParametersAreNull(project: UpdateProjectEntity) {
            if (project.name == null && project.description == null) {
                throw InvalidParameterException(
                    UPDATE_NULL_PARAMS,
                    detail = UPDATE_NULL_PARAMS_DETAIL,
                )
            }
        }

        fun checkLabelsExistance(labels: Array<String>?) {
            if (labels == null || labels.isEmpty()) {
                throw InvalidParameterException(MISSING_LABELS_ARRAY)
            }
        }

        fun checkStatesClosedAndArchivedExistance(project: CreateProjectEntity) {
            val closed = "closed"
            val archived = "archived"
            if (!(project.states.contains(closed) && project.states.contains(archived))) {
                throw InvalidParameterException(CLOSED_ARCH_STATES_MISSING)
            }

            val closedIdx = project.statesTransitions.indexOf(closed)
            val archivedIdx = project.statesTransitions.indexOf(archived)
            if (closedIdx == -1 || archivedIdx == -1) {
                throw InvalidParameterException(CLOSED_ARCH_STATES_MISSING)
            } else if (project.statesTransitions[archivedIdx - 1] != closed) {
                throw InvalidParameterException(CLOSED_ARCH_NO_TRANSITION)
            }
        }

        // TODO: Validate if there is at least a transition for each created state
    }

    object Issue {
        fun checkIfAllParametersAreNull(issue: UpdateIssueEntity) {
            if (issue.name == null && issue.description == null && issue.state == null) {
                throw InvalidParameterException(
                    UPDATE_NULL_PARAMS,
                    detail = UPDATE_NULL_PARAMS_DETAIL,
                )
            }
        }
    }

    object Comment {
        fun checkIfIssueIsArchived(comment: CommentDto, detailedMsg: String) {
            if (comment.isArchived != null && comment.isArchived) {
                throw ArchivedIssueException(CANNOT_CHANGE_ISSUE, detailedMsg)
            }
        }
    }
}