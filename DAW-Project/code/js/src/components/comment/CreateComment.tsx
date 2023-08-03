import { ChangeEvent, useCallback, useEffect, useMemo, useRef, useState } from "react"
import * as DawJson from '../../models/DawJsonModel'
import { useFetch } from '../../hooks/useFetch'
import { Navigate } from "react-router-dom"
import React from "react"
import { Issue } from "../issue/Common"
import { DisplayError } from "../Error"
import IssueDetails from "../issue/IssueDetails"

const BASE_URL = "http://localhost:8080"

function CreateComment({ action, setAction }: { action: DawJson.Action, setAction: React.Dispatch<React.SetStateAction<string>>}) {
    const [comment, setComment] = useState('');

    const [fetchUrl, setFetchUrl] = useState('')
    const [init, setInit] = useState<RequestInit>({})

    const { isFetching, isCanceled, cancel, result, error } = useFetch<any>(fetchUrl, init)

    function commentHandleOnChange(event: ChangeEvent<HTMLInputElement>) {
        const inputValue = event.target.value
        setComment(inputValue)
    }

    function submitOnClickHandler() {
        function isRequired(prop: DawJson.Property) {
            return !prop.required
        }
        const payload: any = {}
        action.properties.forEach(prop => {
            switch (prop.name) {
                case 'comment':
                    if (isRequired(prop) && !comment) {
                        console.log("Comment is undefined")
                        return
                    }
                    payload['comment'] = comment 
            }
        })
        setInit({
            method: action.method,
            headers: {
                'Content-Type': action.type
            },
            credentials: "include",
            body: JSON.stringify(payload),
        })
        setFetchUrl(BASE_URL + action.href)
    }

    if (isFetching) {
        console.log('Fetching...')
    }

    if (!isFetching && fetchUrl) {
        if (result?.body?.type === 'success') {
            return <IssueDetails/>
        } else {
            console.log(result?.body?.problem)
            return <DisplayError message={result?.body?.problem.title}/>
        }
    } 
    
    return (
        <div className="m-4">
            <div className="bg-red-600 inline p-1 rounded-md text-white relative left-60"><button onClick={() => setAction('')}>X</button></div>
            <h1 className="mb-4 font-bold">Create comment</h1>
            <div>
                <label className="m-1 mb-0 block">{`Comment:`}</label>
                <input  className="mt-1 block px-3 py-2 bg-white border border-slate-300 rounded-md text-sm shadow-sm placeholder-slate-400" type="text" name="comment" placeholder="Comment" value={comment} onChange={commentHandleOnChange}/>
            </div>
            <button className="m-2 bg-blue-500 hover:bg-blue-600 active:bg-blue-700 p-1 pl-3 pr-3 rounded-full text-white font-bold" onClick={submitOnClickHandler}>
                Create comment
            </button>
        </div>
    )
}

export default CreateComment;