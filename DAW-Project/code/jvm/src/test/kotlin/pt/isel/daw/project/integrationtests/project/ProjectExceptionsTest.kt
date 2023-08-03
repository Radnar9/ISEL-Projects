package pt.isel.daw.project.integrationtests.project

import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.*
import pt.isel.daw.project.integrationtests.project.ProjectExpectedRepresentations.CREATE_PROJECT_WITHOUT_VALID_BODY
import pt.isel.daw.project.integrationtests.project.ProjectExpectedRepresentations.INIT_STATE_DOESNT_BELONG_IN_STATES
import pt.isel.daw.project.integrationtests.project.ProjectExpectedRepresentations.PROJECT_NOT_FOUND
import pt.isel.daw.project.integrationtests.project.ProjectExpectedRepresentations.INVALID_PROJECTID_HTTP_METHOD
import pt.isel.daw.project.integrationtests.project.ProjectExpectedRepresentations.INVALID_PROJECTID_PATH_PARAM_TYPE
import pt.isel.daw.project.integrationtests.project.ProjectExpectedRepresentations.ODD_TRANSITIONS_ARRAY_LENGTH
import pt.isel.daw.project.integrationtests.project.ProjectExpectedRepresentations.PROJECT_TO_ADD_LABELS_NOT_FOUND
import pt.isel.daw.project.integrationtests.project.ProjectExpectedRepresentations.TRANSITION_STATE_NOT_IN_STATES
import pt.isel.daw.project.integrationtests.project.ProjectExpectedRepresentations.UPDATE_PROJECT_WITHOUT_BODY
import pt.isel.daw.project.integrationtests.project.ProjectExpectedRepresentations.UPDATE_PROJECT_WITH_NULL_PARAMS
import pt.isel.daw.project.model.DawJsonModel
import pt.isel.daw.project.model.ProblemJsonModel
import pt.isel.daw.project.model.Uris
import pt.isel.daw.project.model.project.CreateProjectEntity
import pt.isel.daw.project.model.project.UpdateProjectEntity
import pt.isel.daw.project.utils.Utils
import pt.isel.daw.project.utils.serializeJsonTo

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProjectExceptionsTest {
    @Autowired
    private lateinit var client: TestRestTemplate

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var jdbi: Jdbi

    private val delScript = Utils.LoadScript.getResourceFile("sql/deleteTables.sql")
    private val fillScript = Utils.LoadScript.getResourceFile("sql/insertTables.sql")

    @BeforeAll
    fun setUp() {
        jdbi.open().use { h -> h.createScript(delScript).execute(); h.createScript(fillScript).execute() }
    }

    @AfterAll
    fun cleanUp() {
        jdbi.open().use { h -> h.createScript(delScript).execute();}
    }

    @Test
    fun `projectId path parameter not an integer`() {
        Assertions.assertNotNull(client)
        val url = "${Utils.DOMAIN}$port${Uris.Projects.PATH}/abc"

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com", "soubuefixe")

        val res = client.exchange(url, HttpMethod.GET, HttpEntity<String>(headers), String::class.java)

        Assertions.assertEquals(INVALID_PROJECTID_PATH_PARAM_TYPE, res.body)
        Assertions.assertEquals(ProblemJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, res.statusCode)
    }

    @Test
    fun `Http method not allowed`() {
        Assertions.assertNotNull(client)
        val url = "${Utils.DOMAIN}$port${Uris.Projects.makeSingle(1)}"

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com", "soubuefixe")

        val res = client.exchange(url, HttpMethod.POST, HttpEntity<String>(headers), String::class.java)

        Assertions.assertEquals(INVALID_PROJECTID_HTTP_METHOD, res.body)
        Assertions.assertEquals(ProblemJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.METHOD_NOT_ALLOWED, res.statusCode)
    }

    @Test
    fun `Project to get not found`() {
        Assertions.assertNotNull(client)
        val url = "${Utils.DOMAIN}$port${Uris.Projects.makeSingle(100)}"

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com", "soubuefixe")

        val res = client.exchange(url, HttpMethod.GET, HttpEntity<String>(headers), String::class.java)

        Assertions.assertEquals(PROJECT_NOT_FOUND, res.body)
        Assertions.assertEquals(ProblemJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.NOT_FOUND, res.statusCode)
    }

    @Test
    fun `Project to update not found`() {
        Assertions.assertNotNull(client)
        val url = "${Utils.DOMAIN}$port${Uris.Projects.makeSingle(100)}"

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com", "soubuefixe")
        headers.contentType = DawJsonModel.MEDIA_TYPE
        val project = UpdateProjectEntity(
            100,
            "Make tests",
            "Integration Tests",
            null
        )
        val req = HttpEntity<String>(project.serializeJsonTo<UpdateProjectEntity>(), headers)
        val res = client.exchange(url, HttpMethod.PUT, req, String::class.java)

        Assertions.assertEquals(PROJECT_NOT_FOUND, res.body)
        Assertions.assertEquals(ProblemJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.NOT_FOUND, res.statusCode)
    }

    @Test
    fun `Project to delete not found`() {
        Assertions.assertNotNull(client)
        val url = "${Utils.DOMAIN}$port${Uris.Projects.makeSingle(100)}"

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com", "soubuefixe")

        val req = HttpEntity<String>(headers)
        val res = client.exchange(url, HttpMethod.DELETE, req, String::class.java)

        Assertions.assertEquals(PROJECT_NOT_FOUND, res.body)
        Assertions.assertEquals(ProblemJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.NOT_FOUND, res.statusCode)
    }

    @Test
    fun `Project to add labels not found`() {
        Assertions.assertNotNull(client)
        val url = "${Utils.DOMAIN}$port${Uris.Projects.makeLabels(100)}"

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com", "soubuefixe")
        headers.contentType = DawJsonModel.MEDIA_TYPE
        val project = UpdateProjectEntity(
            100,
            null,
            null,
            arrayOf("update", "tests")
        )
        val req = HttpEntity<String>(project.serializeJsonTo<UpdateProjectEntity>(), headers)
        val res = client.exchange(url, HttpMethod.PUT, req, String::class.java)

        Assertions.assertEquals(PROJECT_TO_ADD_LABELS_NOT_FOUND, res.body)
        Assertions.assertEquals(ProblemJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.NOT_FOUND, res.statusCode)
    }

    @Test
    fun `Project to update with null body params`() {
        Assertions.assertNotNull(client)
        val url = "${Utils.DOMAIN}$port${Uris.Projects.makeSingle(1)}"

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com", "soubuefixe")
        headers.contentType = DawJsonModel.MEDIA_TYPE
        val project = UpdateProjectEntity(
            1,
            null,
            null,
            null
        )
        val req = HttpEntity<String>(project.serializeJsonTo<UpdateProjectEntity>(), headers)
        val res = client.exchange(url, HttpMethod.PUT, req, String::class.java)

        Assertions.assertEquals(UPDATE_PROJECT_WITH_NULL_PARAMS, res.body)
        Assertions.assertEquals(ProblemJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, res.statusCode)
    }

    @Test
    fun `Project to update without body params`() {
        Assertions.assertNotNull(client)
        val url = "${Utils.DOMAIN}$port${Uris.Projects.makeSingle(1)}"

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com", "soubuefixe")

        val req = HttpEntity<String>(headers)
        val res = client.exchange(url, HttpMethod.PUT, req, String::class.java)

        Assertions.assertEquals(UPDATE_PROJECT_WITHOUT_BODY, res.body)
        Assertions.assertEquals(ProblemJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, res.statusCode)
    }

    @Test
    fun `Project to create without body`() {
        Assertions.assertNotNull(client)
        val url = "${Utils.DOMAIN}$port${Uris.Projects.PATH}"

        val headers = HttpHeaders()
        headers.setBasicAuth("zezinho@hotmail.com", "souboapessoa")
        headers.contentType = MediaType.APPLICATION_JSON

        val req = HttpEntity<String>(headers)
        val res = client.exchange(url, HttpMethod.POST, req, String::class.java)

        Assertions.assertEquals(CREATE_PROJECT_WITHOUT_VALID_BODY, res.body)
        Assertions.assertEquals(ProblemJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, res.statusCode)
    }

    @Test
    fun `Project to create with a missing body parameter`() {
        Assertions.assertNotNull(client)
        val url = "${Utils.DOMAIN}$port${Uris.Projects.PATH}"

        val headers = HttpHeaders()
        headers.setBasicAuth("zezinho@hotmail.com", "souboapessoa")
        headers.contentType = MediaType.APPLICATION_JSON
        val body = "{\n" +
                "    \"name\": \"Construção de rede no Estádio da Luz\",\n" +
                "    \"description\": \"Construção de rede para proteger do arremesso de materiais perigosos para o relvado\",\n" +
                "    \"labels\": [\"ltest1\", \"ltest2\", \"ltest3\"],\n" +
                "    \"states\": [\"stest1\", \"stest2\", \"stest3\"],\n" +

                "    \"statesTransitions\": [\"stest1\", \"stest2\", \"stest2\", \"stest3\"]\n" +
                "}"
        val req = HttpEntity<String>(body, headers)
        val res = client.postForEntity(url, req, String::class.java)

        Assertions.assertEquals(CREATE_PROJECT_WITHOUT_VALID_BODY, res.body)
        Assertions.assertEquals(ProblemJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, res.statusCode)
    }

    @Test
    fun `Project to create with a mismatch body parameter`() {
        Assertions.assertNotNull(client)
        val url = "${Utils.DOMAIN}$port${Uris.Projects.PATH}"

        val headers = HttpHeaders()
        headers.setBasicAuth("zezinho@hotmail.com", "souboapessoa")
        headers.contentType = MediaType.APPLICATION_JSON

        val body = "{\n" +
                "    \"name\": \"Construção de rede no Estádio da Luz\",\n" +
                "    \"description\": \"Construção de rede para proteger do arremesso de materiais perigosos para o relvado\",\n" +
                "    \"labels\": 1,\n" +
                "    \"states\": [\"stest1\", \"stest2\", \"stest3\"],\n" +
                "    \"initialState\": \"stest3\",\n" +
                "    \"statesTransitions\": [\"stest1\", \"stest2\", \"stest2\", \"stest3\"]\n" +
                "}"
        val req = HttpEntity<String>(body, headers)
        val res = client.postForEntity(url, req, String::class.java)

        Assertions.assertEquals(CREATE_PROJECT_WITHOUT_VALID_BODY, res.body)
        Assertions.assertEquals(ProblemJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, res.statusCode)
    }

    @Test
    fun `Create project with initialState that does not belong to states array`() {
        Assertions.assertNotNull(client)
        val url = "${Utils.DOMAIN}$port${Uris.Projects.PATH}"

        val headers = HttpHeaders()
        headers.setBasicAuth("zezinho@hotmail.com", "souboapessoa")
        headers.contentType = MediaType.APPLICATION_JSON
        val project = CreateProjectEntity(
            "Construção de rede no Estádio da Luz",
            "Construção de rede para proteger do arremesso de materiais perigosos para o relvado",
            arrayOf("ltest1", "ltest2", "ltest3"),
            arrayOf("stest1", "stest2", "stest3"),
            "rip",
            arrayOf("stest1", "stest2", "stest2", "stest3")
        )

        val req = HttpEntity<String>(project.serializeJsonTo<CreateProjectEntity>(), headers)
        val res = client.postForEntity(url, req, String::class.java)

        Assertions.assertEquals(INIT_STATE_DOESNT_BELONG_IN_STATES, res.body)
        Assertions.assertEquals(ProblemJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, res.statusCode)
    }

    @Test
    fun `Create project with odd transitions array length`() {
        Assertions.assertNotNull(client)
        val url = "${Utils.DOMAIN}$port${Uris.Projects.PATH}"

        val headers = HttpHeaders()
        headers.setBasicAuth("zezinho@hotmail.com", "souboapessoa")
        headers.contentType = MediaType.APPLICATION_JSON
        val project = CreateProjectEntity(
            "Construção de rede no Estádio da Luz",
            "Construção de rede para proteger do arremesso de materiais perigosos para o relvado",
            arrayOf("ltest1", "ltest2", "ltest3"),
            arrayOf("stest1", "stest2", "stest3"),
            "stest3",
            arrayOf("stest1", "stest2", "stest2")
        )

        val req = HttpEntity<String>(project.serializeJsonTo<CreateProjectEntity>(), headers)
        val res = client.postForEntity(url, req, String::class.java)

        Assertions.assertEquals(ODD_TRANSITIONS_ARRAY_LENGTH, res.body)
        Assertions.assertEquals(ProblemJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, res.statusCode)
    }

    @Test
    fun `Create project with transitions that doesnt belong to states array`() {
        Assertions.assertNotNull(client)
        val url = "${Utils.DOMAIN}$port${Uris.Projects.PATH}"

        val headers = HttpHeaders()
        headers.setBasicAuth("zezinho@hotmail.com", "souboapessoa")
        headers.contentType = MediaType.APPLICATION_JSON
        val project = CreateProjectEntity(
            "Construção de rede no Estádio da Luz",
            "Construção de rede para proteger do arremesso de materiais perigosos para o relvado",
            arrayOf("ltest1", "ltest2", "ltest3"),
            arrayOf("stest1", "stest2", "stest3"),
            "stest3",
            arrayOf("stest1", "stest2", "stest2", "stest99")
        )

        val req = HttpEntity<String>(project.serializeJsonTo<CreateProjectEntity>(), headers)
        val res = client.postForEntity(url, req, String::class.java)

        Assertions.assertEquals(TRANSITION_STATE_NOT_IN_STATES, res.body)
        Assertions.assertEquals(ProblemJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, res.statusCode)
    }
}