package pt.isel.daw.project.integrationtests.project

import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.*
import pt.isel.daw.project.integrationtests.project.ProjectExpectedRepresentations.ADD_LABELS_TO_PROJECT
import pt.isel.daw.project.integrationtests.project.ProjectExpectedRepresentations.CREATE_PROJECT
import pt.isel.daw.project.integrationtests.project.ProjectExpectedRepresentations.DELETE_PROJECT
import pt.isel.daw.project.integrationtests.project.ProjectExpectedRepresentations.GET_EMPTY_PROJECTS
import pt.isel.daw.project.integrationtests.project.ProjectExpectedRepresentations.GET_PROJECT
import pt.isel.daw.project.integrationtests.project.ProjectExpectedRepresentations.GET_PROJECTS
import pt.isel.daw.project.integrationtests.project.ProjectExpectedRepresentations.UPDATE_PROJECT_DESC
import pt.isel.daw.project.integrationtests.project.ProjectExpectedRepresentations.UPDATE_PROJECT_NAME
import pt.isel.daw.project.integrationtests.project.ProjectExpectedRepresentations.UPDATE_PROJECT_NAME_DESC
import pt.isel.daw.project.model.DawJsonModel
import pt.isel.daw.project.model.Uris
import pt.isel.daw.project.model.project.CreateProjectEntity
import pt.isel.daw.project.model.project.UpdateProjectEntity
import pt.isel.daw.project.utils.Utils
import pt.isel.daw.project.utils.Utils.DOMAIN
import pt.isel.daw.project.utils.serializeJsonTo

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProjectTests {

    // We need to use field injection because construction is done by JUnit and not Spring context
    @Autowired
    private lateinit var client: TestRestTemplate

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var jdbi: Jdbi

    private val delScript = Utils.LoadScript.getResourceFile("sql/deleteTables.sql")
    private val fillScript = Utils.LoadScript.getResourceFile("sql/insertTables.sql")

    @BeforeEach
    fun setUp() {
        jdbi.open().use { h -> h.createScript(delScript).execute(); h.createScript(fillScript).execute() }
    }

    @AfterAll
    fun cleanUp() {
        jdbi.open().use { h -> h.createScript(delScript).execute();}
    }

    @Test
    fun `Get user projects`() {
        assertNotNull(client)
        val url = "$DOMAIN$port${Uris.Projects.PATH}"

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com", "soubuefixe")

        val res = client.exchange(url, HttpMethod.GET, HttpEntity<String>(headers), String::class.java)

        assertEquals(GET_PROJECTS, res.body)
        assertEquals(DawJsonModel.MEDIA_TYPE, res.headers.contentType)
        assertEquals(HttpStatus.OK, res.statusCode)
    }

    @Test
    fun `Get user without projects`() {
        assertNotNull(client)
        val url = "$DOMAIN$port${Uris.Projects.PATH}"

        val headers = HttpHeaders()
        headers.setBasicAuth("guizado@hotmail.com", "souoronaldo")

        val res = client.exchange(url, HttpMethod.GET, HttpEntity<String>(headers), String::class.java)

        assertEquals(GET_EMPTY_PROJECTS, res.body)
        assertEquals(DawJsonModel.MEDIA_TYPE, res.headers.contentType)
        assertEquals(HttpStatus.OK, res.statusCode)
    }

    @Test
    fun `Create a project`() {
        assertNotNull(client)
        val url = "$DOMAIN$port${Uris.Projects.PATH}"

        val headers = HttpHeaders()
        headers.setBasicAuth("zezinho@hotmail.com", "souboapessoa")
        headers.contentType = MediaType.APPLICATION_JSON
        val project = CreateProjectEntity(
            "Construção de rede no Estádio da Luz",
            "Construção de rede para proteger do arremesso de materiais perigosos para o relvado",
            arrayOf("ltest1", "ltest2", "ltest3"),
            arrayOf("stest1", "stest2", "closed", "archived"),
            "stest1",
            arrayOf("stest1", "stest2", "stest2", "closed", "closed", "archived")
        )
        val req = HttpEntity<String>(project.serializeJsonTo<CreateProjectEntity>(), headers)
        val res = client.postForEntity(url, req, String::class.java)

        assertEquals(CREATE_PROJECT, res.body)
        assertEquals(DawJsonModel.MEDIA_TYPE, res.headers.contentType)
        assertEquals(HttpStatus.CREATED, res.statusCode)
        assertNotNull(res.headers.location)
    }

    @Test
    fun `Get a user project`() {
        assertNotNull(client)
        val url = "$DOMAIN$port${Uris.Projects.makeSingle(1)}"

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com", "soubuefixe")

        val res = client.exchange(url, HttpMethod.GET, HttpEntity<String>(headers), String::class.java)

        assertEquals(GET_PROJECT, res.body)
        assertEquals(DawJsonModel.MEDIA_TYPE, res.headers.contentType)
        assertEquals(HttpStatus.OK, res.statusCode)
    }

    @Test
    fun `Update a project with new name and description`() {
        assertNotNull(client)
        val url = "$DOMAIN$port${Uris.Projects.makeSingle(1)}"

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com", "soubuefixe")
        headers.contentType = DawJsonModel.MEDIA_TYPE
        val project = UpdateProjectEntity(
            1,
            "Make tests",
            "Integration Tests",
            null
        )
        val req = HttpEntity<String>(project.serializeJsonTo<UpdateProjectEntity>(), headers)
        val res = client.exchange(url, HttpMethod.PUT, req, String::class.java)

        assertEquals(UPDATE_PROJECT_NAME_DESC, res.body)
        assertEquals(DawJsonModel.MEDIA_TYPE, res.headers.contentType)
        assertEquals(HttpStatus.OK, res.statusCode)
    }

    @Test
    fun `Update project with only a new name`() {
        assertNotNull(client)
        val url = "$DOMAIN$port${Uris.Projects.makeSingle(1)}"

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com", "soubuefixe")
        headers.contentType = DawJsonModel.MEDIA_TYPE
        val project = UpdateProjectEntity(
            1,
            "Make unit tests",
            null,
            null
        )
        val req = HttpEntity<String>(project.serializeJsonTo<UpdateProjectEntity>(), headers)
        val res = client.exchange(url, HttpMethod.PUT, req, String::class.java)

        assertEquals(UPDATE_PROJECT_NAME, res.body)
        assertEquals(DawJsonModel.MEDIA_TYPE, res.headers.contentType)
        assertEquals(HttpStatus.OK, res.statusCode)
    }

    @Test
    fun `Update project with only a new description`() {
        assertNotNull(client)
        val url = "$DOMAIN$port${Uris.Projects.makeSingle(1)}"

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com", "soubuefixe")
        headers.contentType = DawJsonModel.MEDIA_TYPE
        val project = UpdateProjectEntity(
            1,
            null,
            "We love testing!!!",
            null
        )
        val req = HttpEntity<String>(project.serializeJsonTo<UpdateProjectEntity>(), headers)
        val res = client.exchange(url, HttpMethod.PUT, req, String::class.java)

        assertEquals(UPDATE_PROJECT_DESC, res.body)
        assertEquals(DawJsonModel.MEDIA_TYPE, res.headers.contentType)
        assertEquals(HttpStatus.OK, res.statusCode)
    }

    @Test
    fun `Delete project`() {
        assertNotNull(client)
        val url = "$DOMAIN$port${Uris.Projects.makeSingle(1)}"

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com", "soubuefixe")
        headers.contentType = DawJsonModel.MEDIA_TYPE

        val req = HttpEntity<String>(headers)
        val res = client.exchange(url, HttpMethod.DELETE, req, String::class.java)

        assertEquals(DELETE_PROJECT, res.body)
        assertEquals(DawJsonModel.MEDIA_TYPE, res.headers.contentType)
        assertEquals(HttpStatus.OK, res.statusCode)
    }

    @Test
    fun `Add labels to project`() {
        assertNotNull(client)
        val url = "$DOMAIN$port${Uris.Projects.makeLabels(1)}"

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com", "soubuefixe")
        headers.contentType = DawJsonModel.MEDIA_TYPE
        val project = UpdateProjectEntity(
            1,
            null,
            null,
            arrayOf("update", "tests")
        )
        val req = HttpEntity<String>(project.serializeJsonTo<UpdateProjectEntity>(), headers)
        val res = client.exchange(url, HttpMethod.PUT, req, String::class.java)

        assertEquals(ADD_LABELS_TO_PROJECT, res.body)
        assertEquals(DawJsonModel.MEDIA_TYPE, res.headers.contentType)
        assertEquals(HttpStatus.OK, res.statusCode)
    }
}