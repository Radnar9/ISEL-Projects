import { ChangeEvent, ReactElement, useMemo, useState } from "react"
import { Link, Navigate, Outlet, useParams } from "react-router-dom"
import * as DawJson from '../../models/DawJsonModel'
import { ProblemJson } from "../../models/ProblemJson"
import { useFetch, FetchResult } from '../../hooks/useFetch'
import CreateProject from "./CreateProject"
import { getAction, getActionsOrUndefined, getEntitiesOrUndefined, getProblemOrUndefined, getPropertiesOrUndefined } from "../../models/ModelUtils"
import { Collection, CollectionPagination } from "../pagination/CollectionPagination"
import { DisplayError } from "../Error"

export const PROJECTS_URL = "http://localhost:8080/v1/projects?page="

type ProjectItem = {
    id: string,
    name: string,
    description: string,
}

function ProjectItemComponent({ entity }: { entity: DawJson.Entity<ProjectItem> }) {
    const project = entity.properties  
    const id = project.id
    return (
        <li className="px-3 pt-3">
            <Link to={`/projects/${id}`}>
                <div className="bg-gray-200 inline-block rounded-lg box-content p-3 shadow-lg m-1">
                    <p className="font-bold">{project.name}</p>
                    <p className="text-sm">{project.description}</p>
                </div>
            </Link>
        </li>
    )
}

function ProjectsList({ entities }: { entities?: DawJson.Entity<ProjectItem>[]}) {
    if (!entities) return null
    const items = entities.map(entity => {
        if (entity.class.includes("project") && entity.rel?.includes("item")) {
            return <ProjectItemComponent key={entity.properties.id} entity={entity} />
        }
    })
    return (<>
        {items.length !== 0 && items[0] ? 
            <ul className="ml-8">
                {items}
            </ul>
        : null}
    </>)
}

function ProjectsActions({ actions, setAction }: { actions?: DawJson.Action[], setAction: React.Dispatch<React.SetStateAction<boolean>> }) {
    function onClickHandler() {
        setAction(true)
    }
    const actionsElements = actions?.map((action, idx) => {
        switch (action.name) {
            case 'create-project': return (
                <div key={idx} className="mb-2 ml-8">
                    <button onClick={onClickHandler} className="rounded-lg bg-sky-400 p-2 text-white font-bold">
                        Create Project
                    </button>
                </div>
            )
        }
    })
    return actionsElements?.length === 0 ? null : <div>{actionsElements}</div>
}

function ProblemComponent({ problem }: { problem?: ProblemJson }) {
    if (!problem) return null
    return (
        <div>
            {Object.entries(problem).map((key, value) => <p>{`${key[0]}: ${key[1]}`}</p>)}
        </div>
    )
}

export default function Projects() {
    const credentials: RequestInit = {
        credentials: "include"
    }
    const init = useMemo(() => credentials ,[])

    const [currentPage, setPage] = useState(0)
    const { isFetching, isCanceled, cancel, result, error } = useFetch<Collection>(PROJECTS_URL + currentPage, init)
    
    const [isAction, setAction] = useState(false)

    if (isCanceled) return <p>Canceled</p>
    if (error !== undefined) {
        console.log(error)
        return <DisplayError message="Unexpected error"/>
    }

    if (isAction) return <CreateProject action={getAction("create-project", result?.body)!!} setAction={setAction}/>
    
    return (
        <div>
            <h1 className="m-2 p-3 text-lg font-bold">Projects Page</h1>
            {isFetching ? <p>Fetching...</p> : 
            <>
                <ProblemComponent problem={getProblemOrUndefined(result?.body)}/>
                <ProjectsActions actions={getActionsOrUndefined(result?.body)} setAction={setAction}/>
                <ProjectsList entities={getEntitiesOrUndefined(result?.body)}/>
                <CollectionPagination collection={getPropertiesOrUndefined(result?.body)} setPage={setPage} />
                <Outlet/>
            </>}
        </div>
    )
}
