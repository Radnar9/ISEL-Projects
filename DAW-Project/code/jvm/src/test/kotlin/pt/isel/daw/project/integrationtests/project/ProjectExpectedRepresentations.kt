package pt.isel.daw.project.integrationtests.project

object ProjectExpectedRepresentations {
    const val GET_PROJECT = "{\"class\":[\"project\"],\"properties\":{\"id\":1,\"name\":\"Caparica Metropolitano\"," +
            "\"description\":\"Expansão do metro para a zona da Costa da Caparica\",\"labels\":[{\"id\":1,\"name\"" +
            ":\"defect\"},{\"id\":2,\"name\":\"new-functionality\"},{\"id\":3,\"name\":\"exploration\"}],\"states\"" +
            ":[{\"id\":1,\"name\":\"closed\"},{\"id\":2,\"name\":\"archived\"},{\"id\":3,\"name\":\"todo\"},{\"id\":" +
            "4,\"name\":\"wip\"}],\"initialState\":{\"id\":3,\"name\":\"todo\"},\"statesTransitions\":[{\"id\":1," +
            "\"name\":\"closed\"},{\"id\":2,\"name\":\"archived\"},{\"id\":3,\"name\":\"todo\"},{\"id\":4,\"name\"" +
            ":\"wip\"},{\"id\":4,\"name\":\"wip\"},{\"id\":1,\"name\":\"closed\"}]},\"entities\":[{\"class\":[\"" +
            "issue\",\"collection\"],\"rel\":[\"project-issues\"],\"properties\":{\"pageIndex\":0,\"pageSize\":2" +
            ",\"collectionSize\":2},\"entities\":[{\"class\":[\"issue\"],\"rel\":[\"item\"],\"properties\":{\"" +
            "id\":1,\"name\":\"Construir perímetro de segurança\",\"description\":\"Projetar e implementar um " +
            "perímetro de segurança para a inicialização das obras\",\"labels\":[{\"id\":2,\"name\":\"new-" +
            "functionality\"}],\"state\":{\"id\":3,\"name\":\"todo\"}},\"links\":[{\"rel\":[\"self\"],\"href\"" +
            ":\"/v1/projects/1/issues/1\"}]},{\"class\":[\"issue\"],\"rel\":[\"item\"],\"properties\":{\"id\"" +
            ":2,\"name\":\"Retirar alcatrão\",\"description\":\"Remoção do alcatrão da estrada, onde vai passar " +
            "o futuro metro\",\"labels\":[{\"id\":1,\"name\":\"defect\"}],\"state\":{\"id\":3,\"name\":\"todo\"}}" +
            ",\"links\":[{\"rel\":[\"self\"],\"href\":\"/v1/projects/1/issues/2\"}]}],\"actions\":[{\"name\":\"" +
            "create-issue\",\"title\":\"Create an issue\",\"method\":\"POST\",\"href\":\"/v1/projects/1/issues\"" +
            ",\"type\":\"application/json\",\"properties\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":" +
            "\"description\",\"type\":\"string\"},{\"name\":\"labels\",\"type\":\"array\",\"itemsType\":\"number\"" +
            ",\"required\":false}]}],\"links\":[{\"rel\":[\"self\"],\"href\":\"/v1/projects/1/issues?page=0\"}]}," +
            "{\"class\":[\"user\"],\"rel\":[\"author\"],\"properties\":{\"id\":\"cf128ed3-0d65-42d9-8c96-8ff2e05b3d" +
            "08\",\"name\":\"José Bonifácio\",\"email\":\"joca@gmail.com\"},\"links\":[{\"rel\":[\"self\"],\"href" +
            "\":\"/v1/user\"}]}],\"actions\":[{\"name\":\"delete-project\",\"title\":\"Delete project\",\"method\"" +
            ":\"DELETE\",\"href\":\"/v1/projects/1\"},{\"name\":\"update-project\",\"title\":\"Update project\"," +
            "\"method\":\"PUT\",\"href\":\"/v1/projects/1\",\"type\":\"application/json\",\"properties\":[{\"name\"" +
            ":\"name\",\"type\":\"string\"},{\"name\":\"description\",\"type\":\"string\"}]}],\"links\":[{\"rel\":" +
            "[\"self\"],\"href\":\"/v1/projects/1\"}]}"

