const express = require('express');
const { Pool } = require('pg');
const cors = require('cors');
require('dotenv').config();

const app = express();
app.use(cors());
app.use(express.json());

const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  ssl: { rejectUnauthorized: false }
});

// Crear tabla si no existe
pool.query(`
  CREATE TABLE IF NOT EXISTS cliente (
    id SERIAL PRIMARY KEY,
    nombre TEXT NOT NULL,
    telefono TEXT NOT NULL,
    correo TEXT NOT NULL,
    direccion TEXT NOT NULL
  )
`);

// GET todos los clientes
app.get('/clientes', async (req, res) => {
  const { rows } = await pool.query('SELECT * FROM cliente ORDER BY nombre ASC');
  res.json(rows);
});

// GET buscar clientes
app.get('/clientes/buscar', async (req, res) => {
  const { q } = req.query;
  const filtro = `%${q}%`;
  const { rows } = await pool.query(
    'SELECT * FROM cliente WHERE nombre ILIKE $1 OR telefono ILIKE $1 OR correo ILIKE $1 OR direccion ILIKE $1 ORDER BY nombre ASC',
    [filtro]
  );
  res.json(rows);
});

// POST crear cliente
app.post('/clientes', async (req, res) => {
  const { nombre, telefono, correo, direccion } = req.body;
  const { rows } = await pool.query(
    'INSERT INTO cliente (nombre, telefono, correo, direccion) VALUES ($1, $2, $3, $4) RETURNING *',
    [nombre, telefono, correo, direccion]
  );
  res.status(201).json(rows[0]);
});

// PUT actualizar cliente
app.put('/clientes/:id', async (req, res) => {
  const { nombre, telefono, correo, direccion } = req.body;
  const { rows } = await pool.query(
    'UPDATE cliente SET nombre=$1, telefono=$2, correo=$3, direccion=$4 WHERE id=$5 RETURNING *',
    [nombre, telefono, correo, direccion, req.params.id]
  );
  res.json(rows[0]);
});

// DELETE eliminar cliente
app.delete('/clientes/:id', async (req, res) => {
  await pool.query('DELETE FROM cliente WHERE id=$1', [req.params.id]);
  res.status(204).send();
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`API running on port ${PORT}`));