package edu.udb.retrofitappcrud.interaces

import edu.udb.retrofitappcrud.modelos.Alumno
import edu.udb.retrofitappcrud.modelos.Profesor
import retrofit2.Call
import retrofit2.http.*

interface ProfesorAPI {

    @GET("profesor.php")
    fun obtenerProfesores(): Call<List<Profesor>>

    @POST("profesor.php")
    fun crearProfesor(@Body profesor: Profesor): Call<Profesor>

    @DELETE("profesor.php/{id}")
    fun eliminarProfesor(@Path("id") id: Int): Call<Void>
}