package com.example.cletaeats_mobile.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

/**
 * Centraliza todas las operaciones con Firebase Authentication.
 * Solo se usa cuando DataMode == CLOUD.
 */
object FirebaseAuthHelper {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    val currentUser: FirebaseUser? get() = auth.currentUser

    /**
     * Inicia sesión con email/password en Firebase Auth.
     * Retorna el FirebaseUser si tiene éxito, null si falla.
     */
    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("Usuario nulo en Firebase"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Registra un nuevo usuario en Firebase Auth.
     * Llamar solo durante registro nuevo en modo CLOUD.
     */
    suspend fun createUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("Error al crear usuario"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cierra sesión en Firebase Auth.
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * Obtiene el UID de Firebase del usuario actual (para identificarlo en Firestore).
     */
    val uid: String? get() = auth.currentUser?.uid
}