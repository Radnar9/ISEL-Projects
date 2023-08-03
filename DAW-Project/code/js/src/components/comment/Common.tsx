import { Link, Outlet, useParams } from "react-router-dom"
import * as DawJson from '../../models/DawJsonModel'
import { getDate } from '../Common'
import { CardBody, CardDescription, CardDiv, CardTitle, CardValue } from "../CardComponents"
import { UserItem } from "../user/UserUtils"

export type CommentItem = {
    id: string,
    comment: string,
    timestamp: string
}

export type CommentItemComponentProps = {
    entity: DawJson.Entity<CommentItem>
    author: DawJson.Entity<UserItem> | undefined
    children: React.ReactNode
}

export function CommentItemComponent( { entity, author, children }: CommentItemComponentProps ) {
    const comment = entity.properties  
    return (
        <li className="px-3 pt-3">
            <div className="relative bg-gray-200 inline-block rounded-lg box-content p-3 shadow-lg">
                <p className="font-bold inline">{author?.properties.name}</p>
                {children}
                <p className="text-sm">{getDate(comment.timestamp)}</p>
                <p className="text-sm">{comment.comment}</p>
            </div>
        </li>
    )
}

export type CommentCardProps = {
    entity: DawJson.Entity<CommentItem>
    author: DawJson.Entity<UserItem> | undefined
}

export function CommentCard({ entity, author }: CommentCardProps) {
    const comment = entity.properties
    const authorInfo = author?.properties
    return(
        <div className="max-w-md grid-rows-2">
            <CardBody>
                <CardTitle title={authorInfo?.name}/>
                <CardDescription description={comment.comment}/> 
                <CardDiv>
                    <span className="inline-block bg-sky-100 rounded-full px-3 py-2 text-sm font-semibold text-gray-700 mr-2 mb-2">
                        {`Creation time: ${getDate(comment.timestamp)}`}
                    </span>
                </CardDiv>
                <CardValue value={authorInfo?.email}/>
            </CardBody>
        </div>
    )
}