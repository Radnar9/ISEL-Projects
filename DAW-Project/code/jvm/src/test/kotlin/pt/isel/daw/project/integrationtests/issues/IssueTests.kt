package pt.isel.daw.project.integrationtests.issues

import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import pt.isel.daw.project.model.DawJsonModel
import pt.isel.daw.project.model.Uris
import pt.isel.daw.project.model.issue.CreateIssueEntity
import pt.isel.daw.project.model.issue.UpdateIssueEntity
import pt.isel.daw.project.model.project.CreateProjectEntity
import pt.isel.daw.project.model.project.UpdateProjectEntity
import pt.isel.daw.project.utils.Utils
import pt.isel.daw.project.utils.serializeJsonTo

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IssueTests {

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
        jdbi.open().use { h -> h.createScript(delScript).execute(); }
    }

    @Test
    fun `Get issues of a project`() {
        Assertions.assertNotNull(client)
        val url = Uris.Issues.makeMultiple(projectId = 1)

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com", "soubuefixe")

        val res = client.exchange(url, HttpMethod.GET, HttpEntity<String>(headers), String::class.java)

        Assertions.assertEquals(IssueExpectedRepresentations.GET_ISSUES, res.body)
        Assertions.assertEquals(DawJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.OK, res.statusCode)
    }

    @Test
    fun `Get empty list of issues`() {
        Assertions.assertNotNull(client)
        val url = Uris.Issues.makeMultiple(projectId = 4)

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com", "soubuefixe")

        val res = client.exchange(url, HttpMethod.GET, HttpEntity<String>(headers), String::class.java)

        Assertions.assertEquals(IssueExpectedRepresentations.GET_EMPTY_ISSUES, res.body)
        Assertions.assertEquals(DawJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.OK, res.statusCode)
    }

    @Test
    fun `Create an issue with labels`() {
        Assertions.assertNotNull(client)
        val url = Uris.Issues.makeMultiple(projectId = 1)

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com", "soubuefixe")
        headers.contentType = DawJsonModel.MEDIA_TYPE
        val issue = CreateIssueEntity(
            "Custo da rede",
            "O clube está em retenção de custos, devemos colocar uma rede simples",
            arrayOf(1,2)
        )
        val req = HttpEntity<String>(issue.serializeJsonTo<CreateProjectEntity>(), headers)
        val res = client.postForEntity(url, req, String::class.java)

        Assertions.assertEquals(IssueExpectedRepresentations.CREATE_ISSUE_WITH_LABELS, res.body!!.removeRange(168, 181))
        Assertions.assertEquals(DawJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.CREATED, res.statusCode)
        Assertions.assertNotNull(res.headers.location)
    }

    @Test
    fun `Create an issue without labels`() {
        Assertions.assertNotNull(client)
        val url = Uris.Issues.makeMultiple(projectId = 1)

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com", "soubuefixe")
        headers.contentType = DawJsonModel.MEDIA_TYPE
        val issue = CreateIssueEntity(
            "Custo da rede",
            "O clube está em retenção de custos, devemos colocar uma rede simples",
            null
        )
        val req = HttpEntity<String>(issue.serializeJsonTo<CreateProjectEntity>(), headers)
        val res = client.postForEntity(url, req, String::class.java)

        Assertions.assertEquals(IssueExpectedRepresentations.CREATE_ISSUE_WITHOUT_LABELS, res.body!!.removeRange(168, 181))
        Assertions.assertEquals(DawJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.CREATED, res.statusCode)
        Assertions.assertNotNull(res.headers.location)
    }

    @Test
    fun `Get a project issue`() {
        Assertions.assertNotNull(client)
        val url = Uris.Issues.makeSingle(projectId = 1, issueId = 1)

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com", "soubuefixe")

        val res = client.exchange(url, HttpMethod.GET, HttpEntity<String>(headers), String::class.java)
        Assertions.assertEquals(IssueExpectedRepresentations.GET_PROJECT_ISSUE, res.body!!.removeRange(198, 211))
        Assertions.assertEquals(DawJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.OK, res.statusCode)
    }

    @Test
    fun `Update an issue with new name and description`() {
        Assertions.assertNotNull(client)
        val url = Uris.Issues.makeSingle(projectId = 1, issueId = 1)

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com", "soubuefixe")
        headers.contentType = DawJsonModel.MEDIA_TYPE
        val issue = UpdateIssueEntity(
            1,
            "Make tests",
            "Integration Tests",
            null
        )
        val req = HttpEntity<String>(issue.serializeJsonTo<UpdateIssueEntity>(), headers)
        val res = client.exchange(url, HttpMethod.PUT, req, String::class.java)

        Assertions.assertEquals(IssueExpectedRepresentations.UPDATE_ISSUE_NAME_AND_DESCRIPTION, res.body)
        Assertions.assertEquals(DawJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.OK, res.statusCode)
    }

    @Test
    fun `Update issue with only a new name`() {
        Assertions.assertNotNull(client)
        val url = Uris.Issues.makeSingle(projectId = 1, issueId = 1)

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com", "soubuefixe")
        headers.contentType = DawJsonModel.MEDIA_TYPE
        val issue = UpdateIssueEntity(
            1,
            "Make unit tests",
            null,
            null
        )
        val req = HttpEntity<String>(issue.serializeJsonTo<UpdateProjectEntity>(), headers)
        val res = client.exchange(url, HttpMethod.PUT, req, String::class.java)

        Assertions.assertEquals(IssueExpectedRepresentations.UPDATE_ONLY_ISSUE_NAME, res.body)
        Assertions.assertEquals(DawJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.OK, res.statusCode)
    }

    @Test
    fun `Update issue with only a new description`() {
        Assertions.assertNotNull(client)
        val url = Uris.Issues.makeSingle(projectId = 1, issueId = 1)

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com", "soubuefixe")
        headers.contentType = DawJsonModel.MEDIA_TYPE
        val issue = UpdateIssueEntity(
            1,
            null,
            "we love coding!!!!",
            null
        )
        val req = HttpEntity<String>(issue.serializeJsonTo<UpdateProjectEntity>(), headers)
        val res = client.exchange(url, HttpMethod.PUT, req, String::class.java)

        Assertions.assertEquals(IssueExpectedRepresentations.UPDATE_ONLY_ISSUE_DESCRIPTION, res.body)
        Assertions.assertEquals(DawJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.OK, res.statusCode)
    }

    @Test
    fun `Delete issue`() {
        Assertions.assertNotNull(client)
        val url = Uris.Issues.makeSingle(projectId = 1, issueId = 1)

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com", "soubuefixe")
        headers.contentType = DawJsonModel.MEDIA_TYPE

        val req = HttpEntity<String>(headers)
        val res = client.exchange(url, HttpMethod.DELETE, req, String::class.java)

        Assertions.assertEquals(IssueExpectedRepresentations.DELETE_ISSUE, res.body)
        Assertions.assertEquals(DawJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.OK, res.statusCode)
    }
}