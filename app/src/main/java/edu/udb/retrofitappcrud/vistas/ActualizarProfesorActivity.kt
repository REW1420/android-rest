package edu.udb.retrofitappcrud.vistas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import edu.udb.retrofitappcrud.AppConfig
import edu.udb.retrofitappcrud.R
import edu.udb.retrofitappcrud.interaces.ProfesorAPI
import edu.udb.retrofitappcrud.modelos.Alumno
import edu.udb.retrofitappcrud.modelos.Profesor
import okhttp3.Credentials
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ActualizarProfesorActivity : AppCompatActivity() {

    private lateinit var nombreEditText: EditText
    private lateinit var apellidoEditText: EditText
    private lateinit var carnetEditText: EditText
    private lateinit var actualizarButton: Button

    // Obtener las credenciales de autenticación
    val auth_username = "admin"
    val auth_password = "admin123"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actualizar_profesor)

        nombreEditText = findViewById(R.id.nombreEditTextP)
        apellidoEditText = findViewById(R.id.apellidoEditTextP)
        carnetEditText = findViewById(R.id.editTextCarnetP)
        actualizarButton = findViewById(R.id.actualizarButtonP)

        // Crea un cliente OkHttpClient con un interceptor que agrega las credenciales de autenticación
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", Credentials.basic(auth_username, auth_password))
                    .build()
                chain.proceed(request)
            }
            .build()

        // Crea una instancia de Retrofit con el cliente OkHttpClient
        val retrofit = Retrofit.Builder()
            .baseUrl(AppConfig.baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        // Crea una instancia del servicio que utiliza la autenticación HTTP básica
        val api = retrofit.create(ProfesorAPI::class.java)

        // Obtener el ID del profesor de la actividad anterior
        val profesorID = intent.getIntExtra("profesor_id", 0)
        Log.e("API", "alumnoId : $profesorID")

        val nombre = intent.getStringExtra("nombre").toString()
        val apellido = intent.getStringExtra("apellido").toString()
        val carnet = intent.getStringExtra("carnet").toString()

        nombreEditText.setText(nombre)
        apellidoEditText.setText(apellido)
        carnetEditText.setText(carnet)


        actualizarButton.setOnClickListener {
            //profesor a actualizar
            val profesorActualizado = Profesor(
                profesorID,
                nombreEditText.text.toString(),
                apellidoEditText.text.toString(),
                carnetEditText.text.toString()
            )

            val jsonprofesorActualizado = Gson().toJson(profesorActualizado)
            Log.d("API", "JSON enviado: $jsonprofesorActualizado")

            // Realizar una solicitud PUT para actualizar el objeto Profesor

            api.actualizarProfesor(profesorID, profesorActualizado)
                .enqueue(object : Callback<Profesor> {
                    override fun onResponse(call: Call<Profesor>, response: Response<Profesor>) {
                        if (response.isSuccessful && response.body() != null) {
                            // Si la solicitud es exitosa, mostrar un mensaje de éxito en un Toast
                            Toast.makeText(
                                this@ActualizarProfesorActivity,
                                "Profesor actualizado correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            val i = Intent(getBaseContext(), ProfesorActivity::class.java)
                            startActivity(i)
                        } else {
                            // Si la respuesta del servidor no es exitosa, manejar el error
                            try {
                                val errorJson = response.errorBody()?.string()
                                val errorObj = errorJson?.let { it1 -> JSONObject(it1) }
                                val errorMessage = errorObj?.getString("message")
                                Toast.makeText(
                                    this@ActualizarProfesorActivity,
                                    errorMessage,
                                    Toast.LENGTH_SHORT
                                ).show()
                            } catch (e: Exception) {
                                // Si no se puede parsear la respuesta del servidor, mostrar un mensaje de error genérico
                                Toast.makeText(
                                    this@ActualizarProfesorActivity,
                                    "Error al actualizar el profesor",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.e("API", "Error al parsear el JSON: ${e.message}")
                            }
                        }

                    }

                    override fun onFailure(call: Call<Profesor>, t: Throwable) {

                        // Si la solicitud falla, mostrar un mensaje de error en un Toast
                        Log.e("API", "onFailure : $t")
                        Toast.makeText(
                            this@ActualizarProfesorActivity,
                            "Error al actualizar el profesor",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Si la respuesta JSON está malformada, manejar el error
                        try {
                            val gson = GsonBuilder().setLenient().create()
                            val error = t.message ?: ""
                            gson.fromJson(error, Alumno::class.java)
                            // trabajar con el objeto Alumno si se puede parsear
                        } catch (e: JsonSyntaxException) {
                            Log.e("API", "Error al parsear el JSON: ${e.message}")
                        } catch (e: IllegalStateException) {
                            Log.e("API", "Error al parsear el JSON: ${e.message}")
                        }
                    }

                })
        }
    }
}