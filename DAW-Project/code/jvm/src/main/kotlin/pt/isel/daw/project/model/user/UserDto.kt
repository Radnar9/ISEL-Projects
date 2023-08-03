package pt.isel.daw.project.model.user

import java.util.*

data class UserDto(val id: UUID, val name: String, val email: String)

data class UserSession(val name: String, val email: String, val token: UUID)