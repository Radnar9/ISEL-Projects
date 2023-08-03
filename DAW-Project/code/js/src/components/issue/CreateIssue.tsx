import { ChangeEvent, useCallback, useEffect, useMemo, useRef, useState } from "react"
import * as DawJson from '../../models/DawJsonModel'
import { useFetch } from '../../hooks/useFetch'
import { Navigate } from "react-router-dom"
import React from "react"
import { Issue, Label } from "./Common"
import { DisplayError } from "../Error"
import { Project } from "../project/ProjectDetails"

const BASE_URL = "http://localhost:8080"

function CreateIssue({ props, action, setAction }: { props: Project, action: DawJson.Action, setAction: React.Dispatch<React.SetStateAction<string>>}) {
    const [projectName, setProjectName] = useState('');
    const [description, setDescription] = useState('');
    const [addedLabels, setAddedLabels] = useState<Label[]>([]);
    const [remainingLabels, setRemainingLabels] = useState(props.labels);

    const [fetchUrl, setFetchUrl] = useState('')
    const [init, setInit] = useState<RequestInit>({})

    const { isFetching, isCanceled, cancel, result, error } = useFetch<any>(fetchUrl, init)

    function projectNameHandleOnChange(event: ChangeEvent<HTMLInputElement>) {
        const inputValue = event.target.value
        setProjectName(inputValue)
    }

    function descriptionHandleOnChange(event: ChangeEvent<HTMLInputElement>) {
        const inputValue = event.target.value
        setDescription(inputValue)
    }

    function submitOnClickHandler() {
        function isRequired(prop: DawJson.Property) {
            return !prop.required
        }
        const payload: any = {}
        action.properties.forEach(prop => {
            switch (prop.name) {
                case 'name':
                    if (isRequired(prop) && !projectName) {
                        console.log("Name is undefined")
                        return
                    }
                    payload['name'] = projectName 
                    break
                case 'description':
                    if (isRequired(prop) && !description) {
                        console.log("Description is undefined")
                        return
                    }
                    payload['description'] = description 
                    break
                case 'labels':
                    if (isRequired(prop) && addedLabels.length === 0) {
                        console.log("State is undefined")
                        return
                    } else {
                        payload['labels'] = addedLabels.map(label => label.id) 
                    }
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

    function addLabelOnClickHandler(label: Label, idx: number) {
        setAddedLabels([...addedLabels, label])
        const arr = [...remainingLabels]
        const removed = arr.splice(idx, 1)[0]
        setRemainingLabels([...arr])
    }

    function removeLabelOnClickHandler(label:Label, idx: number) {
        const arr = [...addedLabels]
        const removed = arr.splice(idx, 1)[0]
        setAddedLabels([...arr])
        setRemainingLabels([...remainingLabels, label])
    }

    if (isFetching) {
        console.log('Fetching...')
    }

    if (!isFetching && fetchUrl) {
        if (result?.body?.type === 'success') {
            return <Navigate to={`issues/${result?.body?.entity.properties.id}`}/>
        } else {
            <DisplayError message={result?.body?.problem.title}/>
            console.log(result?.body?.problem)
        }
    } 
    
    return (
        <div className="m-4">
            <div className="bg-red-600 inline p-1 rounded-md text-white relative left-60"><button onClick={() => setAction('')}>X</button></div>
            <h1 className="mb-4 font-bold">Create issue</h1>
            <div>
                <label className="m-1 mb-0 block">{`Name:`}</label>
                <input  className="mt-1 block px-3 py-2 bg-white border border-slate-300 rounded-md text-sm shadow-sm placeholder-slate-400" type="text" name="name" placeholder="Name" value={projectName} onChange={projectNameHandleOnChange}/>
            </div>

            <div>
                <label className="m-1 mb-0 block">{`Description:`}</label>
                <input key={2} className="mt-1 block px-3 py-2 bg-white border border-slate-300 rounded-md text-sm shadow-sm placeholder-slate-400" type="text" name="description" placeholder="Description" value={description} onChange={descriptionHandleOnChange}/>
            </div>

            <div>
                <label className="m-1 mb-0 block">{`Click in a label to add it to the issue:`}</label>
                <div className="text-white font-semibold m-1">
                    {remainingLabels.map((label, idx) =>
                        <span key={idx} className="m-1 rounded-full p-1 pl-2 pr-2 bg-blue-300 inline-flex">
                            <button className="font-bold" onClick={() => addLabelOnClickHandler(label, idx)}>{label.name}</button>
                        </span>
                    )}
                </div>
                {addedLabels.length !== 0 ? <label className="m-1 mb-0 block">{`Added labels:`}</label> : null}
                <div className="text-white font-semibold m-1">
                    {addedLabels.map((label, idx) =>
                        <span key={idx} className="m-1 rounded-full p-1 pl-2 pr-2 bg-blue-300 inline-flex">
                            <p>{label.name}</p>
                            <button className="font-bold pl-2 text-red-800" onClick={() => removeLabelOnClickHandler(label, idx)}>X</button>
                        </span>
                    )}
                </div>
            </div>
            <button className="m-2 bg-blue-500 hover:bg-blue-600 active:bg-blue-700 p-1 pl-3 pr-3 rounded-full text-white font-bold" onClick={submitOnClickHandler}>
                Update issue
            </button>
        </div>
    )
}

export default CreateIssue;