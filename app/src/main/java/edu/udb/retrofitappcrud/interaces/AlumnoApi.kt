package edu.udb.retrofitappcrud.interaces

import edu.udb.retrofitappcrud.modelos.Alumno
import retrofit2.Call
import retrofit2.http.*

interface AlumnoApi {

    @GET("alumno.php")
    fun obtenerAlumnos(): Call<List<Alumno>>

    @GET("alumno/{id}")
    fun obtenerAlumnoPorId(@Path("id") id: Int): Call<Alumno>

    @POST("alumno.php")
    fun crearAlumno(@Body alumno: Alumno): Call<Alumno>

    @PUT("alumno.php/{id}")
    fun actualizarAlumno(@Path("id") id: Int, @Body alumno: Alumno): Call<Alumno>

    @PUT("alumno.php/{id}")
    fun eliminarAlumno(@Path("id") id: Int, @Body alumno: Alumno): Call<Void>
}