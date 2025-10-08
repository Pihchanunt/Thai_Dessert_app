package com.example.thai_dessert_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.Firebase
import com.google.firebase.ai.*
import com.example.thai_dessert_app.IngredientAdapter


// -----------------------------
// DetailPage Activity
class DetailPage : AppCompatActivity() {

    lateinit var ingredientname: TextView

    lateinit var ingredientquantity: TextView
    private lateinit var IngredientAdapter: IngredientAdapter
    private lateinit var dessertId: String
    private lateinit var textViewRecipe: TextView

    // Gemini model
    private val generativeModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-2.5-flash")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_detail_page)

        // account button
        val accountButton: ImageButton = findViewById(R.id.account_icon)
        val sharedPref = getSharedPreferences("CurrentUserPrefs", Context.MODE_PRIVATE)

        accountButton.setOnClickListener {
            val currentUserEmail = sharedPref.getString("currentUserEmail", null)
            if (currentUserEmail.isNullOrEmpty()) {
                // No user logged in → go to LoginPage
                val intent = Intent(this, LoginPage::class.java)
                startActivity(intent)
            } else {
                // User logged in → go to AccountPage
                val intent = Intent(this, AccountPage::class.java)
                intent.putExtra("email", currentUserEmail)
                startActivity(intent)
            }
        }

        // --- UI Elements ---
        val buttonIcon = findViewById<ImageButton>(R.id.clickable_icon)
        val buttonBack = findViewById<Button>(R.id.button_back)
        val imageView = findViewById<ImageView>(R.id.detailImage)
        val nameView = findViewById<TextView>(R.id.detailName)
        textViewRecipe = findViewById(R.id.textViewRecipe)

        buttonIcon.setOnClickListener {
            startActivity(Intent(this, Class.forName("com.example.thai_dessert_app.HomePage")))
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        buttonBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        // --- Dessert info ---
        dessertId = intent.getStringExtra("dessertId") ?: "default_dessert"
        val dessertName = intent.getStringExtra("dessertName") ?: "Thai Dessert"
        nameView.text = dessertName

        val imageResId = resources.getIdentifier("${dessertId}_pic", "drawable", packageName)
        imageView.setImageResource(if (imageResId != 0) imageResId else R.drawable.ic_launcher_foreground)

        // --- Ingredients TextViews ---
        ingredientname = findViewById(R.id.ingredientName)
        ingredientquantity = findViewById(R.id.ingredientQuantity)

        val ingredientList = loadIngredientsCsv(dessertId)

        ingredientname.text = ingredientList.joinToString("\n") { it.name }
        ingredientquantity.text = ingredientList.joinToString("\n") { it.quantity }

        val ingredientNames = ingredientList.map { it.name }
        val ingredientQuantities = ingredientList.map { it.quantity }
        generateRecipe(dessertName, ingredientNames, ingredientQuantities)

    }

    // Load ingredients from CSV
    private fun loadIngredientsCsv(dessertId: String): List<Ingredient> {
        val ingredients = mutableListOf<Ingredient>()
        try {
            val fileName = "${dessertId}_ingredients.csv"
            val inputStream = assets.open(fileName)
            inputStream.bufferedReader().useLines { lines ->
                lines.drop(1).forEach { line ->
                    val tokens = line.split(",")
                    if (tokens.size >= 2) {
                        ingredients.add(Ingredient(tokens[0].trim(), tokens[1].trim()))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ingredients
    }

    // Generate recipe using Gemini
    private fun generateRecipe(
        dessertName: String,
        ingredientNames: List<String>,
        ingredientQuantities: List<String>
    ) {
        textViewRecipe.text = "Generating recipe for $dessertName..."

        val ingredientsWithQuantities = ingredientNames.zip(ingredientQuantities)
            .joinToString(", ") { (name, quantity) -> "$name ($quantity)" }

        val promptText = """
        Create a clear, easy-to-follow Thai dessert recipe for "$dessertName"
        using the following ingredients:
        $ingredientsWithQuantities.
        Include preparation steps and cooking instructions.
        No formatting, just plain text.
        Don't need to show ingredients again.
    """.trimIndent()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = generativeModel.generateContent(promptText)
                withContext(Dispatchers.Main) {
                    textViewRecipe.text = response.text ?: "No recipe generated."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    textViewRecipe.text = "Failed to generate recipe.\n${e.message}"
                }
            }
        }
    }

}
