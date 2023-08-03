package pt.isel.daw.project.integrationtests.issues

object IssueExpectedRepresentations {

    const val INVALID_ISSUEID_PATH_PARAM_TYPE = "{\"type\":\"/probs/validation-error\",\"title\":\"Type mismatch of"+
            " request path parameter.\",\"instance\":\"/v1/projects/1/issues/abc\",\"invalidParams\":[{\"name\":"+
            "\"issueId\",\"local\":\"path\",\"reason\":\"The value must be of the int type.\"}]}"

    const val ISSUE_NOT_FOUND = "{\"type\":\"/probs/not-found\",\"title\":\"The resource was not found.\"," +
            "\"instance\":\"/v1/projects/1/issues/100\"}"

    const val NO_BODY_ISSUE_CREATE = "{\"type\":\"/probs/validation-error\",\"title\":\"One or more request body " +
            "parameters are missing or have a type mismatch.\",\"instance\":\"/v1/projects/2/issues\"}"

    const val NO_BODY_ISSUE_UPDATE = "{\"type\":\"/probs/validation-error\",\"title\":\"One or more request body " +
            "parameters are missing or have a type mismatch.\",\"instance\":\"/v1/projects/3/issues/4\"}"

    const val ALL_PARAMETERS_NULL = "{\"type\":\"/probs/validation-error\",\"title\":\"All updatable parameters " +
            "can't be null.\",\"detail\":\" Please insert one of the parameters in order to update.\",\"instance" +
            "\":\"/v1/projects/1/issues/1\"}"

    const val GET_ISSUES = "{\"class\":[\"issue\",\"collection\"],\"properties\":{\"pageIndex\":0,\"pageSize\":2" +
            ",\"collectionSize\":2},\"entities\":[{\"class\":[\"issue\"],\"rel\":[\"item\"],\"properties\":{\"id" +
            "\":1,\"name\":\"Construir perímetro de segurança\",\"description\":\"Projetar e implementar um" +
            " perímetro de segurança para a inicialização das obras\",\"labels\":[{\"id\":2,\"name\":\"new-" +
            "functionality\"}],\"state\":{\"id\":3,\"name\":\"todo\"}},\"links\":[{\"rel\":[\"self\"],\"href\"" +
            ":\"/v1/projects/1/issues/1\"}]},{\"class\":[\"issue\"],\"rel\":[\"item\"],\"properties\":{\"id\":" +
            "2,\"name\":\"Retirar alcatrão\",\"description\":\"Remoção do alcatrão da estrada, onde vai passar o" +
            " futuro metro\",\"labels\":[{\"id\":1,\"name\":\"defect\"}],\"state\":{\"id\":3,\"name\":\"todo\"}}" +
            ",\"links\":[{\"rel\":[\"self\"],\"href\":\"/v1/projects/1/issues/2\"}]},{\"class\":[\"user\"],\"rel" +
            "\":[\"author\"],\"properties\":{\"id\":\"cf128ed3-0d65-42d9-8c96-8ff2e05b3d08\",\"name\":\"José " +
            "Bonifácio\",\"email\":\"joca@gmail.com\"},\"links\":[{\"rel\":[\"self\"],\"href\":\"/v1/user\"}]}" +
            "],\"actions\":[{\"name\":\"create-issue\",\"title\":\"Create an issue\",\"method\":\"POST\",\"" +
            "href\":\"/v1/projects/1/issues\",\"type\":\"application/json\",\"properties\":[{\"name\":\"name" +
            "\",\"type\":\"string\"},{\"name\":\"description\",\"type\":\"string\"},{\"name\":\"labels\",\"" +
            "type\":\"array\",\"itemsType\":\"number\",\"required\":false}]}],\"links\":[{\"rel\":[\"self\"" +
            "],\"href\":\"/v1/projects/1/issues?page=0\"}]}"

    const val GET_EMPTY_ISSUES = "{\"class\":[\"issue\",\"collection\"],\"properties\":{\"pageIndex\":0,\"" +
            "pageSize\":0,\"collectionSize\":0},\"entities\":[{\"class\":[\"user\"],\"rel\":[\"author\"],\"" +
            "properties\":{\"id\":\"cf128ed3-0d65-42d9-8c96-8ff2e05b3d08\",\"name\":\"José Bonifácio\",\"" +
            "email\":\"joca@gmail.com\"},\"links\":[{\"rel\":[\"self\"],\"href\":\"/v1/user\"}]}],\"actions\"" +
            ":[{\"name\":\"create-issue\",\"title\":\"Create an issue\",\"method\":\"POST\",\"href\":\"" +
            "/v1/projects/4/issues\",\"type\":\"application/json\",\"properties\":[{\"name\":\"name\",\"type\"" +
            ":\"string\"},{\"name\":\"description\",\"type\":\"string\"},{\"name\":\"labels\",\"type\":\"array\"" +
            ",\"itemsType\":\"number\",\"required\":false}]}],\"links\":[{\"rel\":[\"self\"],\"href\":\"/v1/" +
            "projects/4/issues?page=0\"}]}"

