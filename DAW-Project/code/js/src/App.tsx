import { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { SESSION_KEY, LoggedInContext, createRepository, useLoggedInState, EMAIL_KEY, NAME_KEY } from './authentication/Session'

import './App.css';
import Home from './components/Home'
import NavBar from './components/NavBar'
import Profile from './components/Profile';
import { DisplayError } from './components/Error';
import Projects from './components/project/Projects'
import ProjectDetails from './components/project/ProjectDetails';
import CreateProject from './components/project/CreateProject';
import IssueDetails from './components/issue/IssueDetails';
import Login from './authentication/LoginPage';

const userSessionRepo = createRepository()

function AppRouter() {
  
    const userSession = useLoggedInState()
    const IsAuthenticated = userSession?.isLoggedIn
    
    return (
        <Router>
            <NavBar/>
            <Routes>
                {!IsAuthenticated && <Route path="*" element={<Login />}/>}
                {IsAuthenticated && <Route path="projects/:projectIdParam/issues/:issueIdParam" element={<IssueDetails />}/>}
                {IsAuthenticated && <Route path="projects/:projectId" element={<ProjectDetails />}/>}
                {IsAuthenticated && <Route path="projects" element={<Projects />}/>}
                {IsAuthenticated && <Route path="profile" element={<Profile/>}/>}
                <Route path="login" element={<Login />}/>
                <Route path="/" element={<Home />}/>
                <Route path="*" element={<DisplayError message="This page isn't available"/>}/>
            </Routes>
        </Router>
    )
}


function App() {

  const [isLoggedIn, setLoggedIn] = useState(sessionStorage.getItem(SESSION_KEY) === "true")
  const [userName, setUserName] = useState(sessionStorage.getItem(NAME_KEY))
  const [userEmail, setUserEmail] = useState(sessionStorage.getItem(EMAIL_KEY))

  const currentSessionContext = { 
      isLoggedIn: isLoggedIn,
      userName: userName,
      userEmail: userEmail,
      login: (username: string, password: string) => {
          userSessionRepo.login(username, password).then(async response => {
              let validCredentials = (response.status === 200)
              sessionStorage.setItem(SESSION_KEY, JSON.stringify(validCredentials))
              setLoggedIn(validCredentials)
              
              if (validCredentials) {
                  let userInfo = await response.json()
                  sessionStorage.setItem(NAME_KEY, userInfo.name)
                  sessionStorage.setItem(EMAIL_KEY, userInfo.email)
                  setUserName(userInfo.name)
                  setUserEmail(userInfo.email)
              }
          })
      },
      logout: () => {
          userSessionRepo.logout().then(status => {
              if(status === 200) {
                  sessionStorage.removeItem(SESSION_KEY)
                  setLoggedIn(false)
              } else {
                  sessionStorage.setItem(SESSION_KEY, JSON.stringify(true))
              }
          })
      }
    }

  return (
      <div>
          <LoggedInContext.Provider value={currentSessionContext}>
              <AppRouter/>
          </LoggedInContext.Provider>  
      </div>
  )
}

export default App;
