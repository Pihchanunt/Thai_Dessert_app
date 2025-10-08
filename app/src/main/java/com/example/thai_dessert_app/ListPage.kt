package com.example.thai_dessert_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

// -------------------- DATA CLASSES --------------------
data class Dessert(
    val id: String,
    val name: String,
    val north: Boolean,
    val northeast: Boolean,
    val central: Boolean,
    val south: Boolean
)

data class Ingredient(
    val name: String,
    val quantity: String
)

// -------------------- ADAPTER (with heart) --------------------
class DessertAdapter(var desserts: List<Dessert>) : RecyclerView.Adapter<DessertAdapter.DessertViewHolder>() {
    private var heartClickListener: ((Dessert) -> Unit)? = null

    fun setHeartClickListener(listener: (Dessert) -> Unit) {
        heartClickListener = listener
    }

    fun submitList(list: List<Dessert>) {
        desserts = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = desserts.size

    inner class DessertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.dessertImage)
        val textView: TextView = itemView.findViewById(R.id.dessertName)
        val favoriteButton: ImageButton = itemView.findViewById(R.id.buttonFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DessertViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dessert, parent, false)
        return DessertViewHolder(view)
    }

    override fun onBindViewHolder(holder: DessertViewHolder, position: Int) {
        val dessert = desserts[position]
        holder.textView.text = dessert.name

        val context = holder.itemView.context
        val imageResId =
            context.resources.getIdentifier("${dessert.id}_pic", "drawable", context.packageName)
        holder.imageView.setImageResource(if (imageResId != 0) imageResId else R.drawable.ic_launcher_foreground)

        // Determine current logged-in user
        val currentUserPref = context.getSharedPreferences("CurrentUserPrefs", Context.MODE_PRIVATE)
        val userEmail = currentUserPref.getString("currentUserEmail", null)

        // Load favorites for this user (stored by dessert.id)
        val favPref = context.getSharedPreferences("FavoritesPrefs", Context.MODE_PRIVATE)
        val userFavs =
            if (userEmail != null) favPref.getStringSet(userEmail, setOf())?.toMutableSet()
                ?: mutableSetOf()
            else mutableSetOf<String>()

        // Set heart icon based on whether this dessert.id is in user's favorites
        holder.favoriteButton.setBackgroundResource(
            if (dessert.id in userFavs) R.drawable.icon_heart_coral
            else R.drawable.icon_heart_white
        )

        // Heart click: toggle favorite (or send user to login if not logged in)
        holder.favoriteButton.setOnClickListener {
            val ctx = holder.itemView.context
            val currentUser = ctx.getSharedPreferences("CurrentUserPrefs", Context.MODE_PRIVATE)
                .getString("currentUserEmail", null)

            if (currentUser.isNullOrEmpty()) {
                // Not logged in -> prompt and open LoginPage
                Toast.makeText(ctx, "Please log in to save favorites", Toast.LENGTH_SHORT).show()
                val intent = Intent(ctx, LoginPage::class.java)
                ctx.startActivity(intent)
                return@setOnClickListener
            }

            // Re-read set fresh (avoid stale closure)
            val prefs = ctx.getSharedPreferences("FavoritesPrefs", Context.MODE_PRIVATE)
            val favs = prefs.getStringSet(currentUser, setOf())?.toMutableSet() ?: mutableSetOf()

            if (dessert.id in favs) {
                favs.remove(dessert.id)
                holder.favoriteButton.setBackgroundResource(R.drawable.icon_heart_white)
            } else {
                favs.add(dessert.id)
                holder.favoriteButton.setBackgroundResource(R.drawable.icon_heart_coral)
            }
            prefs.edit().putStringSet(currentUser, favs).apply()
        }

        // Item click -> go to detail page (same as before)
        holder.itemView.setOnClickListener {
            val ctx = holder.itemView.context
            val intent = Intent(ctx, DetailPage::class.java)
            intent.putExtra("dessertId", dessert.id)
            intent.putExtra("dessertName", dessert.name)
            ctx.startActivity(intent)
            if (ctx is AppCompatActivity) {
                ctx.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
        holder.favoriteButton.setOnClickListener {
            val currentUser = context.getSharedPreferences("CurrentUserPrefs", Context.MODE_PRIVATE)
                .getString("currentUserEmail", null)

            if (currentUser.isNullOrEmpty()) {
                Toast.makeText(context, "Please log in to save favorites", Toast.LENGTH_SHORT)
                    .show()
                context.startActivity(Intent(context, LoginPage::class.java))
            } else {
                val prefs = context.getSharedPreferences("FavoritesPrefs", Context.MODE_PRIVATE)
                val favs =
                    prefs.getStringSet(currentUser, setOf())?.toMutableSet() ?: mutableSetOf()

                if (dessert.id in favs) {
                    favs.remove(dessert.id)
                    holder.favoriteButton.setBackgroundResource(R.drawable.icon_heart_white)
                } else {
                    favs.add(dessert.id)
                    holder.favoriteButton.setBackgroundResource(R.drawable.icon_heart_coral)
                }
                prefs.edit().putStringSet(currentUser, favs).apply()

                heartClickListener?.invoke(dessert)
            }
        }


        fun submitList(list: List<Dessert>) {
            desserts = list
            notifyDataSetChanged()
        }
    }
}
    // -------------------- LIST PAGE ACTIVITY --------------------
class ListPage : AppCompatActivity() {

    private lateinit var chipGroup: ChipGroup
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DessertAdapter
    private lateinit var allDesserts: List<Dessert>
    private lateinit var searchBar: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_page)

        // back button
        val buttonBack = findViewById<Button>(R.id.button_back)

        buttonBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
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

        // back/home icon
        findViewById<ImageButton>(R.id.clickable_icon).setOnClickListener {
            startActivity(Intent(this, HomePage::class.java))
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        // load data
        allDesserts = loadDessertsFromCsv(this, "thai_dessert_list.csv")

        // recycler
        recyclerView = findViewById(R.id.dessertRecyclerView)
        adapter = DessertAdapter(allDesserts)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(this, 3)

        // chips
        chipGroup = findViewById(R.id.chipGroup)
        val chipAll = findViewById<Chip>(R.id.chipAll)
        val chipNorth = findViewById<Chip>(R.id.chipNorth)
        val chipNortheast = findViewById<Chip>(R.id.chipNortheast)
        val chipCentral = findViewById<Chip>(R.id.chipCentral)
        val chipSouth = findViewById<Chip>(R.id.chipSouth)
        chipGroup.isSingleSelection = true
        chipGroup.isSelectionRequired = false

        // manually manage selection
        fun selectChip(chip: Chip) {
            // uncheck all first
            chipAll.isChecked = false
            chipNorth.isChecked = false
            chipNortheast.isChecked = false
            chipCentral.isChecked = false
            chipSouth.isChecked = false
            // check the desired chip
            chip.isChecked = true
        }

        chipAll.setOnClickListener { selectChip(chipAll); filterDesserts() }
        chipNorth.setOnClickListener {
            if (chipNorth.isChecked) {
                selectChip(chipNorth)
            } else {
                selectChip(chipAll) // if deselected, go back to All
            }
            filterDesserts()
        }
        chipNortheast.setOnClickListener {
            if (chipNortheast.isChecked) selectChip(chipNortheast)
            else selectChip(chipAll)
            filterDesserts()
        }
        chipCentral.setOnClickListener {
            if (chipCentral.isChecked) selectChip(chipCentral)
            else selectChip(chipAll)
            filterDesserts()
        }
        chipSouth.setOnClickListener {
            if (chipSouth.isChecked) selectChip(chipSouth)
            else selectChip(chipAll)
            filterDesserts()
        }

// set default selection
        val region = intent.getStringExtra("selectedRegion")
        when (region) {
            "North" -> selectChip(chipNorth)
            "Northeast" -> selectChip(chipNortheast)
            "Central" -> selectChip(chipCentral)
            "South" -> selectChip(chipSouth)
            else -> selectChip(chipAll)
        }

        // search
        searchBar = findViewById(R.id.search_bar)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterDesserts()
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // region from previous page
        when (intent.getStringExtra("selectedRegion")) {
            "North" -> chipNorth.isChecked = true
            "Northeast" -> chipNortheast.isChecked = true
            "Central" -> chipCentral.isChecked = true
            "South" -> chipSouth.isChecked = true
            else -> chipAll.isChecked = true
        }

        // initial
        filterDesserts()
    }

    private fun loadDessertsFromCsv(context: Context, filename: String): List<Dessert> {
        val desserts = mutableListOf<Dessert>()
        context.assets.open(filename).bufferedReader().useLines { lines ->
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

    private fun filterDessertsByRegion(desserts: List<Dessert>, region: String): List<Dessert> {
        return when (region) {
            "North" -> desserts.filter { it.north }
            "Northeast" -> desserts.filter { it.northeast }
            "Central" -> desserts.filter { it.central }
            "South" -> desserts.filter { it.south }
            else -> desserts
        }
    }

    private fun filterDesserts() {
        val query = searchBar.text.toString().trim().lowercase()

        val selectedChipId = chipGroup.checkedChipId
        val region = when (selectedChipId) {
            R.id.chipNorth -> "North"
            R.id.chipNortheast -> "Northeast"
            R.id.chipCentral -> "Central"
            R.id.chipSouth -> "South"
            else -> "All" // default if no chip selected
        }


        val regionFiltered = filterDessertsByRegion(allDesserts, region)
        val finalFiltered = if (query.isEmpty()) regionFiltered
        else regionFiltered.filter { it.name.lowercase().contains(query) }
        adapter.submitList(finalFiltered)
    }
}

class IngredientAdapter(private var ingredients: List<Ingredient>) :
    RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder>() {

    class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(android.R.id.text1)
        val quantityText: TextView = itemView.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return IngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val ingredient = ingredients[position]
        holder.nameText.text = ingredient.name
        holder.quantityText.text = ingredient.quantity
    }

    override fun getItemCount(): Int = ingredients.size
}


