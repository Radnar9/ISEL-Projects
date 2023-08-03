import { ChangeEvent, useCallback, useEffect, useMemo, useRef, useState } from "react"
import * as DawJson from '../../models/DawJsonModel'
import { useFetch } from '../../hooks/useFetch'
import { Navigate } from "react-router-dom"
import React from "react"
import { DisplayError } from "../Error"

const BASE_URL = "http://localhost:8080/v1/projects"

function CreateProject({ action, setAction }: { action: DawJson.Action, setAction: React.Dispatch<React.SetStateAction<boolean>>}) {
    const [projectName, setProjectName] = useState('');
    const [description, setDescription] = useState('');
    const [initialState, setInitialState] = useState('');

    function projectNameHandleOnChange(event: ChangeEvent<HTMLInputElement>) {
        const inputValue = event.target.value
        setProjectName(inputValue)
    }

    function descriptionHandleOnChange(event: ChangeEvent<HTMLInputElement>) {
        const inputValue = event.target.value
        setDescription(inputValue)
    }

    function initialStateHandleOnChange(event: ChangeEvent<HTMLInputElement>) {
        const inputValue = event.target.value
        setInitialState(inputValue)
    }

    const labelRef = useRef<HTMLInputElement>(null)
    const stateRef = useRef<HTMLInputElement>(null)
    const firstStateTransitionRef = useRef<HTMLInputElement>(null)
    const secondStateTransitionRef = useRef<HTMLInputElement>(null)

    const initArrays: string[] = [];
    const [labels, setLabel] = useState(initArrays);
    const [states, setState] = useState(['closed', 'archived']);
    const [transitions, setTransition] = useState(['closed', 'archived']);

    const [fetchUrl, setFetchUrl] = useState('')
    const [init, setInit] = useState<RequestInit>({})

    const { isFetching, isCanceled, cancel, result, error } = useFetch<any>(fetchUrl, init)

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
                    if (isRequired(prop) && labels.length === 0) {
                        console.log("Labels is undefined")
                        return
                    }
                    payload['labels'] = labels 
                    break
                case 'states':
                    if (isRequired(prop) && states.length === 2) {
                        console.log("States is undefined")
                        return
                    }
                    payload['states'] = states 
                    break
                case 'initialState':
                    if (isRequired(prop) && (!initialState || !states.includes(initialState))) {
                        console.log("Initial state is undefined or it's not defined in states")
                        return
                    } 
                    payload['initialState'] = initialState 
                    break
                case 'statesTransitions':
                    if (isRequired(prop) && transitions.length === 2) {
                        console.log("Transitions is undefined")
                        return
                    } 
                    payload['statesTransitions'] = transitions 
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
        setFetchUrl(BASE_URL)
    }

    function Labels() {
        function addLabelOnClickHandler() {
            const label = labelRef.current?.value
            if (label && !labels.includes(label)) setLabel([...labels, label])
        }
    
        function removeLabelOnClickHandler(idx: number) {
            const arr = [...labels]
            arr.splice(arr.indexOf(labels[idx]), 1)
            setLabel(arr)
        }

        return (<div>
            <label className="m-1 mb-0 block">{`Labels:`}</label>
            <input ref={labelRef} className="mt-1 px-3 py-2 bg-white border border-slate-300 rounded-md text-sm shadow-sm placeholder-slate-400" type="text" name="label" placeholder="Labels"/>
            <button className="m-2 bg-blue-500 hover:bg-blue-600 active:bg-blue-700 p-1 pl-3 pr-3 rounded-full text-white font-bold" onClick={addLabelOnClickHandler}>
                Add
            </button>
            <div className="text-white font-semibold m-1">
                {labels.map((label, idx) =>
                    <span key={idx} className="m-1 rounded-full p-1 pl-2 pr-2 bg-blue-300 inline-flex">
                        <p>{label}</p>
                        <button className="font-bold pl-2" onClick={(e) => removeLabelOnClickHandler(idx)}>X</button>
                    </span>
                )}
            </div>
        </div>)
    }

    function States() {
        function isDefaultState(state: string) {
            return state === "closed" || state === "archived"
        }

        function addStateOnClickHandler() {
            const state = stateRef.current?.value
            if (state && !states.includes(state)) setState([...states, state])
        }

        function removeStateOnClickHandler(idx: number) {
            const arr = [...states]
            const removedState = arr.splice(idx, 1)[0]
            setState(arr)

            // Verify if there is transitions with the removed state
            const transArr = [...transitions]
            for (let i = 0; i < transArr.length; i++) {
                if (transArr[i] === removedState) {
                    transArr.splice(i % 2 === 0 ? i : i - 1, 2)
                    // After the removal the transArr will be smaller
                    i = (i % 2 === 0 && i > 0 ? i - 1 : i - 2)
                }
            }
            if (transArr.length !== transitions.length) setTransition(transArr)
        }

        return (<div>
            <label className="m-1 mb-0 block">{`States:`}</label>
            <input ref={stateRef} className="mt-1 px-3 py-2 bg-white border border-slate-300 rounded-md text-sm shadow-sm placeholder-slate-400" type="text" name="state" placeholder="States"/>
            <button className="m-2 bg-blue-500 hover:bg-blue-600 active:bg-blue-700 p-1 pl-3 pr-3 rounded-full text-white font-bold" onClick={addStateOnClickHandler}>
                Add
            </button>
            <div className="text-white font-semibold m-1">
                {states.map((state, idx) =>
                    <span key={idx} className="m-1 rounded-full p-1 pl-2 pr-2 bg-blue-300 inline-flex">
                        <p>{state}</p>
                        {isDefaultState(state) ? null : <button className="font-bold pl-2" onClick={() => removeStateOnClickHandler(idx)}>X</button>}
                    </span>
                )}
            </div>
        </div>)
    }

    function Transitions() {
        function addTransitionOnClickHandler() {
            const state1 = firstStateTransitionRef.current?.value
            const state2 = secondStateTransitionRef.current?.value
            if (state1 && state2 && state1 !== state2 && states.includes(state1) && states.includes(state2)) {
                const result = transitions.filter((state, idx) => (state1 === state && idx % 2 === 0 && state2 === transitions[idx + 1]))
                if (result.length !== 0) return; // Transition already exists
                setTransition([...transitions, state1, state2])
            }
        }

        function removeTransitionOnClickHandler(idx: number) {
            const arr = [...transitions]
            arr.splice(arr.indexOf(transitions[idx]), 2)
            setTransition(arr)
        }

        return (
            <div>
                <label className="m-1 mb-0 block">Transitions:</label>
                <input ref={firstStateTransitionRef} className="mt-1 px-3 py-2 bg-white border border-slate-300 rounded-md text-sm shadow-sm placeholder-slate-400" type="text" name="firstStateTransition" placeholder="First state"/>
                <input ref={secondStateTransitionRef} className="m-1 mt-1 px-3 py-2 bg-white border border-slate-300 rounded-md text-sm shadow-sm placeholder-slate-400" type="text" name="secondStateTransition" placeholder="Second state"/>
                <button className="m-2 bg-blue-500 hover:bg-blue-600 active:bg-blue-700 p-1 pl-3 pr-3 rounded-full text-white font-bold" onClick={addTransitionOnClickHandler}>
                    Add
                </button>
                <div className="text-white font-semibold m-1">
                    {transitions.map((state, idx) => <>{idx % 2 === 1 ? null : 
                        <span key={idx} className="m-1 rounded-full p-1 pl-2 pr-2 bg-blue-300 inline-flex">
                            <p>{`(${state}, ${transitions[idx + 1]}) `}</p>
                            {idx === 0 ? null : <button className="font-bold pl-2" onClick={() => removeTransitionOnClickHandler(idx)}>X</button>}
                        </span>
                    }</>)}
                </div>
            </div>
        )
    }

    if (isFetching) {
        console.log('Fetching...')
    }

    if (!isFetching && fetchUrl) {
        if (result?.body?.type === 'success') {
            return <Navigate to={`${result?.body.entity.properties.id}`}/>
        } else {
            console.log(result?.body?.problem)
            return <DisplayError message={result?.body?.problem.title}/>
        }
    } 

    return (
        <div className="m-4">
            <div className="bg-red-600 inline p-1 rounded-md text-white relative left-60"><button onClick={() => setAction(false)}>X</button></div>
            <h1 className="mb-4 font-bold">Create new project</h1>
        
            <div>
                <label className="m-1 mb-0 block">{`Name:`}</label>
                <input  className="mt-1 block px-3 py-2 bg-white border border-slate-300 rounded-md text-sm shadow-sm placeholder-slate-400" type="text" name="name" placeholder="Name" value={projectName} onChange={projectNameHandleOnChange}/>
            </div>

            <div>
                <label className="m-1 mb-0 block">{`Description:`}</label>
                <input key={2} className="mt-1 block px-3 py-2 bg-white border border-slate-300 rounded-md text-sm shadow-sm placeholder-slate-400" type="text" name="description" placeholder="Description" value={description} onChange={descriptionHandleOnChange}/>
            </div>

            <Labels/>
            <States/>

            <div>
                <label className="m-1 mb-0 block">{`Initial state:`}</label>
                <input className="mt-1 block px-3 py-2 bg-white border border-slate-300 rounded-md text-sm shadow-sm placeholder-slate-400" type="text" name="initialState" placeholder="Initial state" value={initialState} onChange={initialStateHandleOnChange}/>
            </div>
            
            <Transitions/>

            <button className="m-2 bg-blue-500 hover:bg-blue-600 active:bg-blue-700 p-1 pl-3 pr-3 rounded-full text-white font-bold" onClick={submitOnClickHandler}>
                Create project
            </button>
        </div>
    )
}

export default CreateProject;