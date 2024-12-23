import { FetchResult } from "../hooks/useFetch"
import * as DawJson from "./DawJsonModel"

export function getEntityOrUndefined<T>(result?: FetchResult<T>): DawJson.Entity<T> | undefined {
    switch (result?.type) {
        case 'success': return result.entity
        case 'problem': return undefined
    }
}

export function getPropertiesOrUndefined<T>(result?: FetchResult<T>): T | undefined {
    switch (result?.type) {
        case 'success': return result.entity.properties
        case 'problem': return undefined
    }
}

export function getEntitiesOrUndefined<T>(result?: FetchResult<T>): DawJson.Entity<any>[] | undefined {
    switch (result?.type) {
        case 'success': return result.entity.entities
        case 'problem': return undefined
    }
}

export function getActionsOrUndefined<T>(result?: FetchResult<T>): DawJson.Action[] | undefined {
    switch (result?.type) {
        case 'success': return result.entity.actions
        case 'problem': return undefined
    }
}

export function getAction<T>(actionName: string, result?: FetchResult<T>): DawJson.Action | undefined {
    const entity = getEntityOrUndefined(result)
    return entity?.actions?.find(action => action.name === actionName)
}

export function getProblemOrUndefined<T>(result?: FetchResult<T>) {
    switch (result?.type) {
        case 'success': return undefined
        case 'problem': return result.problem
    }
}