    const val CREATE_ISSUE_WITH_LABELS = "{\"class\":[\"issue\"],\"properties\":{\"id\":5,\"name\":\"Custo da rede\",\"" +
            "description\":\"O clube está em retenção de custos, devemos colocar uma rede simples\",\"creation" +
            "Timestamp\":,\"labels\":[{\"id\":1,\"name\":\"defect\"},{\"id\":2,\"name\":\"new-functionality\"}]" +
            ",\"state\":{\"id\":3,\"name\":\"todo\"},\"possibleTransitions\":[{\"id\":4,\"name\":\"wip\"}]},\"" +
            "entities\":[{\"class\":[\"user\"],\"rel\":[\"author\"],\"properties\":{\"id\":\"cf128ed3-0d65-42d9" +
            "-8c96-8ff2e05b3d08\",\"name\":\"José Bonifácio\",\"email\":\"joca@gmail.com\"},\"links\":[{\"rel\"" +
            ":[\"self\"],\"href\":\"/v1/user\"}]}],\"links\":[{\"rel\":[\"self\"],\"href\":\"/v1/projects/1/iss" +
            "ues/5\"}]}"

    const val CREATE_ISSUE_WITHOUT_LABELS = "{\"class\":[\"issue\"],\"properties\":{\"id\":5,\"name\":\"Custo " +
            "da rede\",\"description\":\"O clube está em retenção de custos, devemos colocar uma rede simples\"," +
            "\"creationTimestamp\":,\"state\":{\"id\":3,\"name\":\"todo\"},\"possibleTransitions\":[{\"id\":4," +
            "\"name\":\"wip\"}]},\"entities\":[{\"class\":[\"user\"],\"rel\":[\"author\"],\"properties\":{\"id\"" +
            ":\"cf128ed3-0d65-42d9-8c96-8ff2e05b3d08\",\"name\":\"José Bonifácio\",\"email\":\"joca@gmail.com\"}" +
            ",\"links\":[{\"rel\":[\"self\"],\"href\":\"/v1/user\"}]}],\"links\":[{\"rel\":[\"self\"],\"href\":" +
            "\"/v1/projects/1/issues/5\"}]}"

    const val GET_PROJECT_ISSUE = "{\"class\":[\"issue\"],\"properties\":{\"id\":1,\"name\":\"Construir " +
            "perímetro de segurança\",\"description\":\"Projetar e implementar um perímetro de segurança " +
            "para a inicialização das obras\",\"creationTimestamp\":,\"labels\":[{\"id\":2,\"name\":\"new-" +
            "functionality\"}],\"state\":{\"id\":3,\"name\":\"todo\"},\"possibleTransitions\":[{\"id\":4," +
            "\"name\":\"wip\"}]},\"entities\":[{\"class\":[\"comment\",\"collection\"],\"rel\":[\"issue-" +
            "comments\"],\"properties\":{\"pageIndex\":0,\"pageSize\":1,\"collectionSize\":1},\"entities\"" +
            ":[{\"class\":[\"comment\"],\"rel\":[\"item\"],\"properties\":{\"id\":1,\"comment\":\"Investim" +
            "ento muito elevado, deviam pensar melhor\",\"timestamp\":1649454767012},\"entities\":[{\"class" +
            "\":[\"user\"],\"rel\":[\"author\"],\"properties\":{\"id\":\"cf128ed3-0d65-42d9-8c96-8ff2e05b3d" +
            "08\",\"name\":\"José Bonifácio\",\"email\":\"joca@gmail.com\"},\"links\":[{\"rel\":[\"self\"],\"" +
            "href\":\"/v1/user\"}]}],\"actions\":[{\"name\":\"delete-comment\",\"title\":\"Delete a comment\"" +
            ",\"method\":\"DELETE\",\"href\":\"/v1/projects/1/issues/1/comments/1\"},{\"name\":\"update-comment" +
            "\",\"title\":\"Update a comment\",\"method\":\"PUT\",\"href\":\"/v1/projects/1/issues/1/comments/1" +
            "\",\"type\":\"application/json\",\"properties\":[{\"name\":\"comment\",\"type\":\"string\"}]}],\"" +
            "links\":[{\"rel\":[\"self\"],\"href\":\"/v1/projects/1/issues/1/comments/1\"}]}],\"actions\":[{\"" +
            "name\":\"create-comment\",\"title\":\"Create a comment\",\"method\":\"POST\",\"href\":\"/v1/proj" +
            "ects/1/issues/1/comments\",\"type\":\"application/json\",\"properties\":[{\"name\":\"comment\",\"" +
            "type\":\"string\"}]}],\"links\":[{\"rel\":[\"self\"],\"href\":\"/v1/projects/1/issues/1/comments?" +
            "page=0\"}]},{\"class\":[\"user\"],\"rel\":[\"author\"],\"properties\":{\"id\":\"cf128ed3-0d65-42d9" +
            "-8c96-8ff2e05b3d08\",\"name\":\"José Bonifácio\",\"email\":\"joca@gmail.com\"},\"links\":[{\"rel\"" +
            ":[\"self\"],\"href\":\"/v1/user\"}]}],\"actions\":[{\"name\":\"delete-issue\",\"title\":\"Delete " +
            "issue\",\"method\":\"DELETE\",\"href\":\"/v1/projects/1/issues/1\"},{\"name\":\"update-issue\",\"" +
            "title\":\"Update issue\",\"method\":\"PUT\",\"href\":\"/v1/projects/1/issues/1\",\"type\":\"appli" +
            "cation/json\",\"properties\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"description\",\"" +
            "type\":\"string\"}]}],\"links\":[{\"rel\":[\"self\"],\"href\":\"/v1/projects/1/issues/1\"}]}"

