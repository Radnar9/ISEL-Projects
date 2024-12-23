import { createContext, useContext } from 'react'

export const SESSION_KEY = 'IsLogin'
export const NAME_KEY = 'UserName'
export const EMAIL_KEY = 'UserEmail'

export async function loginUser(email: string, password: string): Promise<Response> {
  console.log('Login User function called')
  console.log(`${email} + ${password}`)

  const response = await fetch("http://localhost:8080/v1/login",
    {
      method: "POST",
      body: JSON.stringify({ email: email, password: password }),
      credentials: "include"
    })
  console.log(response.status)
  return response
}

export async function logoutUser(): Promise<number> {
  const response = await fetch("http://localhost:8080/v1/logout",  
  { 
     method: "POST",
     credentials: "include"
  })
  console.log(response.status)
  return response.status
}

export function createRepository() {
  return {
    login: loginUser,
    logout: logoutUser
  }
}

export type ContextType = {
  isLoggedIn: boolean,
  userName: string | null,
  userEmail: string | null,
  login: (username: string, password: string) => void,
  logout: () => void
}

export const LoggedInContext = createContext<ContextType | undefined>(undefined)

export function useLoggedInState() {
  return useContext(LoggedInContext)
}