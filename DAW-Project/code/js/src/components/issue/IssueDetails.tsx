import { ReactElement, useCallback, useMemo, useState } from "react"
import { Link, Outlet, useParams } from "react-router-dom"
import { Issue, IssueCard, IssueItemComponent } from "./Common"
import { useFetch, FetchResult } from '../../hooks/useFetch'
import { CommentItem, CommentItemComponent } from "../comment/Common"
import { Collection, CollectionPagination } from "../pagination/CollectionPagination"
import { getAction, getActionsOrUndefined, getEntitiesOrUndefined, getEntityOrUndefined, getPropertiesOrUndefined } from "../../models/ModelUtils"
import * as DawJson from '../../models/DawJsonModel'
import UpdateIssue from "./UpdateIssue"
import { DeleteComponent } from "../DeleteComponent"
import CreateComment from "../comment/CreateComment"
import React from "react"
import { DeleteComment } from "../comment/DeleteComment"
import { DisplayError } from "../Error"

const ISSUE_URL = (projectId: number | undefined, issueId: number | undefined): string => {
    return `http://localhost:8080/v1/projects/${projectId}/issues/${issueId}`
}

const COMMENTS_URL = (projectId: number | undefined, issueId: number | undefined, page: number): string => {
    return page < 0 ? '' : `http://localhost:8080/v1/projects/${projectId}/issues/${issueId}/comments?page=${page}`
}

export default function IssueDetails({projectIdArg, issueIdArg}: {projectIdArg?: number, issueIdArg?: number}) {
    const { projectIdParam, issueIdParam } = useParams();
    let projectId = Number(projectIdParam)
    let issueId = Number(issueIdParam)
    if (!projectIdParam && !issueIdParam) {
        projectId = projectIdArg!!
        projectId = issueIdArg!!
    }

    const credentials: RequestInit = {
        credentials: "include"
    }
    const init = useMemo(() => credentials ,[])

    const [currentPage, setPage] = useState(-1)
    const [action, setAction] = useState('')

    const [deleteCommentAction, setDeleteCommentAction] = useState<DawJson.Action>()

    const { isFetching, isCanceled, cancel, result, error } = useFetch<Issue>(ISSUE_URL(projectId, issueId), init);
    const { 
        isFetching: areCommentsFetching, 
        isCanceled: areCommentsCanceled, 
        cancel: cancelComments, 
        result: commentsResult, 
        error: commentsError 
    } = useFetch<Collection>(COMMENTS_URL(projectId, issueId, currentPage), init);
    
    // if (isFetching) return <p>Fetching...</p>
    if (isCanceled) return <p>Canceled</p>
    if (error !== undefined) {
        console.log(error)
        return <DisplayError error={error}/>
    }

    switch (action) {
        case 'update-issue': return <UpdateIssue props={getPropertiesOrUndefined(result?.body)!!} projectId={Number(projectId)!!} setAction={setAction} action={getAction('update-issue', result?.body)!!}/>
        case 'delete-issue': return <DeleteComponent setAction={setAction} urlToDelete={ISSUE_URL(projectId, issueId)} redirectUrl={`/projects/${projectId}`}/>
        case 'create-comment': {
            const commentListEntity = getEntitiesOrUndefined(result?.body)!!.find(entity => entity.class.includes("comment") && entity.class.includes("collection") && entity.rel?.includes("issue-comments"))
            const createCommentAction = commentListEntity?.actions?.find(action => action.name === 'create-comment')
            return <CreateComment action={createCommentAction!!} setAction={setAction}/>
        }
        case 'delete-comment': {
            return <DeleteComment action={deleteCommentAction!!} issueId={issueId} projectId={projectId}/>
        }
    }
    
    function CommentList({ entities }: { entities?: DawJson.Entity<CommentItem>[]}) {
        if (!entities) return null
        let entityPage
        if (commentsResult) entityPage = getEntityOrUndefined(commentsResult.body)!!
        else entityPage = entities.find(entity => entity.class.includes("comment") && entity.class.includes("collection") && entity.rel?.includes("issue-comments"))
        const collection = entityPage?.properties
        const items = entityPage?.entities?.map((entity, idx) => {
            if (entity.class.includes("comment") && entity.rel?.includes("item")) {
                return <CommentItemComponent 
                    key={entity.properties.id} 
                    entity={entity}
                    author={entity.entities?.find(author => author.class.includes("user") && author.rel?.includes("author"))}>
                    <CommentActions actions={entity.actions} setAction={setAction}></CommentActions>
                </CommentItemComponent>
            }
        })

        return (<>
            <div className="flex">
                <h1 className="m-5 pl-2 text-lg font-bold">Comments</h1>
                <CommentsActions actions={entityPage?.actions} setAction={setAction}/>
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

    function IssueActions({ actions, setAction }: { actions?: DawJson.Action[], setAction: React.Dispatch<React.SetStateAction<string>> }) {
        const actionsElements = actions?.map((action, idx) => {
            switch (action.name) {
                case 'update-issue': return (
                    <div key={idx} className="mb-2 ml-8">
                        <button onClick={() => setAction('update-issue')} className="rounded-lg bg-green-600 p-2 text-white font-bold">
                            Update issue
                        </button>
                    </div>
                )
                case 'delete-issue': return (
                    <div key={idx} className="mb-2 ml-8">
                        <button onClick={() => setAction('delete-issue')} className="rounded-lg bg-red-800 p-2 text-white font-bold">
                            Delete issue
                        </button>
                    </div>
                )
            }
        })
        return actionsElements?.length === 0 ? null : <div className="mt-4 inline-flex">{actionsElements}</div>
    }

    function CommentsActions({ actions, setAction }: { actions?: DawJson.Action[], setAction: React.Dispatch<React.SetStateAction<string>> }) {
        const actionsElements = actions?.map((action, idx) => {
            switch (action.name) {
                case 'create-comment': return (
                    <div key={idx} className="mb-2 ml-8">
                        <button onClick={() => setAction('create-comment')} className="rounded-lg bg-sky-400 p-2 text-white font-bold">
                            Create comment
                        </button>
                    </div>
                )
            }
        })
        return actionsElements?.length === 0 ? null : <div className="mt-4 inline-flex">{actionsElements}</div>
    }

    function CommentActions({ actions, setAction }: { actions?: DawJson.Action[], setAction: React.Dispatch<React.SetStateAction<string>> }) {
        function deleteOnClickHandler(action: DawJson.Action) {
            setDeleteCommentAction(action)
            setAction('delete-comment')
        }

        const actionsElements = actions?.map((action, idx) => {
            switch (action.name) {
                case 'delete-comment': return (
                   <div key={idx} className="absolute top-1 right-5">
                        <button onClick={() => deleteOnClickHandler(action)} className="text-red-800 text-2xl font-bold">
                            x
                        </button>
                    </div>
                )
            }
        })
        return actionsElements?.length === 0 ? null : <div className="mt-4 inline-flex">{actionsElements}</div>
    }
    
    return (<>
        { !isFetching ? <>
            {result?.body?.type === 'problem' ? <DisplayError message={result.body.problem.title}/> :
        <div>
            <IssueCard entity={getEntityOrUndefined(result?.body)}>
                <IssueActions actions={getActionsOrUndefined(result?.body)} setAction={setAction}/>
            </IssueCard>
            {areCommentsFetching ? null : <CommentList entities={getEntitiesOrUndefined(result?.body)}/>}
        </div>}</> : null} </>)
}