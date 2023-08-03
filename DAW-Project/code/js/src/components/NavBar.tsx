import { NavLink } from "react-router-dom";
import { useLoggedInState } from "../authentication/Session";

export default function NavBar() {
    
    const userSession = useLoggedInState()
    const IsAuthenticated = userSession?.isLoggedIn

    return (
        <div className="p-3 bg-blue-500"> 
            <nav>
                <ul className="relative flex space-x-5 font-bold text-white">
                    <li><NavLink to="/">Home</NavLink></li>
                    {IsAuthenticated && <li><NavLink to="projects">Projects</NavLink></li>}
                    {!IsAuthenticated && <li className="absolute right-3"><NavLink to="login">Login</NavLink></li>}
                    {IsAuthenticated && <p className="absolute right-20 text-sky-200">{`Hello, ${userSession.userName?.split(' ')[0]}`}</p>}
                    {IsAuthenticated && <li className="absolute right-3"><NavLink to="profile">Profile</NavLink></li>}
                </ul>
            </nav>
        </div>
    );
}

// https://flowbite.com/docs/components/navbar/#