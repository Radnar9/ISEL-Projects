package pt.isel.daw.project.integrationtests.argresolvers

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
import pt.isel.daw.project.integrationtests.argresolvers.ArgResolverExpectedRepresentations.INVALID_AUTH_CREDENTIALS
import pt.isel.daw.project.integrationtests.argresolvers.ArgResolverExpectedRepresentations.INVALID_PAGINATION_TYPE
import pt.isel.daw.project.integrationtests.project.ProjectExpectedRepresentations
import pt.isel.daw.project.model.ProblemJsonModel
import pt.isel.daw.project.model.Uris
import pt.isel.daw.project.utils.Utils

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArgumentResolversTest {

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
        jdbi.open().use { h -> h.createScript(delScript).execute(); }
    }

    @Test
    fun `Invalid authorization credentials`() {
        Assertions.assertNotNull(client)
        val url = "${Utils.DOMAIN}$port${Uris.Projects.makeSingle(1)}"

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com123", "soubuefixe")

        val res = client.exchange(url, HttpMethod.GET, HttpEntity<String>(headers), String::class.java)

        Assertions.assertEquals(INVALID_AUTH_CREDENTIALS, res.body)
        Assertions.assertEquals(ProblemJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, res.statusCode)
    }

    @Test
    fun `Invalid pagination type`() {
        Assertions.assertNotNull(client)
        val url = "${Utils.DOMAIN}$port${Uris.Projects.PATH}?page=abc"

        val headers = HttpHeaders()
        headers.setBasicAuth("joca@gmail.com", "soubuefixe")

        val res = client.exchange(url, HttpMethod.GET, HttpEntity<String>(headers), String::class.java)

        Assertions.assertEquals(INVALID_PAGINATION_TYPE, res.body)
        Assertions.assertEquals(ProblemJsonModel.MEDIA_TYPE, res.headers.contentType)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, res.statusCode)
    }
}