import { useState } from 'react'
import { Navigate } from 'react-router-dom'
import { useLoggedInState }  from '../authentication/Session'
import { CardBody, CardDangerousButton, CardDescription, CardDiv, CardP, CardTitle } from './CardComponents'

export default function Profile() {

    const [isLogout, setIsLogout] = useState(false)
    const userSession = useLoggedInState()

    function logoutFunction() {
        userSession?.logout()
        setIsLogout(true)
    }

    if (isLogout) return <Navigate to={'/'}/>

    return (
        <div>
            <CardBody>
                <CardTitle title={userSession?.userName}/>
                <CardP text= {'Email:'}></CardP>
                <CardDescription description={userSession?.userEmail}/>
                <CardDiv>
                    <CardDangerousButton buttonName={'Logout'} onClick={logoutFunction}/>
                </CardDiv>
            </CardBody>
        </div>
    )
}