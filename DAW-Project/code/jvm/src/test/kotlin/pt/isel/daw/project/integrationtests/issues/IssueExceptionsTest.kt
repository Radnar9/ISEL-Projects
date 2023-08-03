package pt.isel.daw.project.integrationtests.issues

import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.*
import pt.isel.daw.project.integrationtests.comments.CommentExpectedRepresentations
import pt.isel.daw.project.model.DawJsonModel
import pt.isel.daw.project.model.ProblemJsonModel
import pt.isel.daw.project.model.Uris
import pt.isel.daw.project.model.issue.CreateIssueEntity
import pt.isel.daw.project.model.issue.UpdateIssueEntity
import pt.isel.daw.project.model.project.CreateProjectEntity
import pt.isel.daw.project.utils.Utils
import pt.isel.daw.project.utils.serializeJsonTo

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IssueExceptionsTest {
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
    fun `issueId path parameter not an integer`() {
        Assertions.assertNotNull(client)
        val url = "${Utils.DOMAIN}$port${Uris.Projects.PATH}/1/issues/abc"

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com", "soubuefixe")

        val res = client.exchange(url, HttpMethod.GET, HttpEntity<String>(headers), String::class.java)

        Assertions.assertEquals(IssueExpectedRepresentations.INVALID_ISSUEID_PATH_PARAM_TYPE, res.body)
        Assertions.assertEquals(ProblemJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, res.statusCode)
    }

    @Test
    fun `try update a nonexistent issue`() {
        Assertions.assertNotNull(client)
        val url = Uris.Issues.makeSingle(projectId = 1, issueId = 100)

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com", "soubuefixe")

        val res = client.exchange(url, HttpMethod.GET, HttpEntity<String>(headers), String::class.java)

        Assertions.assertEquals(IssueExpectedRepresentations.ISSUE_NOT_FOUND, res.body)
        Assertions.assertEquals(ProblemJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.NOT_FOUND, res.statusCode)
    }

    @Test
    fun `try delete a nonexistent issue`() {
        Assertions.assertNotNull(client)
        val url = Uris.Issues.makeSingle(projectId = 1, issueId = 100)

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com", "soubuefixe")

        val res = client.exchange(url, HttpMethod.DELETE, HttpEntity<String>(headers), String::class.java)

        Assertions.assertEquals(IssueExpectedRepresentations.ISSUE_NOT_FOUND, res.body)
        Assertions.assertEquals(ProblemJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.NOT_FOUND, res.statusCode)
    }

    @Test
    fun `try create an issue without body request`() {
        Assertions.assertNotNull(client)
        val url = Uris.Issues.makeMultiple(projectId = 2)

        val headers = HttpHeaders()
        headers.setBasicAuth("pedrocas@outlook.com", "soubuerico")
        headers.contentType = MediaType.APPLICATION_JSON

        val req = HttpEntity<String>(null, headers)
        val res = client.postForEntity(url, req, String::class.java)

        Assertions.assertEquals(IssueExpectedRepresentations.NO_BODY_ISSUE_CREATE, res.body)
        Assertions.assertEquals(MediaType.APPLICATION_PROBLEM_JSON, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, res.statusCode)
    }

    @Test
    fun `try update an issue without body request`() {
        Assertions.assertNotNull(client)
        val url = Uris.Issues.makeSingle(projectId = 3, issueId = 4)

        val headers = HttpHeaders()
        headers.setBasicAuth("zezinho@hotmail.com", "souboapessoa")

        val req = HttpEntity<String>(null, headers)
        val res = client.exchange(url, HttpMethod.PUT, req, String::class.java)

        Assertions.assertEquals(IssueExpectedRepresentations.NO_BODY_ISSUE_UPDATE, res.body)
        Assertions.assertEquals(MediaType.APPLICATION_PROBLEM_JSON, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, res.statusCode)
    }

    @Test
    fun `Try update an issue with all parameters `() {
        Assertions.assertNotNull(client)
        val url = Uris.Issues.makeSingle(projectId = 1, issueId = 1)

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com", "soubuefixe")
        headers.contentType = DawJsonModel.MEDIA_TYPE

        val issue = UpdateIssueEntity(
            1,
            null,
            null,
            null
        )
        val req = HttpEntity<String>(issue.serializeJsonTo<UpdateIssueEntity>(), headers)
        val res = client.exchange(url, HttpMethod.PUT, req, String::class.java)

        Assertions.assertEquals(IssueExpectedRepresentations.ALL_PARAMETERS_NULL, res.body)
        Assertions.assertEquals(MediaType.APPLICATION_PROBLEM_JSON, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, res.statusCode)
    }
}