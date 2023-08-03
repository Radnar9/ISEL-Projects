package pt.isel.daw.project.model.project

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.daw.project.model.issue.IssueItemDto
import java.sql.ResultSet

class ProjectMapper {/*: RowMapper<ProjectDto> {
    override fun map(rs: ResultSet, ctx: StatementContext): ProjectDto {
        var id = 0
        var name = ""
        var description = ""
        val issues: MutableList<IssueItemDto> = arrayListOf()
        println("CONNECTION--->${ctx.connection.isClosed}")

        while (rs.next()) {
            val aux = rs.getInt(1)
            if (id != aux) {
                id = aux
                name = rs.getString(2)
                description = rs.getString(3)
            }
            val issueId = rs.getInt(4)
            if (issueId != 0) {
                val a = rs.getArray(7) // How to transform Array into List
                issues.add(
                    IssueItemDto(
                        issueId,
                        rs.getString(5),
                        rs.getString(6),
                        a
                    )
                )
            }
        }
        println("CONNECTION--->${ctx.connection.isClosed}")
        ctx.use {

        }
        println("CONNECTION--->${ctx.connection.isClosed}")

        return ProjectDto(id, name, description, issues)
        return ProjectDto(
            1, "", "",
            arrayListOf()
        )
    }*/
}
