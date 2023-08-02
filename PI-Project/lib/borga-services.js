const error = require('./errors/application-errors')
const validator = require('./utils/validator')

module.exports = function (gamesData, data) {
    if (!validator.isObjectAndNotNull(gamesData) || !validator.isObjectAndNotNull(data)) {
        throw "Invalid or missing argument for gamesData or dataMem"
    }

    return {
        getPopularGames,
        searchGames,
        createUser,
        getUser,
        createGroup,
        updateGroup: validated(updateGroup),
        getGroups,
        deleteGroup,
        addGameToGroup,
        getGroupDetails,
        deleteGroupGame,
        getGameDetails,
        validateCredentials
    }

    function validated(fun) {
        return function(...args) {
            return validateToken(args[0]).then(() => fun.apply(this, args))
        }
    }

    function getPopularGames(skip, limit) {
        const pagingParams = verifyPagingParameters(skip, limit)
        if (!pagingParams.inRange) {
            return Promise.reject(error.INVALID_QUERY_PARAMETERS)
        }
        return gamesData.getPopularGames(pagingParams.skip, pagingParams.limit)
    }

    function searchGames(name) {
        if (!name || name.length > 50) {
            return Promise.reject(error.INVALID_GAME_NAME)
        }
        return gamesData.searchGames(name)
    }

    function getUser(token) {
        if (!token || !validator.isGuid(token)) {
            return Promise.reject(error.INVALID_GUID_TOKEN)
        }
        return data.getUserDetails(token)
    }

    function createUser(user) {
        if (user.username && user.password && user.confirmPassword && user.name && user.club) {
            if (user.password !== user.confirmPassword) {
                return Promise.reject(error.CONFIRM_PASSWORD_MISMATCH)
            }
            return data.createUser(user)
        }
        return Promise.reject(error.INVALID_USER_INPUT)
    }

    function createGroup(token, group) {
        if (!token || !validator.isGuid(token)) {
            return Promise.reject(error.INVALID_GUID_TOKEN)
        }
        if (group.name && group.description) {
            return data.createGroup(token, group.name, group.description)
        }
        return Promise.reject(error.INVALID_GROUP_INPUT)
    }

    function updateGroup(token, groupId, group) {
        if (!token || !validator.isGuid(token)) {
            return Promise.reject(error.INVALID_GUID_TOKEN)
        }
        if (isNaN(groupId) || groupId < 0) {
            return Promise.reject(error.INVALID_GROUP_ID)
        }
        if (group && (group.name || group.description)) {
            return data.updateGroup(token, Number(groupId), group.name, group.description)
        }
        return Promise.reject(error.INVALID_GROUP_INPUT)
    }

    function getGroups(token) {
        if (!token || !validator.isGuid(token)) {
            return Promise.reject(error.INVALID_GUID_TOKEN)
        }
        return data.getGroups(token)
    }

    function deleteGroup(token, groupId) {
        if (!token || !validator.isGuid(token)) {
            return Promise.reject(error.INVALID_GUID_TOKEN)
        }
        if (isNaN(groupId) || groupId < 0) {
            return Promise.reject(error.INVALID_GROUP_ID)
        }        
        return data.deleteGroup(token, Number(groupId))
    }

    function getGroupDetails(token, groupId) {
        if (!token || !validator.isGuid(token)) {
            return Promise.reject(error.INVALID_GUID_TOKEN)
        }
        if (isNaN(groupId) || groupId < 0) {
            return Promise.reject(error.INVALID_GROUP_ID)
        }
        return data.getGroupDetails(token, Number(groupId))
    }

    function addGameToGroup(token, groupId, gameId) {
        if (!token || !validator.isGuid(token)) {
            return Promise.reject(error.INVALID_GUID_TOKEN)
        }
        if (isNaN(groupId) || groupId < 0) {
            return Promise.reject(error.INVALID_GROUP_ID)
        }
        if (!gameId) {
            return Promise.reject(error.INVALID_GAME_INPUT)
        }
        groupId = Number(groupId)
        return data.verifyTokenAndGroup(token, groupId)
            .then(() => data.existsGame(gameId)
                .then(() => {return {id: gameId}}, () => gamesData.getGameById(gameId)
                    .then(game => data.addGameToGroup(token, groupId, game), (reason) => Promise.reject(reason))))
    }

    function deleteGroupGame(token, groupId, gameId) {
        if (!token || !validator.isGuid(token)) {
            return Promise.reject(error.INVALID_GUID_TOKEN)
        }
        if (isNaN(groupId) || groupId < 0) {
            return Promise.reject(error.INVALID_GROUP_ID)
        }
        if (!gameId) {
            return Promise.reject(error.INVALID_GAME_ID)
        }
        return data.deleteGroupGame(token, Number(groupId), gameId)
    }

    function getGameDetails(gameId) {
        if (!gameId) {
            return Promise.reject(error.INVALID_GAME_ID)
        }
        return data.getGameDetails(gameId)
            .catch(() => gamesData.getGameById(gameId))
    }

    function validateCredentials(username, password) {
        return data.validateCredentials(username, password)
    }
}

function verifyPagingParameters(skip, limit) {
    if (isNaN(skip)) {
        skip = 0
    }
    if (isNaN(limit)) {
        limit = 10
    }
    let inRange = true
    if (skip < 0 || skip > 50 || limit < 0 || limit > 50) {
        inRange = false
    }
    return {skip: skip, limit: limit, inRange: inRange}
}