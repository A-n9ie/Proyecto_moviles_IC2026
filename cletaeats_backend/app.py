# app.py
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from routes.auth_routes import router as auth_router
from routes.admin_routes import router as admin_router
from routes.public_routes import router as public_router
from routes.cliente_routes import router as cliente_router
from routes.repartidor_routes import router as repartidor_router
from fastapi.staticfiles import StaticFiles
import os

app = FastAPI(title="CleteaEats API", version="2.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
    allow_credentials=False,
)
os.makedirs("uploads", exist_ok=True)
app.mount("/uploads", StaticFiles(directory="uploads"), name="uploads")
app.include_router(auth_router,    prefix="/auth")
app.include_router(admin_router,   prefix="/admin")
app.include_router(public_router)
app.include_router(cliente_router, prefix="/cliente")
app.include_router(repartidor_router, prefix="/repartidor")

# Para correr: uvicorn app:app --reload --port 8000