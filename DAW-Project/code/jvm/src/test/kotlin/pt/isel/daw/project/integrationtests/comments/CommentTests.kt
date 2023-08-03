package pt.isel.daw.project.integrationtests.comments

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
import pt.isel.daw.project.model.comment.CreateCommentEntity
import pt.isel.daw.project.model.comment.UpdateCommentEntity
import pt.isel.daw.project.utils.Utils
import pt.isel.daw.project.utils.serializeJsonTo

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommentTests {

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
    fun `Get comments of an issue`() {
        Assertions.assertNotNull(client)
        val url = Uris.Comment.makeMultiple(projectId = 1, issueId = 1)

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com", "soubuefixe")

        val res = client.exchange(url, HttpMethod.GET, HttpEntity<String>(headers), String::class.java)

        Assertions.assertEquals(CommentExpectedRepresentations.GET_COMMENTS, res.body)
        Assertions.assertEquals(DawJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.OK, res.statusCode)
    }

    @Test
    fun `Get empty list of comments`() {
        Assertions.assertNotNull(client)
        val url = Uris.Comment.makeMultiple(projectId = 2, issueId = 3)

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com", "soubuefixe")

        val res = client.exchange(url, HttpMethod.GET, HttpEntity<String>(headers), String::class.java)

        Assertions.assertEquals(CommentExpectedRepresentations.GET_EMPTY_COMMENTS, res.body)
        Assertions.assertEquals(DawJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.OK, res.statusCode)
    }

    @Test
    fun `Get a comment of an issue`() {
        Assertions.assertNotNull(client)
        val url = Uris.Comment.makeSingle(projectId = 1, issueId = 1, commentId = 1)

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com", "soubuefixe")

        val res = client.exchange(url, HttpMethod.GET, HttpEntity<String>(headers), String::class.java)

        Assertions.assertEquals(CommentExpectedRepresentations.GET_COMMENT, res.body)
        Assertions.assertEquals(DawJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.OK, res.statusCode)
    }

    @Test
    fun `Create a comment`() {
        Assertions.assertNotNull(client)
        val url = Uris.Comment.makeMultiple(projectId = 2, issueId = 3)

        val headers = HttpHeaders()
        headers.setBasicAuth("pedrocas@outlook.com", "soubuerico")
        headers.contentType = DawJsonModel.MEDIA_TYPE

        val comment = CreateCommentEntity("Comment Test")
        val req = HttpEntity<String>(comment.serializeJsonTo<CreateCommentEntity>(), headers)
        val res = client.postForEntity(url, req, String::class.java)

        Assertions.assertEquals(CommentExpectedRepresentations.CREATE_COMMENT, res.body!!.removeRange(79, 92))
        Assertions.assertEquals(DawJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.CREATED, res.statusCode)
        Assertions.assertNotNull(res.headers.location)
    }

    @Test
    fun `Update a comment`() {
        Assertions.assertNotNull(client)
        val url = Uris.Comment.makeSingle(projectId = 3,4,3)

        val headers = HttpHeaders()
        headers.setBasicAuth("zezinho@hotmail.com", "souboapessoa")
        headers.contentType = DawJsonModel.MEDIA_TYPE

        val comment = UpdateCommentEntity(1, "Comment Test: we love Lisbon")
        val req = HttpEntity<String>(comment.serializeJsonTo<CreateCommentEntity>(), headers)
        val res = client.exchange(url, HttpMethod.PUT, req, String::class.java)

        Assertions.assertEquals(CommentExpectedRepresentations.UPDATE_COMMENT, res.body)
        Assertions.assertEquals(DawJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.OK, res.statusCode)
    }
}