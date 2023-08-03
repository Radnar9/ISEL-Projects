package pt.isel.daw.project.dao

import org.jdbi.v3.core.statement.OutParameters
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.customizer.OutParameter
import org.jdbi.v3.sqlobject.statement.SqlCall
import org.jdbi.v3.sqlobject.statement.SqlQuery
import pt.isel.daw.project.model.user.UserCredentials
import java.util.*

interface UserDao {

    @SqlCall("CALL validate_user_credentials(:userRep, :email, :password);")
    @OutParameter(name = "userRep", sqlType = java.sql.Types.OTHER)
    fun validateUserCredentials(@BindBean userCredentials: UserCredentials): OutParameters

    @SqlQuery("SELECT get_user_info(:userToken);")
    fun getUserInfo(userToken: UUID): String
}