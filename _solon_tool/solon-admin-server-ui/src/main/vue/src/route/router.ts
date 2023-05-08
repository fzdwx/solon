import {createRouter, createWebHashHistory} from 'vue-router'
import {routes} from "./route.ts";

export const router = createRouter({
    history: createWebHashHistory(),
    routes
})