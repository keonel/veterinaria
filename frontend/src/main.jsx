import React from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext.jsx';
import { TiendaProvider } from './context/TiendaContext.jsx';
import App from './App.jsx';
import 'bootstrap/dist/css/bootstrap.min.css';


createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <TiendaProvider>
          <App />
        </TiendaProvider>
      </AuthProvider>
    </BrowserRouter>
  </React.StrictMode>
);