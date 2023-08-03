import { CardBody, CardDescription, CardDiv, CardValue, CardP, CardTitle } from "../CardComponents"
import { Link } from "react-router-dom"
import { getDate } from "../Common"
import * as DawJson from '../../models/DawJsonModel'

export type StateItem = {
    id: number,
    name: string
}

export type Label = { 
    id: number,
    name: string
}

export type IssueItem = {
    id: number,
    name: string,
    labels?: Label[],
    state: StateItem
}

export type Issue = {
    id: number,
    name: string,
    description: string,
    creationTimestamp: string,
    closeTimestamp: string,
    labels: Array<Label>,
    state: StateItem,
    possibleTransitions: Array<StateItem>
}

function ListLabels({labels}: {labels?: Label[]}) {
    if (!labels) return null
    return (
        <div>
            <p className="text-sm">Labels:</p>
            <ul className="list-none">
                { Object.entries(labels).map(label => { return <li key={label[1].id} className="text-sm"> {label[1].name}</li> }) }
            </ul>
        </div>
    )
}

type IssueItemComponentProps = {
    projectId: string | undefined,
    entity: DawJson.Entity<IssueItem>
}

export function IssueItemComponent( { projectId, entity }: IssueItemComponentProps ) {
    const issue = entity.properties  
    const id = issue.id
    return (
        <li className="px-3 pt-3">
            <Link to={`/projects/${projectId}/issues/${id}`}>
                <div className="bg-gray-200 inline-block rounded-lg box-content p-3 shadow-lg">
                    <p className="font-bold">{issue.name}</p>
                    <p className="text-sm">Current state: {issue.state.name}</p>
                    <ListLabels labels={issue.labels}/>
                </div>
            </Link>
        </li>
    )
}

export function IssueCard({ entity, children }: { entity?: DawJson.Entity<Issue>, children?: React.ReactNode}) {
    if (!entity) return null
    const issue = entity.properties
    const hasLabels = issue.labels !== undefined
    const hasTransitions = issue.possibleTransitions !== undefined
    return(
        <CardBody>
            <CardTitle title={issue.name}/>
            <CardDescription description={issue.description}/>
            <CardDiv>
                <span>Current state: </span>
                <span className="pt-4 font-semibold">{issue.state.name}</span>
            </CardDiv>
            <CardDiv>
                <span className="inline-block bg-sky-100 rounded-full px-3 py-2 text-sm font-semibold text-gray-700 mr-2 mb-2">
                    {`Creation time: ${getDate(issue.creationTimestamp)}`}
                </span>
                <span className="inline-block bg-purple-100 rounded-full px-3 py-2 text-sm font-semibold text-gray-700 mr-2 mb-2">
                    {`Close time: ${getDate(issue.closeTimestamp)}`}  
                </span>
            </CardDiv>
            {hasLabels && <>
                <CardP text = {'Labels:'}/>
                <CardDiv>
                    {Object.entries(issue.labels).map(value => {
                        return <CardValue key={value[1].id} value={value[1].name}/>
                    })}
                </CardDiv>
            </>}
            {hasTransitions && <>
                <CardP text = {'Possible transitions:'}/>
                <CardDiv>
                    {Object.entries(issue.possibleTransitions).map(value => {
                        return <span key={value[1].id} className="inline-block bg-gray-200 rounded-full px-3 py-1 text-sm font-semibold text-gray-700 mr-2 mb-2">{value[1].name}</span>
                    })}
                </CardDiv></>}
            {children}
        </CardBody>
    )
}