import { useMemo } from "react"
import { useFetch } from "../../hooks/useFetch"
import { DisplayError } from "../Error"
import IssueDetails from "../issue/IssueDetails"
import * as DawJson from '../../models/DawJsonModel'

const BASE_URL = 'http://localhost:8080'

export function DeleteComment({action, projectId, issueId} : {action: DawJson.Action, projectId: number, issueId: number}) {
    const credentials: RequestInit = {
        method: 'DELETE',
        credentials: "include",
    }
    const init = useMemo(() => credentials ,[])
    
    const { isFetching, isCanceled, cancel, result, error } = useFetch<any>(BASE_URL + action.href, init)
    
    return (<>{ !isFetching ? <IssueDetails issueIdArg={issueId} projectIdArg={projectId}/> : null }</>)
    
}