    const val GET_PROJECTS = "{\"class\":[\"project\",\"collection\"],\"properties\":{\"pageIndex\":0,\"pageSize" +
            "\":1,\"collectionSize\":1},\"entities\":[{\"class\":[\"project\"],\"rel\":[\"item\"],\"properties\"" +
            ":{\"id\":1,\"name\":\"Caparica Metropolitano\",\"description\":\"Expansão do metro para a zona da Co" +
            "sta da Caparica\"},\"links\":[{\"rel\":[\"self\"],\"href\":\"/v1/projects/1\"}]},{\"class\":[\"user\"" +
            "],\"rel\":[\"author\"],\"properties\":{\"id\":\"cf128ed3-0d65-42d9-8c96-8ff2e05b3d08\",\"name\":\"José " +
            "Bonifácio\",\"email\":\"joca@gmail.com\"},\"links\":[{\"rel\":[\"self\"],\"href\":\"/v1/user\"}]}]," +
            "\"actions\":[{\"na" +
            "me\":\"create-project\",\"title\":\"Create a project\",\"method\":\"POST\",\"href\":\"/v1/projects\"" +
            ",\"type\":\"application/json\",\"properties\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"de" +
            "scription\",\"type\":\"string\"},{\"name\":\"labels\",\"type\":\"array\",\"itemsType\":\"string\"},{\"n" +
            "ame\":\"states\",\"type\":\"array\",\"itemsType\":\"string\"},{\"name\":\"statesTransitions\",\"type\"" +
            ":\"array\",\"itemsType\":\"string\"},{\"name\":\"initialState\",\"type\":\"string\"}]}],\"links\":[{\"" +
            "rel\":[\"self\"],\"href\":\"/v1/projects?page=0\"}]}"

    const val GET_EMPTY_PROJECTS = "{\"class\":[\"project\",\"collection\"],\"properties\":{\"pageIndex\":0,\"" +
            "pageSize\":0,\"collectionSize\":0},\"entities\":[{\"class\":[\"user\"],\"rel\":[\"author\"],\"" +
            "properties\":{\"id\":\"27f6016a-a891-459e-85d6-7df20815769a\",\"name\":\"Guilherme Ronaldo\",\"email" +
            "\":\"guizado@hotmail.com\"},\"links\":[{\"rel\":[\"self\"],\"href\":\"/v1/user\"}]}],\"actions\":[{" +
            "\"name\":\"create-project\",\"title\":\"Create a project\",\"method\":\"POST\",\"href\":\"/v1/projec" +
            "ts\",\"type\":\"application/json\",\"properties\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\"" +
            ":\"description\",\"type\":\"string\"},{\"name\":\"labels\",\"type\":\"array\",\"itemsType\":\"string\"}" +
            ",{\"name\":\"states\",\"type\":\"array\",\"itemsType\":\"string\"},{\"name\":\"statesTransitions\",\"t" +
            "ype\":\"array\",\"itemsType\":\"string\"},{\"name\":\"initialState\",\"type\":\"string\"}]}],\"links\":" +
            "[{\"rel\":[\"self\"],\"href\":\"/v1/projects?page=0\"}]}"

