package com.example.cletaeats_mobile.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.cletaeats_mobile.data.local.SessionManager
import com.example.cletaeats_mobile.data.local.db.CategoriaLocalEntity
import com.example.cletaeats_mobile.data.local.db.CletaEatsDatabase
import com.example.cletaeats_mobile.data.local.db.ComboEntity
import com.example.cletaeats_mobile.data.local.db.PedidoEntity
import com.example.cletaeats_mobile.data.local.db.PerfilLocalEntity
import com.example.cletaeats_mobile.data.local.db.RestauranteEntity
import com.example.cletaeats_mobile.data.local.db.TarjetaLocalEntity
import com.example.cletaeats_mobile.data.local.db.UsuarioLocalEntity
import com.example.cletaeats_mobile.data.remote.IComboApi
import com.example.cletaeats_mobile.data.remote.IPedidoApi
import com.example.cletaeats_mobile.data.remote.IPerfilApi
import com.example.cletaeats_mobile.data.remote.IRepartidorPerfilApi
import com.example.cletaeats_mobile.data.remote.IRestauranteApi
import com.example.cletaeats_mobile.data.remote.ITarjetaApi
import com.example.cletaeats_mobile.data.remote.RetrofitClient
import com.example.cletaeats_mobile.data.repository.RestauranteCloudRepositoryImpl
import com.example.cletaeats_mobile.domain.model.Restaurante
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SyncManager(private val context: Context, private val session: SessionManager? = null) {

    private val cloud      = RestauranteCloudRepositoryImpl()
    private val db         = CletaEatsDatabase.getInstance(context)
    private val dao        = db.restauranteDao()
    private val daoCombo   = db.comboDao()
    private val daoPedido  = db.pedidoDao()
    private val daoUsuario = db.usuarioLocalDao()
    private val firestore  = FirebaseFirestore.getInstance()
    fun hayInternet(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Descarga restaurantes desde Cloud y los guarda en SQLite.
     * Llamar cuando el usuario entra en modo CLOUD o al iniciar app con internet.
     */
    suspend fun sincronizarDesdeCloud() = withContext(Dispatchers.IO) {
        if (!hayInternet()) return@withContext SyncResult.SinInternet
        try {
            val result = cloud.obtenerRestaurantes()
            if (result is com.example.cletaeats_mobile.domain.Result.Success) {
                val entities = result.data.map { r ->
                    RestauranteEntity(
                        id           = r.id,
                        nombre       = r.nombre,
                        categorias   = r.categorias.joinToString(","),
                        direccion    = r.direccion,
                        imagenUrl    = r.imagenUrl,
                        estado       = r.estado,
                        latitud      = r.latitud,
                        longitud     = r.longitud,
                        sincronizado = true
                    )
                }
                dao.insertarTodos(entities)
                SyncResult.Exito(entities.size)
            } else {
                SyncResult.Error("No se pudo obtener datos de Cloud")
            }
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Error de sincronización")
        }
    }

    /**
     * Descarga restaurantes desde el API remoto y los guarda en SQLite.
     * Llamar después de un login exitoso en modo API_REMOTA para tener
     * datos disponibles cuando se use modo LOCAL_SQLITE sin internet.
     */
    suspend fun sincronizarDesdeApi(token: String, rol: String = "") = withContext(Dispatchers.IO) {
        if (!hayInternet()) return@withContext SyncResult.SinInternet
        try {
            // 1. Restaurantes
            val restApi = RetrofitClient.create<com.example.cletaeats_mobile.data.remote.IRestauranteApi>()
            val respRest = restApi.obtenerRestaurantes("Bearer $token")
            var totalRest = 0
            if (respRest.isSuccessful) {
                val entities = respRest.body()!!.map { r ->
                    RestauranteEntity(
                        id           = r.id,
                        nombre       = r.nombre,
                        categorias   = r.categorias.joinToString(",") { it.nombre },
                        direccion    = r.direccion,
                        imagenUrl    = r.imagenUrl,
                        estado       = r.estado,
                        latitud      = r.latitud,
                        longitud     = r.longitud,
                        sincronizado = false
                    )
                }
                dao.insertarTodos(entities)
                totalRest = entities.size
                android.util.Log.d("SYNC", "Restaurantes guardados en SQLite: $totalRest")

                // 2. Combos de cada restaurante
                val comboApi = RetrofitClient.create<IComboApi>()
                entities.forEach { rest ->
                    val respCombos = comboApi.obtenerCombos("Bearer $token", rest.id)
                    if (respCombos.isSuccessful) {
                        val comboEntities = respCombos.body()!!.map { c ->
                            ComboEntity(
                                id            = c.id,
                                restauranteId = c.restauranteId,
                                numeroCombo   = c.numeroCombo,
                                nombre        = c.nombre,
                                descripcion   = c.descripcion,
                                precio        = c.precio,
                                imagenUrl     = c.imagenUrl
                            )
                        }
                        daoCombo.insertarTodos(comboEntities)
                    }
                }
            }

            // 3. Pedidos del usuario actual
            try {
                val pedidoApi = RetrofitClient.create<IPedidoApi>()
                val respPedidos = pedidoApi.obtenerPedidosCliente("Bearer $token")
                if (respPedidos.isSuccessful) {
                    val pedidoEntities = respPedidos.body()!!.map { p ->
                        PedidoEntity(
                            id = p.id,
                            estado = p.estado,
                            estadoTexto = p.estadoTexto,
                            restauranteNombre = p.restauranteNombre,
                            tipoComida = p.tipoComida,
                            clienteNombre = p.clienteNombre,
                            distanciaKm = p.distanciaKm,
                            fechaCreacion = p.fechaCreacion,
                            fechaEntrega = p.fechaEntrega ?: "",
                            itemsCount = p.itemsCount,
                            restauranteLatitud = p.restauranteLatitud ?: 0.0,
                            restauranteLongitud = p.restauranteLongitud ?: 0.0
                        )
                    }
                    daoPedido.limpiarTodos()
                    daoPedido.insertarTodos(pedidoEntities)
                    android.util.Log.d("SYNC", "Pedidos guardados en SQLite: ${pedidoEntities.size}")
                }
            } catch (e: Exception) {
                android.util.Log.w("SYNC", "No se pudieron sincronizar pedidos: ${e.message}")
                // No es fatal — continuamos
            }


            try {
                val restApiCat = RetrofitClient.create<IRestauranteApi>()
                val respCat = restApiCat.obtenerCategorias("Bearer $token")
                if (respCat.isSuccessful) {
                    val catEntities = respCat.body()!!.map { c ->
                        CategoriaLocalEntity(id = c.id, nombre = c.nombre)
                    }
                    db.categoriaLocalDao().limpiarTodas()
                    db.categoriaLocalDao().insertarTodas(catEntities)
                    android.util.Log.d("SYNC", "Categorías guardadas: ${catEntities.size}")
                }
            } catch (e: Exception) {
                android.util.Log.w("SYNC", "No se pudieron sincronizar categorías: ${e.message}")
            }

            // 5. Perfil del usuario (cliente o repartidor)
            try {
                sincronizarPerfilDesdeApi(token, rol)
            } catch (e: Exception) {
                android.util.Log.w("SYNC", "No se pudo sincronizar perfil: ${e.message}")
            }

            SyncResult.Exito(totalRest)
        } catch (e: Exception) {
            android.util.Log.e("SYNC", "Error sincronizando desde API: ${e.message}")
            SyncResult.Error(e.message ?: "Error de sincronización")
        }
    }

    private suspend fun sincronizarPerfilDesdeApi(token: String, rol: String) {
        val emailActivo = session?.getEmail()
            ?.takeIf { it.isNotEmpty() }
            ?: session?.getOfflineEmail()
            ?: return
        val idPerfil = db.usuarioLocalDao().buscarPorEmail(emailActivo)?.idPerfil ?: return

        when (rol.uppercase()) {
            "CLIENTE" -> {
                val perfilApi = RetrofitClient.create<IPerfilApi>()
                val resp = perfilApi.obtenerPerfil("Bearer $token")
                if (resp.isSuccessful) {
                    val body = resp.body()!!
                    db.perfilLocalDao().guardar(
                        PerfilLocalEntity(
                            idPerfil  = idPerfil,
                            rol       = "CLIENTE",
                            nombre    = body.nombre,
                            telefono  = body.telefono,
                            direccion = body.direccion,
                            cedula    = body.cedula,
                            imagenUrl = body.imagen_url
                        )
                    )
                    // Tarjetas del cliente
                    val tarjetaApi = RetrofitClient.create<ITarjetaApi>()
                    val respTarjetas = tarjetaApi.listarTarjetas("Bearer $token")
                    if (respTarjetas.isSuccessful) {
                        val entities = respTarjetas.body()!!.map { t ->
                            TarjetaLocalEntity(
                                id = t.id, clienteId = t.clienteId,
                                numero = t.numero, alias = t.alias,
                                fechaVencimiento = t.fechaVencimiento,
                                cvv = t.cvv, esPrincipal = t.esPrincipal
                            )
                        }
                        db.tarjetaLocalDao().limpiarTodas()
                        db.tarjetaLocalDao().insertarTodas(entities)
                    }
                }
            }
            "REPARTIDOR" -> {
                val perfilApi = RetrofitClient.create<IRepartidorPerfilApi>()
                val resp = perfilApi.obtenerPerfil("Bearer $token")
                if (resp.isSuccessful) {
                    val body = resp.body()!!
                    db.perfilLocalDao().guardar(
                        PerfilLocalEntity(
                            idPerfil = idPerfil,
                            rol = "REPARTIDOR",
                            nombre = body.nombre,
                            telefono = body.telefono,
                            direccion = body.direccion,
                            cedula = body.cedula,
                            correo = body.correo,
                            tarjeta = body.tarjeta,
                            rating = body.rating,
                            amonestaciones = body.amonestaciones
                        )
                    )
                }
            }
        }
    }

    private fun RestauranteEntity.toDomain() = Restaurante(
        id         = id, nombre = nombre,
        categorias = categorias.split(",").map { it.trim() },
        direccion  = direccion, imagenUrl = imagenUrl,
        estado     = estado, latitud = latitud, longitud = longitud
    )

    /**
     * Guarda el usuario que acaba de iniciar sesión en SQLite local.
     * Solo guarda este usuario (no todos los del sistema).
     */
    suspend fun guardarUsuarioLocal(
        idUsuario: Int, email: String, nombre: String,
        rol: String, idPerfil: Int, token: String
    ) = withContext(Dispatchers.IO) {
        try {
            daoUsuario.guardar(
                UsuarioLocalEntity(
                    idUsuario = idUsuario,
                    email = email,
                    nombre = nombre,
                    rol = rol,
                    idPerfil = idPerfil,
                    token = token
                )
            )
            android.util.Log.d("SYNC", "Usuario guardado en SQLite local: $email")
        } catch (e: Exception) {
            android.util.Log.e("SYNC", "Error guardando usuario local: ${e.message}")
        }
    }


    suspend fun sincronizarHaciaCloud() = withContext(Dispatchers.IO) {
        if (!hayInternet()) return@withContext SyncResult.SinInternet
        try {
            // 1. Restaurantes
            val todosRest = dao.obtenerTodos()
            todosRest.forEach { entity ->
                cloud.guardar(entity.toDomain())
            }

            // 2. Combos — agrupados por restauranteId
            val todosCombos = daoCombo.obtenerTodos()   // ← necesitas agregar este método al DAO (ver paso siguiente)
            todosCombos.forEach { c ->
                firestore.collection("combos")
                    .document("${c.id}")
                    .set(mapOf(
                        "id"            to c.id,
                        "restauranteId" to c.restauranteId,
                        "numeroCombo"   to c.numeroCombo,
                        "nombre"        to c.nombre,
                        "descripcion"   to c.descripcion,
                        "precio"        to c.precio,
                        "imagenUrl"     to c.imagenUrl
                    )).await()
            }

            // 3. Pedidos
            val todosPedidos = daoPedido.obtenerTodos()
            todosPedidos.forEach { p ->
                firestore.collection("pedidos")
                    .document("${p.id}")
                    .set(mapOf(
                        "id"                  to p.id,
                        "estado"              to p.estado,
                        "estadoTexto"         to p.estadoTexto,
                        "restauranteNombre"   to p.restauranteNombre,
                        "tipoComida"          to p.tipoComida,
                        "clienteNombre"       to p.clienteNombre,
                        "distanciaKm"         to p.distanciaKm,
                        "fechaCreacion"       to p.fechaCreacion,
                        "fechaEntrega"        to p.fechaEntrega,
                        "itemsCount"          to p.itemsCount,
                        "restauranteLatitud"  to p.restauranteLatitud,
                        "restauranteLongitud" to p.restauranteLongitud
                    )).await()
            }

            // AGREGAR dentro de sincronizarHaciaCloud(), después del bloque de pedidos:

            // 4. Categorías
            val todasCat = db.categoriaLocalDao().obtenerTodas()
            todasCat.forEach { c ->
                firestore.collection("categorias").document("${c.id}")
                    .set(mapOf("id" to c.id, "nombre" to c.nombre)).await()
            }

            // 5. Perfil del usuario activo
            val idPerfilActivo = session?.getIdPerfil() ?: -1
            if (idPerfilActivo == -1) {
                return@withContext SyncResult.Error("No hay perfil activo")
            }
            val perfil = db.perfilLocalDao().obtener(idPerfilActivo)
            perfil?.let { p ->
                val coleccion = if (p.rol == "CLIENTE") "clientes" else "repartidores"
                firestore.collection(coleccion).document("${p.idPerfil}")
                    .set(mapOf(
                        "idPerfil"  to p.idPerfil,
                        "rol"       to p.rol,
                        "nombre"    to p.nombre,
                        "telefono"  to p.telefono,
                        "direccion" to p.direccion,
                        "cedula"    to p.cedula,
                        "imagenUrl" to p.imagenUrl,
                        "correo"    to p.correo,
                        "tarjeta"   to p.tarjeta,
                        "rating"    to p.rating
                    )).await()
            }

        // 6. Tarjetas (solo si es cliente)
            val tarjetas = db.tarjetaLocalDao().obtenerPorCliente(idPerfilActivo)
            tarjetas.forEach { t ->
                firestore.collection("tarjetas").document("${t.id}")
                    .set(mapOf(
                        "id" to t.id, "clienteId" to t.clienteId,
                        "numero" to t.numero, "alias" to t.alias,
                        "fechaVencimiento" to t.fechaVencimiento,
                        "esPrincipal" to t.esPrincipal
                        // CVV NO se sube a Firestore por seguridad
                    )).await()
            }

            android.util.Log.d("SYNC",
                "Cloud sync: ${todosRest.size} restaurantes, ${todosCombos.size} combos, ${todosPedidos.size} pedidos")
            SyncResult.Exito(todosRest.size)
        } catch (e: Exception) {
            android.util.Log.e("SYNC", "Error subiendo a Cloud: ${e.message}")
            SyncResult.Error(e.message ?: "Error subiendo a Cloud")
        }
    }
}


sealed class SyncResult {
    data class Exito(val cantidad: Int)  : SyncResult()
    data class Error(val msg: String)    : SyncResult()
    object SinInternet                   : SyncResult()
}