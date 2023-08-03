package pt.isel.daw.project.integrationtests.comments

object CommentExpectedRepresentations {

    const val COMMENT_NOT_FOUND = "{\"type\":\"/probs/not-found\",\"title\":\"The resource was not found." +
            "\",\"instance\":\"/v1/projects/1/issues/1/comments/100\"}"

    const val NO_BODY_COMMENT_CREATE = "{\"type\":\"/probs/validation-error\",\"title\":\"One or more request body " +
            "parameters are missing or have a type mismatch.\",\"instance\":\"/v1/projects/2/issues/3/comments\"}"

    const val NO_BODY_COMMENT_UPDATE = "{\"type\":\"/probs/validation-error\",\"title\":\"One or more request body" +
            " parameters are missing or have a type mismatch.\",\"instance\":\"/v1/projects/3/issues/4/comments/3\"}"

    const val INVALID_COMMENTID_PATH_PARAM_TYPE = "{\"type\":\"/probs/validation-error\",\"title\":\"Type mismatch " +
            "of request path parameter.\",\"instance\":\"/v1/projects/1/issues/1/comments/abc\",\"invalidParams\"" +
            ":[{\"name\":\"commentId\",\"local\":\"path\",\"reason\":\"The value must be of the int type.\"}]}"

    const val GET_COMMENTS = "{\"class\":[\"comment\",\"collection\"],\"properties\":{\"pageIndex\":0,\"pageSize\"" +
            ":1,\"collectionSize\":1},\"entities\":[{\"class\":[\"comment\"],\"rel\":[\"item\"],\"properties\":" +
            "{\"id\":1,\"comment\":\"Investimento muito elevado, deviam pensar melhor\",\"timestamp\":1649454767012}" +
            ",\"entities\":[{\"class\":[\"user\"],\"rel\":[\"author\"],\"properties\":{\"id\":\"cf128ed3-0d65-42d9-" +
            "8c96-8ff2e05b3d08\",\"name\":\"José Bonifácio\",\"email\":\"joca@gmail.com\"},\"links\":[{\"rel\":[\"" +
            "self\"],\"href\":\"/v1/user\"}]}],\"actions\":[{\"name\":\"delete-comment\",\"title\":\"Delete a commen" +
            "t\",\"method\":\"DELETE\",\"href\":\"/v1/projects/1/issues/1/comments/1\"},{\"name\":\"update-comment\"" +
            ",\"title\":\"Update a comment\",\"method\":\"PUT\",\"href\":\"/v1/projects/1/issues/1/comments/1\",\"" +
            "type\":\"application/json\",\"properties\":[{\"name\":\"comment\",\"type\":\"string\"}]}],\"links\":[" +
            "{\"rel\":[\"self\"],\"href\":\"/v1/projects/1/issues/1/comments/1\"}]}],\"actions\":[{\"name\":\"" +
            "create-comment\",\"title\":\"Create a comment\",\"method\":\"POST\",\"href\":\"/v1/projects/1/issues" +
            "/1/comments\",\"type\":\"application/json\",\"properties\":[{\"name\":\"comment\",\"type\":\"string" +
            "\"}]}],\"links\":[{\"rel\":[\"self\"],\"href\":\"/v1/projects/1/issues/1/comments?page=0\"}]}"

    const val GET_COMMENT = "{\"class\":[\"comment\"],\"properties\":{\"id\":1,\"comment\":\"Investimento muito " +
            "elevado, deviam pensar melhor\",\"timestamp\":1649454767012},\"entities\":[{\"class\":[\"user\"]," +
            "\"rel\":[\"author\"],\"properties\":{\"id\":\"cf128ed3-0d65-42d9-8c96-8ff2e05b3d08\",\"name\":\"" +
            "José Bonifácio\",\"email\":\"joca@gmail.com\"},\"links\":[{\"rel\":[\"self\"],\"href\":\"/v1/user" +
            "\"}]}],\"actions\":[{\"name\":\"delete-comment\",\"title\":\"Delete a comment\",\"method\":\"DELETE" +
            "\",\"href\":\"/v1/projects/1/issues/1/comments/1\"},{\"name\":\"update-comment\",\"title\":\"Update" +
            " a comment\",\"method\":\"PUT\",\"href\":\"/v1/projects/1/issues/1/comments/1\",\"type\":\"application" +
            "/json\",\"properties\":[{\"name\":\"comment\",\"type\":\"string\"}]}],\"links\":[{\"rel\":[\"self\"]" +
            ",\"href\":\"/v1/projects/1/issues/1/comments/1\"}]}"

    const val GET_EMPTY_COMMENTS = "{\"class\":[\"comment\",\"collection\"],\"properties\":{\"pageIndex\":0," +
            "\"pageSize\":0,\"collectionSize\":0},\"entities\":[],\"actions\":[{\"name\":\"create-comment\",\"" +
            "title\":\"Create a comment\",\"method\":\"POST\",\"href\":\"/v1/projects/2/issues/3/comments\",\"" +
            "type\":\"application/json\",\"properties\":[{\"name\":\"comment\",\"type\":\"string\"}]}],\"links\"" +
            ":[{\"rel\":[\"self\"],\"href\":\"/v1/projects/2/issues/3/comments?page=0\"}]}"

    const val CREATE_COMMENT = "{\"class\":[\"comment\"],\"properties\":{\"id\":4,\"comment\":\"Comment Test\"," +
            "\"timestamp\":},\"entities\":[{\"class\":[\"user\"],\"rel\":[\"author\"],\"properties\":{\"id\":\"" +
            "9f5246de-3a02-4f91-9768-ec576311a523\",\"name\":\"Pedro Moreira\",\"email\":\"pedrocas@outlook.com" +
            "\"},\"links\":[{\"rel\":[\"self\"],\"href\":\"/v1/user\"}]}],\"links\":[{\"rel\":[\"self\"],\"href" +
            "\":\"/v1/projects/2/issues/3/comments/4\"},{\"rel\":[\"comments\"],\"href\":\"/v1/projects/2/issues" +
            "/3/comments\"}]}"

    const val UPDATE_COMMENT = "{\"class\":[\"comment\"],\"properties\":{\"id\":3,\"comment\":\"Comment Test: we " +
            "love Lisbon\",\"timestamp\":1649454767012},\"entities\":[{\"class\":[\"user\"],\"rel\":[\"author\"]," +
            "\"properties\":{\"id\":\"b54f4f46-5833-4aae-a205-456da878ebc2\",\"name\":\"Zé Pedro\",\"email\":\"" +
            "zezinho@hotmail.com\"},\"links\":[{\"rel\":[\"self\"],\"href\":\"/v1/user\"}]}],\"links\":[{\"rel\"" +
            ":[\"self\"],\"href\":\"/v1/projects/3/issues/4/comments/3\"}]}"
}