import { ChangeEvent, useCallback, useEffect, useMemo, useRef, useState } from "react"
import * as DawJson from '../../models/DawJsonModel'
import { useFetch } from '../../hooks/useFetch'
import { Navigate } from "react-router-dom"
import React from "react"
import { DisplayError } from "../Error"
import ProjectDetails from "./ProjectDetails"

const BASE_URL = "http://localhost:8080"

function UpdateProject({ action, setAction }: { action: DawJson.Action, setAction: React.Dispatch<React.SetStateAction<string>>}) {
    const [projectName, setProjectName] = useState('');
    const [description, setDescription] = useState('');

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
        console.log(action)
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
        return <p>Fetching...</p>
    }

    if (!isFetching && fetchUrl) {
        if (result?.body?.type === 'success') {
            return <ProjectDetails/>
        } else {
            console.log(result?.body?.problem)
            return <DisplayError message={result?.body?.problem.title}/>
        }
    } 
    
    return (
        <div className="m-4">
            <div className="bg-red-600 inline p-1 rounded-md text-white relative left-60"><button onClick={() => setAction('')}>X</button></div>
            <h1 className="mb-4 font-bold">Update project</h1>
            <div>
                <label className="m-1 mb-0 block">{`Name:`}</label>
                <input  className="mt-1 block px-3 py-2 bg-white border border-slate-300 rounded-md text-sm shadow-sm placeholder-slate-400" type="text" name="name" placeholder="Name" value={projectName} onChange={projectNameHandleOnChange}/>
            </div>

            <div>
                <label className="m-1 mb-0 block">{`Description:`}</label>
                <input key={2} className="mt-1 block px-3 py-2 bg-white border border-slate-300 rounded-md text-sm shadow-sm placeholder-slate-400" type="text" name="description" placeholder="Description" value={description} onChange={descriptionHandleOnChange}/>
            </div>

            <button className="m-2 bg-blue-500 hover:bg-blue-600 active:bg-blue-700 p-1 pl-3 pr-3 rounded-full text-white font-bold" onClick={submitOnClickHandler}>
                Update project
            </button>
        </div>
    )
}

export default UpdateProject;