package com.example.thai_dessert_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


class AccountPage : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DessertAdapter
    private val favoriteDesserts = mutableListOf<Dessert>()
    private lateinit var allDesserts: List<Dessert>
    private lateinit var userEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_page)

        val buttonBackfav = findViewById<Button>(R.id.button_back_tofav)

        buttonBackfav.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }


        val logoutButton: Button = findViewById(R.id.buttonLogout)
        val emailTextView: TextView = findViewById(R.id.textViewEmail)
        recyclerView = findViewById(R.id.recyclerViewFavorites)

        // --- Get current user ---
        userEmail = intent.getStringExtra("email") ?: ""
        emailTextView.text = "Logged in as: $userEmail"

        // --- Load all desserts ---
        allDesserts = loadDessertsFromCsv(this, "thai_dessert_list.csv")

        // --- Load favorites from SharedPreferences ---
        val sharedPref = getSharedPreferences("FavoritesPrefs", Context.MODE_PRIVATE)
        val favoriteNames = sharedPref.getStringSet(userEmail, setOf()) ?: setOf()

        // Map favorite names to Dessert objects
        favoriteDesserts.addAll(favoriteNames.mapNotNull { id ->
            allDesserts.find { it.id == id }
        })

        // --- RecyclerView setup ---
        adapter = DessertAdapter(favoriteDesserts)
        recyclerView.layoutManager = GridLayoutManager(this, 3) // grid like ListPage
        recyclerView.adapter = adapter

        // --- Make hearts clickable to remove favorites ---
        adapter.setHeartClickListener { dessert ->
            // Remove from SharedPreferences
            val favs = sharedPref.getStringSet(userEmail, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
            favs.remove(dessert.name)
            sharedPref.edit().putStringSet(userEmail, favs).apply()

            // Remove from local list and update RecyclerView
            favoriteDesserts.remove(dessert)
            adapter.submitList(favoriteDesserts.toList())
            Toast.makeText(this, "${dessert.name} removed from favorites", Toast.LENGTH_SHORT).show()
        }

        // --- Logout button ---
        logoutButton.setOnClickListener {
            getSharedPreferences("CurrentUserPrefs", Context.MODE_PRIVATE)
                .edit().remove("currentUserEmail").apply()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginPage::class.java))
            finish()
        }
    }

    // --- CSV loader ---
    private fun loadDessertsFromCsv(context: Context, thai_dessert_list: String): List<Dessert> {
        val desserts = mutableListOf<Dessert>()
        context.assets.open(thai_dessert_list).bufferedReader().useLines { lines ->
            lines.drop(1).forEach { line ->
                val tokens = line.split(",")
                if (tokens.size >= 6) {
                    desserts.add(
                        Dessert(
                            id = tokens[0],
                            name = tokens[1],
                            north = tokens[2].trim() == "1",
                            northeast = tokens[3].trim() == "1",
                            central = tokens[4].trim() == "1",
                            south = tokens[5].trim() == "1"
                        )
                    )
                }
            }
        }
        return desserts
    }
}
