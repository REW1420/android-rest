package edu.udb.retrofitappcrud.vistas

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import edu.udb.retrofitappcrud.AppConfig
import edu.udb.retrofitappcrud.adaptadores.AlumnoAdapter
import edu.udb.retrofitappcrud.databinding.ActivityLoginBinding
import edu.udb.retrofitappcrud.interaces.AlumnoApi
import edu.udb.retrofitappcrud.modelos.Alumno
import okhttp3.Credentials
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class LoginActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityLoginBinding

    companion object {
        const val usernameKey = "username"
        const val passwordKey = "password"
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        setupSplashScreen()

        var editTextCorreo = findViewById<EditText>(binding.correo.id)
        var editTextContra = findViewById<EditText>(binding.contra.id)
        var bottonIniciar = findViewById<Button>(binding.iniciar.id)

        /// BORRAR ESTP
        editTextCorreo.setText("admin")
        editTextContra.setText("admin123")

        bottonIniciar.setOnClickListener {


            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("Authorization", Credentials.basic(editTextCorreo.text.toString(), editTextContra.text.toString()))
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
            val api = retrofit.create(AlumnoApi::class.java)
            val call = api.obtenerAlumnos()
            call.enqueue(object : Callback<List<Alumno>> {
                override fun onResponse(call: Call<List<Alumno>>, response: Response<List<Alumno>>) {
                    if (response.isSuccessful) {
                        val sharedPref = applicationContext.getSharedPreferences("sh", 0)
                        with(sharedPref.edit()) {
                            putString(usernameKey, editTextCorreo.text.toString())
                            putString(passwordKey, editTextContra.text.toString())
                            apply()
                        }
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    } else {
                        val error = response.errorBody()?.string()
                        Log.e("API", "Error con las credenciales: $error")
                        Toast.makeText(
                            this@LoginActivity,
                            "Error con Credenciales, Intentelo de nuevo",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                }
                override fun onFailure(call: Call<List<Alumno>>, t: Throwable) {

                    Log.e("API", "Error con credenciales: ${t.message}")
                    Toast.makeText(
                        this@LoginActivity,
                        "Error con credenciales",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

        }


    }

    private fun setupSplashScreen() {
        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    // Check whether the initial data is ready.
                    content.viewTreeObserver.removeOnPreDrawListener(this)
                    return true
                }
            }
        )
    }


}