package edu.udb.retrofitappcrud.vistas

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.udb.retrofitappcrud.AppConfig
import edu.udb.retrofitappcrud.R
import edu.udb.retrofitappcrud.adaptadores.AlumnoAdapter
import edu.udb.retrofitappcrud.interaces.AlumnoApi
import edu.udb.retrofitappcrud.modelos.Alumno
import okhttp3.Credentials
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AlumnoAdapter
    private lateinit var api: AlumnoApi
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val fab_agregar: FloatingActionButton = findViewById<FloatingActionButton>(R.id.fab_agregar)
        //bottom nav
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


        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Crea un cliente OkHttpClient con un interceptor que agrega las credenciales de autenticación
        val sharedPref = applicationContext.getSharedPreferences("sh", 0)
        val username = sharedPref.getString(LoginActivity.usernameKey, "")
        val password = sharedPref.getString(LoginActivity.passwordKey, "")

        println(username)
        println(password)


        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader(
                        "Authorization",
                        Credentials.basic(username.toString(), password.toString())
                    )
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
        api = retrofit.create(AlumnoApi::class.java)

        cargarDatos(api)

        // Cuando el usuario quiere agregar un nuevo registro
        fab_agregar.setOnClickListener(View.OnClickListener {
            val i = Intent(baseContext, CrearAlumnoActivity::class.java)
            i.putExtra("auth_username", username)
            i.putExtra("auth_password", password)
            startActivity(i)
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

    override fun onResume() {
        super.onResume()
        cargarDatos(api)
    }

    private fun cargarDatos(api: AlumnoApi) {


        val call = api.obtenerAlumnos()
        call.enqueue(object : Callback<List<Alumno>> {
            override fun onResponse(call: Call<List<Alumno>>, response: Response<List<Alumno>>) {
                if (response.isSuccessful) {
                    val alumnos = response.body()
                    if (alumnos != null) {
                        adapter = AlumnoAdapter(alumnos)
                        recyclerView.adapter = adapter

                        // Establecemos el escuchador de clics en el adaptador
                        adapter.setOnItemClickListener(object : AlumnoAdapter.OnItemClickListener {
                            override fun onItemClick(alumno: Alumno) {
                                val opciones = arrayOf("Modificar Alumno", "Eliminar Alumno")

                                AlertDialog.Builder(this@MainActivity)
                                    .setTitle(alumno.nombre)
                                    .setItems(opciones) { dialog, index ->
                                        when (index) {
                                            0 -> Modificar(alumno)
                                            1 -> eliminarAlumno(alumno, api)
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
                        this@MainActivity,
                        "Error al obtener los alumnos 1",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<Alumno>>, t: Throwable) {
                Log.e("API", "Error al obtener los alumnos: ${t.message}")
                Toast.makeText(
                    this@MainActivity,
                    "Error al obtener los alumnos 2",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun Modificar(alumno: Alumno) {
        // Creamos un intent para ir a la actividad de actualización de alumnos
        val i = Intent(getBaseContext(), ActualizarAlumnoActivity::class.java)
        // Pasamos el ID del alumno seleccionado a la actividad de actualización
        i.putExtra("alumno_id", alumno.id)
        i.putExtra("nombre", alumno.nombre)
        i.putExtra("apellido", alumno.apellido)
        i.putExtra("edad", alumno.edad)
        // Iniciamos la actividad de actualización de alumnos
        startActivity(i)
    }

    private fun eliminarAlumno(alumno: Alumno, api: AlumnoApi) {
        val alumnoTMP = Alumno(alumno.id, "", "", -987)
        Log.e("API", "id : $alumno")
        val llamada = api.eliminarAlumno(alumno.id, alumnoTMP)
        llamada.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Alumno eliminado", Toast.LENGTH_SHORT).show()
                    cargarDatos(api)
                } else {
                    val error = response.errorBody()?.string()
                    Log.e("API", "Error al eliminar alumno : $error")
                    Toast.makeText(
                        this@MainActivity,
                        "Error al eliminar alumno 1",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("API", "Error al eliminar alumno : $t")
                Toast.makeText(this@MainActivity, "Error al eliminar alumno 2", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.logout, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val biulder = AlertDialog.Builder(this)

        return when (item.itemId) {
            R.id.actionLogout -> {
                logout()
                true
            }
            else -> return super.onOptionsItemSelected(item)

        }
    }

    private fun logout() {
        val usernameKey = "username"
        val passwordKey = "password"
        val i = Intent(this, LoginActivity::class.java)
        val sharedPref = applicationContext.getSharedPreferences("sh", 0)
        val editor = sharedPref.edit()
        editor.remove(usernameKey)
        editor.remove(passwordKey)
        editor.apply()
        startActivity(i)
    }
}