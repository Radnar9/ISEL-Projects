import { ReactElement, useMemo, useReducer, useState } from "react";
import { Link, Outlet, useParams } from "react-router-dom"
import { useFetch, FetchResult } from '../../hooks/useFetch'
import { IssueItem, StateItem, IssueItemComponent, Label } from "../issue/Common";
import {  ProjectCard } from "./Common";
import { getAction, getEntityOrUndefined, getActionsOrUndefined, getEntitiesOrUndefined, getProblemOrUndefined, getPropertiesOrUndefined } from "../../models/ModelUtils"
import * as DawJson from '../../models/DawJsonModel'
import { Collection, CollectionPagination } from "../pagination/CollectionPagination";
import { DeleteComponent } from "../DeleteComponent";
import UpdateProject from "./UpdateProject";
import CreateIssue from "../issue/CreateIssue";
import { DisplayError } from "../Error";

export type Project = {
    id: string,
    name: string,
    description: string,
    labels: Array<Label>,
    state: StateItem,
    initialState: StateItem,
}

const PROJECT_URL = (projectId: string | undefined): string => {
    return `http://localhost:8080/v1/projects/${projectId}`
}

const ISSUES_URL = (projectId: string | undefined, page: number): string => {
    return page < 0 ? '' : `http://localhost:8080/v1/projects/${projectId}/issues?page=${page}`
}

export default function ProjectDetails({  } : {  }) {
    
    const { projectId } = useParams();

    const credentials: RequestInit = {
        credentials: "include"
    }
    const init = useMemo(() => credentials ,[])

    const [currentPage, setPage] = useState(-1)
    const [action, setAction] = useState('')

    const { isFetching, isCanceled, cancel, result, error } = useFetch<Project>(PROJECT_URL(projectId), init);
    const { 
        isFetching: areIssuesFetching, 
        isCanceled: areIssuesCanceled, 
        cancel: cancelIssues, 
        result: issuesResult, 
        error: issuesError 
    } = useFetch<IssueItem>(ISSUES_URL(projectId, currentPage), init);

    if (isFetching) return <p>Fetching...</p>
    if (isCanceled) return <p>Canceled</p>
    if (error !== undefined) {
        console.log(error)
        return <DisplayError error={error}/>
    }

    switch (action) {
        case 'delete-project': return <DeleteComponent urlToDelete={PROJECT_URL(projectId)} redirectUrl='/projects' setAction={setAction}/>
        case 'update-project': return <UpdateProject action={getAction('update-project', result?.body)!!} setAction={setAction} />
        case 'create-issue': {
            const issueListEntity = getEntitiesOrUndefined(result?.body)!!.find(entity => entity.class.includes("issue") && entity.class.includes("collection") && entity.rel?.includes("project-issues"))
            return <CreateIssue props={getPropertiesOrUndefined(result?.body)!!} setAction={setAction} action={issueListEntity?.actions?.find(action => action.name == 'create-issue')!!}/>
        }
    }

    function IssueList({ entities }: { entities?: DawJson.Entity<IssueItem>[]}) {
        if (!entities) return null
        let entityPage
        if (issuesResult) entityPage = getEntityOrUndefined(issuesResult.body)!!
        else entityPage = entities.find(entity => entity.class.includes("issue") && entity.class.includes("collection") && entity.rel?.includes("project-issues"))
        const collection = entityPage?.properties
        const items = entityPage?.entities?.map(entity => {
            if (entity.class.includes("issue") && entity.rel?.includes("item")) {
                const issue = entity.properties
                return <IssueItemComponent 
                    key={issue.id} 
                    projectId={projectId}
                    entity={entity} />
            }
        })
        return (<>
            <div className="flex">
                <h1 className="m-5 pl-2 text-lg font-bold">Issues</h1>
                <IssuesActions actions={entityPage?.actions} setAction={setAction}></IssuesActions>
            </div>
            {items && items.length !== 0 && items[0] ?
                <div>
                     <ul className="ml-8 list-none">
                        {items}
                    </ul>
                    <CollectionPagination collection={collection} setPage={setPage} />
                </div>
            : <p className="ml-8">Empty list</p>}
        </>)
    }

    function ProjectActions({ actions, setAction }: { actions?: DawJson.Action[], setAction: React.Dispatch<React.SetStateAction<string>> }) {
        const actionsElements = actions?.map((action, idx) => {
            switch (action.name) {
                case 'update-project': return (
                    <div key={idx} className="mb-2 ml-8">
                        <button onClick={() => setAction('update-project')} className="rounded-lg bg-green-600 p-2 text-white font-bold">
                            Update project
                        </button>
                    </div>
                )
                case 'delete-project': return (
                    <div key={idx} className="mb-2 ml-8">
                        <button onClick={() => setAction('delete-project')} className="rounded-lg bg-red-800 p-2 text-white font-bold">
                            Delete project
                        </button>
                    </div>
                )
            }
        })
        return actionsElements?.length === 0 ? null : <div className="mt-4 inline-flex">{actionsElements}</div>
    }

    function IssuesActions({ actions, setAction }: { actions?: DawJson.Action[], setAction: React.Dispatch<React.SetStateAction<string>> }) {
        const actionsElements = actions?.map((action, idx) => {
            switch (action.name) {
                case 'create-issue': return (
                    <div key={idx} className="mb-2 ml-8">
                        <button onClick={() => setAction('create-issue')} className="rounded-lg bg-sky-400 p-2 text-white font-bold">
                            Create issue
                        </button>
                    </div>
                )
            }
        })
        return actionsElements?.length === 0 ? null : <div className="mt-4 inline-flex">{actionsElements}</div>
    }

    return (<>
        {result?.body?.type === 'problem' ? <DisplayError message={result.body.problem.title}/> :
        <div>  
            <ProjectCard entity={getEntityOrUndefined(result?.body)}>
                <ProjectActions actions={getActionsOrUndefined(result?.body)} setAction={setAction}/>
            </ProjectCard>
            {areIssuesFetching ? null : <IssueList entities={getEntitiesOrUndefined(result?.body)}/>}
        </div>}
    </>)
}