package pt.isel.daw.project.controller

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import pt.isel.daw.project.model.Uris
import pt.isel.daw.project.model.user.UserDto
import pt.isel.daw.project.model.user.UserSession


@RestController
@CrossOrigin(origins = ["http://localhost:3000"], allowedHeaders = ["*"], allowCredentials = "true")
class UserController {

    @ResponseBody
    @PostMapping(Uris.Users.LOGIN_PATH)
    fun login(user: UserSession): ResponseEntity<Any> {
        val headers = HttpHeaders()
        return ResponseEntity
            .status(HttpStatus.OK)
            .headers(headers)
            .body(user)
    }

    @PostMapping(Uris.Users.LOGOUT_PATH)
    fun logout(): ResponseEntity<Any> {
        val headers = HttpHeaders()
        return ResponseEntity.status(HttpStatus.OK).headers(headers).build()
    }
}