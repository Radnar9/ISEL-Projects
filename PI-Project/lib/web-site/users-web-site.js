const router = require('express').Router()
const passport = require('passport')
const expressSession = require("express-session");
const httpError = require("../errors/http-errors");

module.exports = function(app, services) {
    if (!services)
        throw "Invalid argument for services"

    // Passport initialization
    app.use(expressSession({ secret: "Doninha fedorenta" }))
    app.use(passport.initialize(undefined))
    app.use(passport.session(undefined))
    passport.serializeUser((user, done) => done(null, user))
    passport.deserializeUser((user, done) => done(null, user))

    router.get('/signup', createUserForm)
    router.post('/signup', createUser)
    router.get('/login', loginForm)
    router.post('/login', login)
    router.post('/logout', logout)

    return router

    function createUserForm(req, res) {
        res.render('signup', { hasError: false })
    }

    function createUser(req, res) {
        services.createUser(req.body)
            .then(user => req.login({ token: user.token, username: user.username }, () => res.redirect('/')))
            .catch(e => {
                processError(e, res)
                res.render('signup', { hasError: true, message: e.message })
            })
    }

    function loginForm(req, res) {
        res.render('login')
    }

    function login(req, res) {
        const username = req.body.username
        const password = req.body.password

        services.validateCredentials(username, password)
            .then(token => req.login({ token: token, username: username }, () => res.redirect('/')))
            .catch(e => {
                processError(e, res)
                res.render('login', { username: username, hasError: true, message: e.message })
            })
    }

    function logout(req, res) {
        req.logout()
        res.redirect('/')
    }

    function processError(appError, res) {
        const error = httpError.processApplicationError(appError)
        res.status(error.status)
    }
}