    const val CREATE_PROJECT = "{\"class\":[\"project\"],\"properties\":{\"id\":4,\"name\":\"Construção de rede no" +
            " Estádio da Luz\",\"description\":\"Construção de rede para proteger do arremesso de materiais perigosos" +
            " para o relvado\",\"labels\":[{\"id\":10,\"name\":\"ltest1\"},{\"id\":11,\"name\":\"ltest2\"},{\"id\":" +
            "12,\"name\":\"ltest3\"}],\"states\":[{\"id\":13,\"name\":\"stest1\"},{\"id\":14,\"name\":\"stest2\"},{" +
            "\"id\":15,\"name\":\"closed\"},{\"id\":16,\"name\":\"archived\"}],\"initialState\":{\"id\":13,\"name\"" +
            ":\"stest1\"},\"statesTransitions\":[{\"id\":13,\"name\":\"stest1\"},{\"id\":14,\"name\":\"stest2\"}," +
            "{\"id\":14,\"name\":\"stest2\"},{\"id\":15,\"name\":\"closed\"},{\"id\":15,\"name\":\"closed\"},{\"id" +
            "\":16,\"name\":\"archived\"}]},\"entities\":[{\"class\":[\"user\"],\"rel\":[\"author\"],\"properties\"" +
            ":{\"id\":\"b54f4f46-5833-4aae-a205-456da878ebc2\",\"name\":\"Zé Pedro\",\"email\":\"zezinho@hotmail.com" +
            "\"},\"links\":[{\"rel\":[\"self\"],\"href\":\"/v1/user\"}]}],\"links\":[{\"rel\":[\"self\"],\"href\":\"" +
            "/v1/projects/4\"}]}"

    const val UPDATE_PROJECT_NAME_DESC = "{\"class\":[\"project\"],\"properties\":{\"id\":1,\"name\":\"Make tests" +
            "\",\"description\":\"Integration Tests\"},\"entities\":[{\"class\":[\"user\"],\"rel\":[\"author\"],\"" +
            "properties\":{\"id\":\"cf128ed3-0d65-42d9-8c96-8ff2e05b3d08\",\"name\":\"José Bonifácio\",\"email\"" +
            ":\"joca@gmail.com\"},\"links\":[{\"rel\":[\"self\"],\"href\":\"/v1/user\"}]}],\"links\":[{\"rel\":[" +
            "\"self\"],\"href\":\"/v1/projects/1\"}]}"

    const val UPDATE_PROJECT_NAME = "{\"class\":[\"project\"],\"properties\":{\"id\":1,\"name\":\"Make unit tests\"" +
            ",\"description\":\"Expansão do metro para a zona da Costa da Caparica\"},\"entities\":[{\"class\":[\"" +
            "user\"],\"rel\":[\"author\"],\"properties\":{\"id\":\"cf128ed3-0d65-42d9-8c96-8ff2e05b3d08\",\"name\":\"" +
            "José Bonifácio\",\"email\":\"joca@gmail.com\"},\"links\":[{\"rel\":[\"self\"],\"href\":\"/v1/user\"}]}]" +
            ",\"links\":[{\"rel\":[\"self\"],\"href\":\"/v1/projects/1\"}]}"

    const val UPDATE_PROJECT_DESC = "{\"class\":[\"project\"],\"properties\":{\"id\":1,\"name\":\"Caparica Metropo" +
            "litano\",\"description\":\"We love testing!!!\"},\"entities\":[{\"class\":[\"user\"],\"rel\":[\"author\"" +
            "],\"properties\":{\"id\":\"cf128ed3-0d65-42d9-8c96-8ff2e05b3d08\",\"name\":\"José Bonifácio\",\"email\"" +
            ":\"joca@gmail.com\"},\"links\":[{\"rel\":[\"self\"],\"href\":\"/v1/user\"}]}],\"links\":[{\"rel\":[\"" +
            "self\"],\"href\":\"/v1/projects/1\"}]}"

    const val DELETE_PROJECT = "{\"class\":[\"project\"],\"properties\":{\"id\":1,\"name\":\"Caparica Metropolitano" +
            "\",\"description\":\"Expansão do metro para a zona da Costa da Caparica\"},\"entities\":[{\"class\":[\"" +
            "user\"],\"rel\":[\"author\"],\"properties\":{\"id\":\"cf128ed3-0d65-42d9-8c96-8ff2e05b3d08\",\"name\":\"" +
            "José Bonifácio\",\"email\":\"joca@gmail.com\"},\"links\":[{\"rel\":[\"self\"],\"href\":\"/v1/user\"}]}]," +
            "\"links\":[{\"rel\":[\"self\"],\"href\":\"/v1/projects/1\"},{\"rel\":[\"projects\"],\"href\":\"" +
            "/v1/projects\"}]}"

