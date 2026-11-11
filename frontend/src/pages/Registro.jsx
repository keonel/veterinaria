import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import useFormulario from "../hooks/useFormulario.js";
import { api } from "../services/api.js";
import { regiones } from "../data/regiones.js";

import {
  validarCorreo,
  validarRun,
  validarContrasena,
} from "../utils/validaciones.js";

const inicial = {
  run: "",
  nombre: "",
  apellidos: "",
  correo: "",
  telefono: "",
  region: "",
  comuna: "",
  direccion: "",
  contrasena: "",
  confirmarContrasena: "",
  terminos: false,
};


function obtenerErroresContrasena(clave) {
  const errores = [];

  if (clave.length < 5) {
    errores.push("Debe tener al menos 5 caracteres.");
  }

  if (clave.length > 10) {
    errores.push("No puede superar los 10 caracteres.");
  }

  if (!/[A-Z]/.test(clave)) {
    errores.push("Debe incluir al menos una letra mayúscula.");
  }

  if (!/[0-9]/.test(clave)) {
    errores.push("Debe incluir al menos un número.");
  }

  return errores;
}


function validar(v) {
  const e = {};

  if (!validarRun(v.run.trim())) {
    e.run = "RUN inválido (ej: 12345678-9)";
  }

  if (v.nombre.trim() === "") {
    e.nombre = "El nombre es obligatorio";
  }

  if (v.apellidos.trim() === "") {
    e.apellidos = "Los apellidos son obligatorios";
  }

  if (!validarCorreo(v.correo.trim())) {
    e.correo =
      "El correo debe ser @duoc.cl, @profesor.duoc.cl o @gmail.com";
  }

  if (v.telefono.trim() === "") {
    e.telefono = "El teléfono es obligatorio";
  }

  if (!v.region) {
    e.region = "Selecciona una región";
  }

  if (!v.comuna) {
    e.comuna = "Selecciona una comuna";
  }

  if (v.direccion.trim() === "") {
    e.direccion = "La dirección es obligatoria";
  }

  if (!validarContrasena(v.contrasena)) {
    e.contrasena = "La contraseña no cumple todos los requisitos";
  }

  if (v.contrasena !== v.confirmarContrasena) {
    e.confirmarContrasena = "Las contraseñas no coinciden";
  }

  if (!v.terminos) {
    e.terminos = "Debes aceptar los términos y condiciones";
  }

  return e;
}

