import { MouseEventHandler } from "react"

export function CardBody({children} : {children?: React.ReactNode}) {
    return (
        <div className="rounded p-3 shadow-lg"> 
            <div className="px-6 py-4">
                {children}
            </div>
        </div>
    )
}

export function CardTitle({title} : {title: string | undefined | null}) {
    return title === undefined ? <></> :
        <div className="bg-clip-text text-transparent bg-gradient-to-r from-pink-500 to-blue-900 font-bold text-xl mb-3">{title}</div>
}

export function CardDescription({description} : {description: string | null | undefined}) {
    return (
        <p className="text-gray-700 text-base italic"> {description}</p>
    )
}

export function CardDiv({children} : {children?: React.ReactNode}) {
    return (
        <div className="pt-3">
            {children}
        </div>
    )
}

export function CardP({text} : {text: string}) {
    return <p>{text}</p>
}

export function CardValue({value}: {value:string | undefined}) {
    return value === undefined ? <></> :
    <span className="inline-block bg-gray-200 rounded-full px-3 py-1 text-sm font-semibold text-gray-700 mr-2 mb-2">{value}</span>
}

export function CardDangerousButton({buttonName, onClick} : {buttonName: string, onClick?: MouseEventHandler}) {
    return <button onClick={onClick} className="focus:outline-none text-white bg-red-500 hover:bg-red-700 px-3 py-1 font-medium rounded-lg">{buttonName}</button>
}
