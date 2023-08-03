import { useForm } from 'react-hook-form'
import { Form, Header, Inputs, InputType, SubmitButton } from './FormComponents'
import { SESSION_KEY, useLoggedInState }  from './Session'
import { Navigate } from 'react-router-dom'

function InvalidUser() {
    return (sessionStorage.getItem(SESSION_KEY) === "false") ? (
        <div className="bg-red-100 border border-red-400 text-red-700 py-2 rounded text-center" role="alert">
            <strong className="font-bold">Invalid credentials inserted!</strong>
        </div>
    ) : null
}

export default function Login() {

    type FormData = { email: string, password: string }

    const { register, handleSubmit, formState: { errors }} = useForm<FormData>();
    const userSession = useLoggedInState()

    const onSubmitHandler = handleSubmit(async ({ email, password }) => {
        console.log(`${email} + ${password}`)
        if (email && password && userSession) {
            userSession.login(email, password)
        }
    })

    const inputs : InputType[] = [
        {
            inputLabelName: 'Email',
            register: register("email", {required: 'Is required', pattern: /.+@.+/, minLength:10, maxLength:200}),
            style: {borderColor: errors.email ? 'red': 'black'},
            className: "w-full p-2 border border-grey-300 rounded mt-1",
            name: "email",
            type: "text",
            errorMessage: errors.email && 'Invalid email'
        },
        {
            inputLabelName: 'Password',
            register: register("password", {required: 'Is required', minLength:0, maxLength:50}),
            style: {borderColor: errors.password ? 'red': 'black'},
            className: "w-full p-2 border border-grey-300 rounded mt-1",
            name: "password",
            type: "password",
            errorMessage: errors.password && 'Invalid password'
        }
    ]
    
    return useLoggedInState()?.isLoggedIn ? <Navigate to={'/'} /> : (
        <div>
            <Form onSubmitHandler = { onSubmitHandler }>
                <Header
                    heading='Login page'
                    paragraph='Must have an account'
                    children = {<InvalidUser/>}
                />
                <Inputs inputs={inputs}/>
                <SubmitButton text={'Sign in'}/>
            </Form>
        </div>
    )    
}