package edu.udb.retrofitappcrud.vistas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
<<<<<<< HEAD
=======
import edu.udb.retrofitappcrud.AppConfig
>>>>>>> master
import edu.udb.retrofitappcrud.R
import edu.udb.retrofitappcrud.interaces.ProfesorAPI
import edu.udb.retrofitappcrud.modelos.Profesor
import okhttp3.Credentials
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CrearProfesor : AppCompatActivity() {
    private lateinit var nombreEditText: EditText
    private lateinit var apellidoEditText: EditText
    private lateinit var carnetEditText: EditText

    // Obtener las credenciales de autenticación
    var auth_username = ""
    var auth_password = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_profesor)

        // Obtención de datos que envia actividad anterior
        val datos: Bundle? = intent.extras
        if (datos != null) {
            auth_username = datos.getString("auth_username").toString()
            auth_password = datos.getString("auth_password").toString()
        }

        nombreEditText = findViewById(R.id.editTextNombre)
        apellidoEditText = findViewById(R.id.editTextApellido)
        carnetEditText = findViewById(R.id.editTextCarnet)
        val crearButton = findViewById<Button>(R.id.btnGuardar)

        crearButton.setOnClickListener {
            val nombre = nombreEditText.text.toString()
            val apellido = apellidoEditText.text.toString()
            val carnet = carnetEditText.text.toString()

            val profesor = Profesor(0, nombre, apellido, carnet)
            Log.e("API", "auth_username: $auth_username")
            Log.e("API", "auth_password: $auth_password")

            // Crea un cliente OkHttpClient con un interceptor que agrega las credenciales de autenticación
            val client = OkHttpClient.Builder().addInterceptor { chain ->
                    val request = chain.request().newBuilder()
<<<<<<< HEAD
                        .addHeader("Authorization", Credentials.basic(auth_username, auth_password))
=======
                        .addHeader("Authorization", Credentials.basic("admin", "admin123"))
>>>>>>> master
                        .build()
                    chain.proceed(request)
                }.build()
            // Crea una instancia de Retrofit con el cliente OkHttpClient
<<<<<<< HEAD
            val retrofit = Retrofit.Builder().baseUrl("http://10.0.2.2/api/")
=======
            val retrofit = Retrofit.Builder().baseUrl(AppConfig.baseUrl)
>>>>>>> master
                .addConverterFactory(GsonConverterFactory.create()).client(client).build()

            // Crea una instancia del servicio que utiliza la autenticación HTTP básica
            val api = retrofit.create(ProfesorAPI::class.java)

            api.crearProfesor(profesor).enqueue(object : Callback<Profesor> {
                override fun onResponse(call: Call<Profesor>, response: Response<Profesor>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@CrearProfesor, "Profesor creado", Toast.LENGTH_SHORT)
                            .show()
                        val i = Intent(baseContext, ProfesorActivity::class.java)
                        startActivity(i)

                    } else {
                        val error = response.errorBody()?.string()
                        Log.e("API", "Error crear alumno: $error")
                        Toast.makeText(
                            this@CrearProfesor, "Error al crear el profesor", Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Profesor>, t: Throwable) {
                    Toast.makeText(
                        this@CrearProfesor, "Error al crear el alumno", Toast.LENGTH_SHORT
                    ).show()
                }

            })
        }
    }
}