    const val UPDATE_ISSUE_NAME_AND_DESCRIPTION = "{\"class\":[\"issue\"],\"properties\":{\"id\":1,\"name\":" +
            "\"Make tests\",\"description\":\"Integration Tests\",\"labels\":[{\"id\":2,\"name\":\"new-functionality" +
            "\"}],\"state\":{\"id\":3,\"name\":\"todo\"}},\"entities\":[{\"class\":[\"user\"],\"rel\":[\"author\"]," +
            "\"properties\":{\"id\":\"cf128ed3-0d65-42d9-8c96-8ff2e05b3d08\",\"name\":\"José Bonifácio\",\"email" +
            "\":\"joca@gmail.com\"},\"links\":[{\"rel\":[\"self\"],\"href\":\"/v1/user\"}]}],\"links\":[{\"rel\"" +
            ":[\"self\"],\"href\":\"/v1/projects/1/issues/1\"}]}"

    const val UPDATE_ONLY_ISSUE_NAME = "{\"class\":[\"issue\"],\"properties\":{\"id\":1,\"name\":\"Make unit tests" +
            "\",\"description\":\"Projetar e implementar um perímetro de segurança para a inicialização das obras" +
            "\",\"labels\":[{\"id\":2,\"name\":\"new-functionality\"}],\"state\":{\"id\":3,\"name\":\"todo\"}}," +
            "\"entities\":[{\"class\":[\"user\"],\"rel\":[\"author\"],\"properties\":{\"id\":\"cf128ed3-0d65-42d9" +
            "-8c96-8ff2e05b3d08\",\"name\":\"José Bonifácio\",\"email\":\"joca@gmail.com\"},\"links\":[{\"rel\":" +
            "[\"self\"],\"href\":\"/v1/user\"}]}],\"links\":[{\"rel\":[\"self\"],\"href\":\"/v1/projects/1/issues" +
            "/1\"}]}"

    const val UPDATE_ONLY_ISSUE_DESCRIPTION = "{\"class\":[\"issue\"],\"properties\":{\"id\":1,\"name\":\"" +
            "Construir perímetro de segurança\",\"description\":\"we love coding!!!!\",\"labels\":[{\"id\"" +
            ":2,\"name\":\"new-functionality\"}],\"state\":{\"id\":3,\"name\":\"todo\"}},\"entities\":[{\"" +
            "class\":[\"user\"],\"rel\":[\"author\"],\"properties\":{\"id\":\"cf128ed3-0d65-42d9-8c96-8ff2" +
            "e05b3d08\",\"name\":\"José Bonifácio\",\"email\":\"joca@gmail.com\"},\"links\":[{\"rel\":[\"" +
            "self\"],\"href\":\"/v1/user\"}]}],\"links\":[{\"rel\":[\"self\"],\"href\":\"/v1/projects/1/issues/1\"}]}"

    const val DELETE_ISSUE = "{\"class\":[\"issue\"],\"properties\":{\"id\":1,\"name\":\"Construir perímetro de " +
            "segurança\",\"description\":\"Projetar e implementar um perímetro de segurança para a inicialização " +
            "das obras\",\"labels\":[{\"id\":2,\"name\":\"new-functionality\"}],\"state\":{\"id\":3,\"name\":\"todo" +
            "\"}},\"entities\":[{\"class\":[\"user\"],\"rel\":[\"author\"],\"properties\":{\"id\":\"cf128ed3-0d65-" +
            "42d9-8c96-8ff2e05b3d08\",\"name\":\"José Bonifácio\",\"email\":\"joca@gmail.com\"},\"links\":[{\"rel\"" +
            ":[\"self\"],\"href\":\"/v1/user\"}]}],\"links\":[{\"rel\":[\"self\"],\"href\":\"/v1/projects/1/issues/" +
            "1\"},{\"rel\":[\"issues\"],\"href\":\"/v1/projects/1/issues\"}]}"


}