
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import MovieApplicationManager from "./components/MovieApplicationManager"

import MovieManager from "./components/MovieManager"

import PaymentManager from "./components/PaymentManager"


import MyPage from "./components/MyPage"
export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/movieApplications',
                name: 'MovieApplicationManager',
                component: MovieApplicationManager
            },

            {
                path: '/movies',
                name: 'MovieManager',
                component: MovieManager
            },

            {
                path: '/payments',
                name: 'PaymentManager',
                component: PaymentManager
            },


            {
                path: '/myPages',
                name: 'MyPage',
                component: MyPage
            },


    ]
})
