package pt.isel.daw.project.integrationtests.argresolvers

object ArgResolverExpectedRepresentations {
    const val INVALID_AUTH_CREDENTIALS = "{\"type\":\"/probs/unauthorized\",\"title\":\"Invalid authentication " +
            "credentials.\",\"instance\":\"/v1/projects/1\"}"

    const val INVALID_PAGINATION_TYPE = "{\"type\":\"/probs/validation-error\",\"title\":\"Type mismatch of request" +
            " query parameter.\",\"instance\":\"/v1/projects\",\"invalidParams\":[{\"name\":\"page\",\"local\":\"" +
            "query_string\",\"reason\":\"The value must be an integer >= 0\"}]}"
}