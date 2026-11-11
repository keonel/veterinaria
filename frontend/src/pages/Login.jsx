import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function Login() {
  const [correo, setCorreo] = useState('');
  const [contrasena, setContrasena] = useState('');
  const [recordarme, setRecordarme] = useState(false);
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState('');
  const { iniciarSesion } = useAuth();
  const navegar = useNavigate();

  async function manejarEnvio(evento) {
    evento.preventDefault();
    const correoNormalizado = correo.trim().toLowerCase();

    if (correoNormalizado === '' || contrasena === '') {
      setError('Debes completar el correo y la contraseña.');
      return;
    }

    setEnviando(true);
    setError('');
    try {
      // El backend compara la contraseña con el hash BCrypt y responde con el JWT.
      // Ya no hay cuentas ni contraseñas escritas en el código del frontend.
      const usuario = await iniciarSesion(correoNormalizado, contrasena);
      // Cada rol entra a su zona: administrador y vendedor al panel, cliente a la tienda
      navegar(usuario.rol === 'CLIENTE' ? '/' : '/admin');
    } catch (e) {
      setError(e.message);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <section className="login-section">
      <div className="login-container">
        <div className="login-encabezado">
          <span className="login-huellas" aria-hidden="true">🐾</span>
          <h1>Iniciar sesión</h1>
          <p>Ingresa a tu cuenta para continuar</p>
        </div>

        <form id="formLogin" onSubmit={manejarEnvio}>
          <div className="form-group">
            <label htmlFor="correo">Correo electrónico</label>
            <input
              id="correo"
              type="email"
              placeholder="correo@ejemplo.com"
              autoComplete="email"
              required
              value={correo}
              onChange={(e) => setCorreo(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label htmlFor="contrasena">Contraseña</label>
            <input
              id="contrasena"
              type="password"
              placeholder="Ingresa tu contraseña"
              autoComplete="current-password"
              required
              value={contrasena}
              onChange={(e) => setContrasena(e.target.value)}
            />
          </div>

          <div className="login-opciones">
            <label className="login-recordarme">
              <input
                type="checkbox"
                checked={recordarme}
                onChange={(e) => setRecordarme(e.target.checked)}
              />
              Recordarme
            </label>

            <span className="login-recuperar">
              ¿Olvidaste tu contraseña?
            </span>
          </div>

          {error && (
            <p className="mensaje-error" role="alert" style={{ marginBottom: '12px' }}>
              {error}
            </p>
          )}

          <button type="submit" className="login-boton" disabled={enviando}>
            {enviando ? 'Ingresando…' : 'Iniciar sesión'}
          </button>
        </form>

        <p className="login-registro">
          ¿No tienes una cuenta?{' '}
          <Link to="/registro">Regístrate aquí</Link>
        </p>
      </div>
    </section>
  );
}
