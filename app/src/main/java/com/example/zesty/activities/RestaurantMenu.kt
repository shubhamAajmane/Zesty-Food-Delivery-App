package com.example.zesty.activities

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.zesty.R
import com.example.zesty.adapters.MenuAdapter
import com.example.zesty.database.RestaurantDatabase
import com.example.zesty.database.RestaurantEntity
import com.example.zesty.util.ConnectivityManager
import com.example.zesty.dataclass.MenuItem
import org.json.JSONException

class RestaurantMenu : AppCompatActivity() {

    lateinit var toolbar: Toolbar
    lateinit var ivFav: ImageView
    lateinit var rvMenuItem: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var menuAdapter: MenuAdapter
    lateinit var resId: String
    lateinit var resName: String
    val menuList = arrayListOf<MenuItem>()

    lateinit var progressBarLayout: RelativeLayout
    lateinit var btnProceedCart: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_menu)

        if (intent != null) {
            resId = intent?.getStringExtra("resId").toString()
            resName = intent?.getStringExtra("resName").toString()
        }

        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.title = resName
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ivFav = findViewById(R.id.ivFavIcon)
        btnProceedCart = findViewById(R.id.btnProceedToCart)
        rvMenuItem = findViewById(R.id.rvMenuItems)
        layoutManager = LinearLayoutManager(this@RestaurantMenu as Context)
        progressBarLayout = findViewById(R.id.progressBarLayout)
        progressBarLayout.visibility = View.VISIBLE

        val resEntity = RestaurantEntity(resId, resName)

        val isFav = DBFavRestaurants(this@RestaurantMenu, resEntity).execute().get()

        if (isFav) {
            ivFav.setImageResource(R.drawable.ic_favselect)
        } else {
            ivFav.setImageResource(R.drawable.ic_favfood)
        }

        connectAPI()
    }

    fun connectAPI() {

        if (ConnectivityManager().checkConnectivity(this@RestaurantMenu)) {
            val queue = Volley.newRequestQueue(this@RestaurantMenu)
            val url = "http://13.235.250.119/v2/restaurants/fetch_result/$resId"
            val jsonObjectRequest = object : JsonObjectRequest(
                Method.GET, url, null,
                Response.Listener {
                    try {
                        val jsonObject = it.getJSONObject("data")

                        if (jsonObject.getBoolean("success")) {

                            val data = jsonObject.getJSONArray("data")

                            for (i in 0 until data.length()) {

                                val menuJsonObject = data.getJSONObject(i)

                                val menuObject = MenuItem(
                                    menuJsonObject.getString("id"),
                                    menuJsonObject.getString("name"),
                                    menuJsonObject.getString("cost_for_one")
                                )
                                menuList.add(menuObject)
                                menuAdapter = MenuAdapter(
                                    this@RestaurantMenu,
                                    resId,
                                    resName,
                                    menuList,
                                    btnProceedCart,
                                    rvMenuItem
                                )
                                rvMenuItem.layoutManager = layoutManager
                                rvMenuItem.adapter = menuAdapter
                            }

                        } else {
                            Toast.makeText(
                                this@RestaurantMenu,
                                "Something went Wrong",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (ex: JSONException) {
                        Toast.makeText(
                            this@RestaurantMenu,
                            "Some Error Occurred",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    progressBarLayout.visibility = View.INVISIBLE
                }, Response.ErrorListener {

                    Toast.makeText(this@RestaurantMenu, "Unable to fetch Data", Toast.LENGTH_SHORT)
                        .show()

                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] =
                        "application/json"
                    headers["token"] = "0aee2c7976e28b"
                    return headers
                }
            }
            queue.add(jsonObjectRequest)
        } else {

            val dialog = AlertDialog.Builder(this@RestaurantMenu)
            dialog.setTitle("No Internet")
            dialog.setMessage("Please check your Internet Connection")
            dialog.setPositiveButton("Open Settings") { text, listener ->

                val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(intent)
            }
            dialog.setNegativeButton("Exit") { text, listener ->
                ActivityCompat.finishAffinity(this@RestaurantMenu)
            }

            dialog.create()
            dialog.show()
        }
    }

    fun emptyCart() {
        val dialog = AlertDialog.Builder(this@RestaurantMenu)
        dialog.setTitle("Go Back?")
        dialog.setMessage("Going back will remove all the items from the cart")
        dialog.setPositiveButton("Yes") { text, listener ->
            super.onBackPressed()
        }
        dialog.setNegativeButton("No") { text, listener ->
        }

        dialog.create()
        dialog.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        emptyCart()
        return true
    }

    override fun onBackPressed() {
        emptyCart()
    }

    class DBFavRestaurants(val context: Context, val resEntity: RestaurantEntity) :
        AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg params: Void?): Boolean {
            val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurants_db")
                .build()

            val restaurantEntity: RestaurantEntity? =
                db.restDao().getRestaurantById(resEntity.resId.toString())
            db.close()

            return restaurantEntity != null
        }

    }
}