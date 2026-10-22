import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App'
import './index.css'

// import matlab ye sari files leke aoo;


ReactDOM.createRoot(document.getElementById('root')!).render(
  // root create roort root means file jo react ko browser je attach krte hn 
  // ! means react agar bole ke root to hn he nahi to usse asuure krne ke liye ke hmne add kiys hn root
  // html mein 
  //React ko browser ke HTML page ke andar application attach karni hoti hai
  <React.StrictMode>
    {/* strict mode mtalab ye problem code mein identify krta hn */}
    <App />
  </React.StrictMode>
)