export default function Registro() {
  const navegar = useNavigate();

  const {
    valores,
    setValores,
    errores,
    manejarCambio,
    enviar,
  } = useFormulario(inicial, validar);

  const [errorServidor, setErrorServidor] = useState("");

  const comunas = regiones[valores.region] || [];

  function cambiarRegion(evento) {
    setValores((actual) => ({
      ...actual,
      region: evento.target.value,
      comuna: "",
    }));
  }


  const erroresContrasena = obtenerErroresContrasena(
    valores.contrasena
  );

  const contrasenaValida = validarContrasena(
    valores.contrasena
  );

  
  async function guardarUsuario(v) {
    setErrorServidor("");
    try {
      // El backend valida de nuevo, revisa duplicados (409), cifra la contraseña
      // y crea la cuenta siempre con rol CLIENTE.
      await api("/auth/registro", {
        method: "POST",
        body: {
          run: v.run.trim(),
          nombre: v.nombre.trim(),
          apellidos: v.apellidos.trim(),
          correo: v.correo.trim().toLowerCase(),
          telefono: v.telefono.trim(),
          region: v.region,
          comuna: v.comuna,
          direccion: v.direccion.trim(),
          contrasena: v.contrasena,
        },
      });

      alert("Registro exitoso. Ahora puedes iniciar sesión.");
      navegar("/login");
    } catch (error) {
      // Muestra el mensaje del backend y, si vienen, los errores campo por campo
      setErrorServidor(
        [error.message, ...Object.values(error.campos || {})].join(" ")
      );
    }
  }

  function manejarEnvio(evento) {
    enviar(guardarUsuario)(evento);
  }

  return (
    <section className="login-section registro-section">
      <div className="login-container registro-container">

        <div className="registro-encabezado">
          <div
            className="registro-huellas"
            aria-hidden="true"
          >
            🐾
          </div>

          <h1>Crear cuenta</h1>

          <p>
            Regístrate para acceder a Veterinaria San Marcos
          </p>
        </div>

        <p className="registro-obligatorios">
          <span>*</span> Campos obligatorios
        </p>

        <form
          id="formRegistro"
          onSubmit={manejarEnvio}
          noValidate
        >

          
          <div className="form-group registro-campo">
            <label htmlFor="run">
              RUN <span className="asterisco-obligatorio">*</span>
            </label>

            <input
              id="run"
              name="run"
              type="text"
              placeholder="12345678-9"
              value={valores.run}
              onChange={manejarCambio}
              className={errores.run ? "error" : ""}
            />

            {errores.run && (
              <small className="mensaje-error">
                {errores.run}
              </small>
            )}
          </div>

          
          <div className="form-group registro-campo">
            <label htmlFor="nombre">
              Nombre <span className="asterisco-obligatorio">*</span>
            </label>

            <input
              id="nombre"
              name="nombre"
              type="text"
              value={valores.nombre}
              onChange={manejarCambio}
              className={errores.nombre ? "error" : ""}
            />

            {errores.nombre && (
              <small className="mensaje-error">
                {errores.nombre}
              </small>
            )}
          </div>

          
          <div className="form-group registro-campo">
            <label htmlFor="apellidos">
              Apellidos <span className="asterisco-obligatorio">*</span>
            </label>

            <input
              id="apellidos"
              name="apellidos"
              type="text"
              value={valores.apellidos}
              onChange={manejarCambio}
              className={errores.apellidos ? "error" : ""}
            />

            {errores.apellidos && (
              <small className="mensaje-error">
                {errores.apellidos}
              </small>
            )}
          </div>

          
          <div className="form-group registro-campo">
            <label htmlFor="correo">
              Correo electrónico{" "}
              <span className="asterisco-obligatorio">*</span>
            </label>

            <input
              id="correo"
              name="correo"
              type="email"
              placeholder="usuario@gmail.com"
              value={valores.correo}
              onChange={manejarCambio}
              className={errores.correo ? "error" : ""}
            />

            {errores.correo && (
              <small className="mensaje-error">
                {errores.correo}
              </small>
            )}
          </div>

          
          <div className="form-group registro-campo">
            <label htmlFor="telefono">
              Teléfono <span className="asterisco-obligatorio">*</span>
            </label>

            <input
              id="telefono"
              name="telefono"
              type="tel"
              placeholder="+56 9 1234 5678"
              value={valores.telefono}
              onChange={manejarCambio}
              className={errores.telefono ? "error" : ""}
            />

            {errores.telefono && (
              <small className="mensaje-error">
                {errores.telefono}
              </small>
            )}
          </div>

          
          <div className="form-group registro-campo">
            <label htmlFor="region">
              Región <span className="asterisco-obligatorio">*</span>
            </label>

            <select
              id="region"
              name="region"
              value={valores.region}
              onChange={cambiarRegion}
              className={errores.region ? "error" : ""}
            >
              <option value="">
                Selecciona una región
              </option>

              {Object.keys(regiones).map((r) => (
                <option key={r} value={r}>
                  {r}
                </option>
              ))}
            </select>

            {errores.region && (
              <small className="mensaje-error">
                {errores.region}
              </small>
            )}
          </div>

          
          <div className="form-group registro-campo">
            <label htmlFor="comuna">
              Comuna <span className="asterisco-obligatorio">*</span>
            </label>

            <select
              id="comuna"
              name="comuna"
              value={valores.comuna}
              onChange={manejarCambio}
              disabled={!valores.region}
              className={errores.comuna ? "error" : ""}
            >
              <option value="">
                Selecciona una comuna
              </option>

              {comunas.map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>

            {errores.comuna && (
              <small className="mensaje-error">
                {errores.comuna}
              </small>
            )}
          </div>

          
          <div className="form-group registro-campo">
            <label htmlFor="direccion">
              Dirección <span className="asterisco-obligatorio">*</span>
            </label>

            <input
              id="direccion"
              name="direccion"
              type="text"
              value={valores.direccion}
              onChange={manejarCambio}
              className={errores.direccion ? "error" : ""}
            />

            {errores.direccion && (
              <small className="mensaje-error">
                {errores.direccion}
              </small>
            )}
          </div>

          
          <div className="form-group registro-campo">
            <label htmlFor="contrasena">
              Contraseña{" "}
              <span className="asterisco-obligatorio">*</span>
            </label>

            <input
              id="contrasena"
              name="contrasena"
              type="password"
              value={valores.contrasena}
              onChange={manejarCambio}
              className={errores.contrasena ? "error" : ""}
              aria-describedby="requisitos-contrasena"
            />

            <p className="registro-ayuda-contrasena">
              La contraseña debe tener entre 5 y 10 caracteres,
              incluir una mayúscula y al menos un número.
            </p>

            
            <div id="requisitos-contrasena" aria-live="polite">
              {valores.contrasena !== "" && (
                <>
                  {erroresContrasena.length > 0 ? (
                    erroresContrasena.map((error) => (
                      <p
                        key={error}
                        style={{
                          color: "#c62828",
                          margin: "4px 0",
                          fontSize: "13px",
                        }}
                      >
                        ✗ {error}
                      </p>
                    ))
                  ) : (
                    <p
                      style={{
                        color: "#218838",
                        margin: "4px 0",
                        fontSize: "13px",
                      }}
                    >
                      ✓ ¡Contraseña válida!
                    </p>
                  )}
                </>
              )}
            </div>

            {errores.contrasena && (
              <small className="mensaje-error">
                {errores.contrasena}
              </small>
            )}
          </div>

          
          <div className="form-group registro-campo">
            <label htmlFor="confirmarContrasena">
              Confirmar contraseña{" "}
              <span className="asterisco-obligatorio">*</span>
            </label>

            <input
              id="confirmarContrasena"
              name="confirmarContrasena"
              type="password"
              value={valores.confirmarContrasena}
              onChange={manejarCambio}
              className={
                errores.confirmarContrasena ? "error" : ""
              }
            />

            {valores.confirmarContrasena !== "" && (
              <small
                style={{
                  display: "block",
                  marginTop: "4px",
                  color:
                    valores.confirmarContrasena === valores.contrasena &&
                    contrasenaValida
                      ? "#218838"
                      : "#c62828",
                }}
              >
                {valores.confirmarContrasena !== valores.contrasena
                  ? "✗ Las contraseñas no coinciden."
                  : contrasenaValida
                    ? "✓ Las contraseñas coinciden."
                    : "La contraseña todavía no cumple los requisitos."}
              </small>
            )}

            {errores.confirmarContrasena && (
              <small className="mensaje-error">
                {errores.confirmarContrasena}
              </small>
            )}
          </div>

          
          <div className="registro-terminos">
            <input
              id="terminos"
              name="terminos"
              type="checkbox"
              checked={valores.terminos}
              onChange={manejarCambio}
            />

            <label htmlFor="terminos">
              Acepto los términos y condiciones
            </label>
          </div>

          {errores.terminos && (
            <small className="mensaje-error">
              {errores.terminos}
            </small>
          )}

          {errorServidor && (
            <p className="mensaje-error" role="alert">
              {errorServidor}
            </p>
          )}

          <button
            type="submit"
            className="btn btn-primary registro-boton"
          >
            Crear cuenta
          </button>
        </form>

        <p className="registro-enlace">
          ¿Ya tienes cuenta?{" "}
          <Link to="/login">Inicia sesión</Link>
        </p>

      </div>
    </section>
  );
}
