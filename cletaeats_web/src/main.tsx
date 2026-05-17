import React from 'react'
import ReactDOM from 'react-dom/client'

import { BrowserRouter } from 'react-router-dom'

import App from './App'

import AppProviders from './providers/AppProviders'

import './styles/globals.css'

import {
    Toaster,
} from 'react-hot-toast'

ReactDOM.createRoot(
    document.getElementById('root')!,
).render(
    <React.StrictMode>
        <BrowserRouter>
            <AppProviders>
                <Toaster
                    position="top-right"
                />
                <App />
            </AppProviders>
        </BrowserRouter>
    </React.StrictMode>,
)