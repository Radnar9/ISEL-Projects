import { CSSProperties, FormEventHandler } from "react"
import { UseFormRegisterReturn } from "react-hook-form"
import { InputError } from "../components/Error"

export type InputType = {
    inputLabelName: string,
    register: UseFormRegisterReturn,
    style?: CSSProperties,
    className: string,
    name: string,
    type: string,
    errorMessage?: string | undefined
}

type HeaderProps = {
    heading: string,
    paragraph: string,
    children?: React.ReactNode
  }

export function Header({heading, paragraph, children}: HeaderProps){
    return(
        <div className="mb-10">
            <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
                {heading}
            </h2>
            <div className="mt-6 flex justify-center">
                {paragraph}
            </div>
            <div className="mt-6">
                {children}
            </div>
        </div>
    )
}

export function SubmitButton({text}: {text:string}) {
    return (
        <button className="w-full py-2 px-4 bg-green-600 hover:bg-green-700 rounded-md text-white text-sm">
            {text}
        </button>
    )
}

export function InputLabel ({text}: {text:string}) {
    return (
        <label className="text-sm font-bold text-gray-600 block">
            {text}
        </label>
    )
}

export function Inputs({inputs} :{inputs: InputType[]}) {
    return (
        <>
            {inputs.map((value, index) => <div key={index}><Input value={value}/></div>)}
        </>
    )
}

export function Input({value}: {value: InputType}) {
    return (
        <div key= {value.name}>
            <InputLabel text= {value.inputLabelName}/>
            <input 
                {...value.register}
                className= {value.className}
                style= {value.style}
                name= {value.name}
                type={value.type}/>
            <InputError error= {value.errorMessage}/>
        </div>
    )
}

/**
 * Type that specifies the props object for the Form component.
 */
type FormProps = {
    onSubmitHandler: FormEventHandler<HTMLFormElement>,
    children?: React.ReactNode
}

export function Form ({onSubmitHandler, children} : FormProps) {
    return (
        <div className="max-w-md w-full shadow-md mx-auto mt-20 bg-white p-8 border border-gray-300 rounded">
            <form className="space-y-6" onSubmit= { onSubmitHandler }>
                {children}
            </form>
        </div>
    )
}