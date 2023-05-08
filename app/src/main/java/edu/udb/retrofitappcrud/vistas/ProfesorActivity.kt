package edu.udb.retrofitappcrud.vistas

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.udb.retrofitappcrud.AppConfig
import edu.udb.retrofitappcrud.R
import edu.udb.retrofitappcrud.adaptadores.ProfesorAdapter
import edu.udb.retrofitappcrud.interaces.ProfesorAPI
import edu.udb.retrofitappcrud.modelos.Profesor
import okhttp3.Credentials
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class ProfesorActivity : AppCompatActivity() {
    lateinit var bottomNavigationView: BottomNavigationView
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: ProfesorAdapter
    private lateinit var api: ProfesorAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profesor)

        val sharedPref = applicationContext.getSharedPreferences("sh", 0)
        val username = sharedPref.getString(LoginActivity.usernameKey, "")
        val password = sharedPref.getString(LoginActivity.passwordKey, "")


        val fab_agregar: FloatingActionButton = findViewById<FloatingActionButton>(R.id.fab_agregar)
        fab_agregar.setOnClickListener(View.OnClickListener {
            val i = Intent(baseContext, CrearProfesor::class.java)
            i.putExtra("auth_username", username)
            i.putExtra("auth_password", password)
            startActivity(i)
        })

        recyclerView = findViewById(R.id.recyclerViewP)
        recyclerView.layoutManager = LinearLayoutManager(this)


        // Crea un cliente OkHttpClient con un interceptor que agrega las credenciales de autenticación

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", Credentials.basic(username.toString(), password.toString()))
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
        api = retrofit.create(ProfesorAPI::class.java)
        cargarDatos(api)

        // Crea una instancia del servicio que utiliza la autenticación HTTP básica
        api = retrofit.create(ProfesorAPI::class.java)
        bottomNavigationView = findViewById(R.id.bottomNavView)

        bottomNavigationView.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.home -> showAlumno()

                R.id.search -> showProfesor()

                else -> {


                }

            }

            true

        }
    }

    override fun onResume() {
        super.onResume()
        cargarDatos(api)
    }

    private fun cargarDatos(api: ProfesorAPI) {

        val call = api.obtenerProfesores()
        call.enqueue(object : Callback<List<Profesor>> {
            override fun onResponse(
                call: Call<List<Profesor>>,
                response: Response<List<Profesor>>

            ) {
                if (response.isSuccessful) {
                    val profesor = response.body()
                    if (profesor != null) {
                        adapter = ProfesorAdapter(profesor)
                        recyclerView.adapter = adapter

                        // Establecemos el escuchador de clics en el adaptador
                        adapter.setOnItemClickListener(object :
                            ProfesorAdapter.OnItemClickListener {
                            override fun onItemClick(profesor: Profesor) {
                                val opciones = arrayOf("Modificar Profesor", "Eliminar Profesor")

                                AlertDialog.Builder(this@ProfesorActivity)
                                    .setTitle(profesor.nombre)
                                    .setItems(opciones) { dialog, index ->
                                        when (index) {
                                            0 -> modificarProfesor(profesor)
                                            1 -> eliminarProfesor(profesor, api)
                                        }
                                    }
                                    .setNegativeButton("Cancelar", null)
                                    .show()
                            }


                        })
                    }
                } else {
                    val error = response.errorBody()?.string()
                    Log.e("API", "Error al obtener los alumnos: $error")
                    Toast.makeText(
                        this@ProfesorActivity,
                        "Error al obtener los alumnos 1",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<Profesor>>, t: Throwable) {
                Log.e("API", "Error al obtener los alumnos: ${t.message}")
                Toast.makeText(
                    this@ProfesorActivity,
                    "Error al obtener los alumnos 2",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun modificarProfesor(profesor: Profesor) {
        // Creamos un intent para ir a la actividad de actualización de profesores
        val i = Intent(this, ActualizarProfesorActivity::class.java)
        //pasamos los datos
        i.putExtra("profesor_id", profesor.id)
        i.putExtra("nombre", profesor.nombre)
        i.putExtra("apellido", profesor.apellido)
        i.putExtra("carnet", profesor.carnet)
        // Iniciamos la actividad de actualización de alumnos
        startActivity(i)


    }

    private fun eliminarProfesor(profesor: Profesor, api: ProfesorAPI) {
        val profesorTMP = Profesor(profesor.id, "", "", "www")
        Log.e("API", "id : $profesor")
        val llamada = api.eliminarProfesor(profesor.id)
        llamada.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ProfesorActivity, "Profesor eliminado", Toast.LENGTH_SHORT)
                        .show()
                    cargarDatos(api)
                } else {
                    val error = response.errorBody()?.string()
                    Log.e("API", "Error al eliminar profesor : $error")
                    Toast.makeText(
                        this@ProfesorActivity,
                        "Error al eliminar profesor 1",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("API", "Error al eliminar profesor : $t")
                Toast.makeText(
                    this@ProfesorActivity,
                    "Error al eliminar profesor 2",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun showAlumno() {
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
    }

    private fun showProfesor() {
        val i = Intent(this, ProfesorActivity::class.java)
        startActivity(i)
    }
}