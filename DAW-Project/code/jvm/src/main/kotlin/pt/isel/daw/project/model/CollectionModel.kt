package pt.isel.daw.project.model

data class CollectionModel(
    val pageIndex: Int,
    val pageMaxSize: Int,
    val collectionSize: Int,
)

data class PaginationEntity(
    val limit: Int,
    var offset: Int,
)

data class PaginationDto(
    val page: Int,
    val limit: Int,
) {
    companion object {
        const val DEFAULT_PAGE = 0
        const val DEFAULT_LIMIT = 10
    }
}

fun PaginationDto.toEntity() = PaginationEntity(limit, page * limit)