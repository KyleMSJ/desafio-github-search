package br.com.igorbag.githubsearch.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.databinding.ActivityMainBinding
import br.com.igorbag.githubsearch.data.GitHubService
import br.com.igorbag.githubsearch.domain.Repository
import br.com.igorbag.githubsearch.ui.adapter.RepositoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    private lateinit var nomeUsuario: EditText
    private lateinit var btnConfirmar: Button
    private lateinit var listaRepositories: RecyclerView
    private lateinit var githubApi: GitHubService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        setupListeners()
        showUserName()
        setupRetrofit()
    }

    // Metodo responsavel por realizar o setup da view e recuperar os Ids do layout
    private fun setupView() {
        nomeUsuario = binding.etNomeUsuario
        btnConfirmar = binding.btnConfirmar
        listaRepositories = binding.rvListaRepositories
    }

    //metodo responsavel por configurar os listeners click da tela
    private fun setupListeners() {
        btnConfirmar.setOnClickListener{
            val userNameText = nomeUsuario.text.toString()
            saveUserLocal(userNameText)
            getAllReposByUserName(userNameText)
        }
    }


    // salvar o usuario preenchido no EditText utilizando uma SharedPreferences
    private fun saveUserLocal(nome: String) {
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString(getString(R.string.saved_name), nome)
            apply()
        }
        Toast.makeText(this, R.string.register_successful, Toast.LENGTH_SHORT).show()
    }

    private fun showUserName(): String {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        val nomeSalvo = sharedPref.getString(getString(R.string.saved_name), "")

        nomeUsuario.setText(nomeSalvo)

        return sharedPref.getString(R.string.saved_name.toString(), "") ?: ""
    }

    //Metodo responsavel por fazer a configuracao base do Retrofit
    fun setupRetrofit() {

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        githubApi = retrofit.create(GitHubService::class.java)
    }

    //Metodo responsavel por buscar todos os repositorios do usuario fornecido
    private fun getAllReposByUserName(nome: String) {
        githubApi.getAllRepositoriesByUser(nome).enqueue(object : Callback<List<Repository>> {
            override fun onResponse(call: Call<List<Repository>>, response: Response<List<Repository>>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        setupAdapter(it)
                    }
                }
                else {
                    Toast.makeText(this@MainActivity, R.string.repo_register_error, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                Toast.makeText(this@MainActivity, R.string.repo_register_error, Toast.LENGTH_SHORT).show()
            }
        })

    }

    // Metodo responsavel por realizar a configuracao do adapter
    fun setupAdapter(list: List<Repository>) {
        val repoAdapter = RepositoryAdapter(list, this::shareRepositoryLink, this::openBrowser)

        listaRepositories.apply {
            adapter = repoAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }


    // Metodo responsavel por compartilhar o link do repositorio selecionado
    fun shareRepositoryLink(urlRepository: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    // Metodo responsavel por abrir o browser com o link informado do repositorio
    fun openBrowser(urlRepository: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(urlRepository)
            )
        )

    }

}