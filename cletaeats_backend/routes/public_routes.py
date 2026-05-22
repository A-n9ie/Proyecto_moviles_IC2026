# routes/public_routes.py
from typing import Optional
from fastapi import APIRouter, HTTPException, Query
from data.repositories.restaurante_repository import RestauranteRepository
from data.repositories.combo_repository import ComboRepository
from data.repositories.categoria_repository import CategoriaRepository
from data.repositories.producto_repository import ProductoRepository

router = APIRouter()

@router.get("/restaurantes")
def listar_restaurantes(categoria: Optional[str] = Query(None)):
    repo_rest = RestauranteRepository()
    repo_cat  = CategoriaRepository()
    restaurantes = repo_rest.obtener_todos_activos()
    result = []
    for r in restaurantes:
        cats = repo_cat.obtener_por_restaurante(r.id)
        if categoria and not any(c["nombre"].lower() == categoria.lower() for c in cats):
            continue
        result.append({
            "id":         r.id,
            "nombre":     r.nombre,
            "direccion":  r.direccion,
            "imagen_url": r.imagen_url,
            "estado":     r.estado,
            "categorias": cats
        })
    return result

@router.get("/combos")
def listar_combos(restaurante: int = Query(..., description="ID del restaurante")):
    repo_combo = ComboRepository()
    repo_prod  = ProductoRepository()
    combos = repo_combo.obtener_por_restaurante(restaurante)
    result = []
    for c in combos:
        prods = repo_prod.obtener_por_combo(c.id)
        result.append({
            "id":             c.id,
            "restaurante_id": c.restaurante_id,
            "numero_combo":   c.numero_combo,
            "nombre":         c.nombre,
            "descripcion":    c.descripcion,
            "precio":         c.precio,
            "imagen_url":     c.imagen_url,
            "estado":         c.estado,
            "productos":      prods
        })
    return result

@router.get("/categorias")
def listar_categorias_publicas():
    return CategoriaRepository().listar_todas()