    const val ADD_LABELS_TO_PROJECT = "{\"class\":[\"project\"],\"properties\":{\"id\":1,\"name\":\"Caparica Metrop" +
            "olitano\",\"description\":\"Expansão do metro para a zona da Costa da Caparica\",\"labels\":[{\"id\"" +
            ":1,\"name\":\"defect\"},{\"id\":2,\"name\":\"new-functionality\"},{\"id\":3,\"name\":\"exploration\"}" +
            ",{\"id\":10,\"name\":\"update\"},{\"id\":11,\"name\":\"tests\"}]},\"entities\":[{\"class\":[\"user\"]" +
            ",\"rel\":[\"author\"],\"properties\":{\"id\":\"cf128ed3-0d65-42d9-8c96-8ff2e05b3d08\",\"name\":\"José " +
            "Bonifácio\",\"email\":\"joca@gmail.com\"},\"links\":[{\"rel\":[\"self\"],\"href\":\"/v1/user\"}]}]," +
            "\"links\":[{\"rel\":[\"self\"],\"href\":\"/v1/projects/1\"}]}"

    const val INVALID_PROJECTID_PATH_PARAM_TYPE = "{\"type\":\"/probs/validation-error\",\"title\":\"Type mismatch" +
            " of request path parameter.\",\"instance\":\"/v1/projects/abc\",\"invalidParams\":[{\"name\":\"" +
            "projectId\",\"local\":\"path\",\"reason\":\"The value must be of the int type.\"}]}"

    const val INVALID_PROJECTID_HTTP_METHOD = "{\"type\":\"/probs/method-not-allowed\",\"title\":\"The request " +
            "method is not supported for the requested instance.\",\"instance\":\"/v1/projects/1\"}"

    const val PROJECT_NOT_FOUND = "{\"type\":\"/probs/not-found\",\"title\":\"The project with the id 100 was" +
            " not found.\",\"instance\":\"/v1/projects/100\"}"

    const val PROJECT_TO_ADD_LABELS_NOT_FOUND = "{\"type\":\"/probs/not-found\",\"title\":\"The project with the id 100 was" +
            " not found.\",\"instance\":\"/v1/projects/100/labels\"}"

    const val UPDATE_PROJECT_WITH_NULL_PARAMS = "{\"type\":\"/probs/validation-error\",\"title\":\"All updatable" +
            " parameters can't be null.\",\"detail\":\" Please insert one of the parameters in order to update.\"," +
            "\"instance\":\"/v1/projects/1\"}"

    const val UPDATE_PROJECT_WITHOUT_BODY = "{\"type\":\"/probs/validation-error\",\"title\":\"One or more request" +
            " body parameters are missing or have a type mismatch.\",\"instance\":\"/v1/projects/1\"}"

    const val CREATE_PROJECT_WITHOUT_VALID_BODY = "{\"type\":\"/probs/validation-error\",\"title\":\"One or more request" +
            " body parameters are missing or have a type mismatch.\",\"instance\":\"/v1/projects\"}"

    const val INIT_STATE_DOESNT_BELONG_IN_STATES = "{\"type\":\"/probs/validation-error\",\"title\":\"One or more" +
            " request parameters are not valid.\",\"instance\":\"/v1/projects\",\"invalidParams\":[{\"name\":\"" +
            "initialState\",\"local\":\"body\",\"reason\":\"The initial state doesn't exist in the states array.\"}]}"

    const val ODD_TRANSITIONS_ARRAY_LENGTH = "{\"type\":\"/probs/validation-error\",\"title\":\"One or more request" +
            " parameters are not valid.\",\"instance\":\"/v1/projects\",\"invalidParams\":[{\"name\":\"" +
            "statesTransitions\",\"local\":\"body\",\"reason\":\"The size of the transitions array must be pair.\"}]}"

    const val TRANSITION_STATE_NOT_IN_STATES = "{\"type\":\"/probs/validation-error\",\"title\":\"One or more request" +
            " parameters are not valid.\",\"instance\":\"/v1/projects\",\"invalidParams\":[{\"name\":\"" +
            "statesTransitions\",\"local\":\"body\",\"reason\":\"At least one entry in transitions array doesn't" +
            " belong in states array.\"}]}"
}

