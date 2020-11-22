package com.example.zesty.activities

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.zesty.R
import com.example.zesty.adapters.CartAdapter
import com.example.zesty.util.ConnectivityManager
import com.example.zesty.dataclass.FoodItem
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class CartActivity : AppCompatActivity() {

    lateinit var rvCart: RecyclerView
    lateinit var layoutManagerCart: RecyclerView.LayoutManager
    lateinit var cartAdapter: CartAdapter
    var listCart = listOf<FoodItem>()
    lateinit var toolbar: androidx.appcompat.widget.Toolbar
    lateinit var btnPlaceOrder: Button
    lateinit var sharedPreferences: SharedPreferences
    lateinit var progressBarLayout: RelativeLayout
    lateinit var resId: String
    lateinit var resName: String
    lateinit var selectedIds: ArrayList<String>

    var totalAmount = 0
    var cartListItems = arrayListOf<FoodItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        if (intent != null) {
            resId = intent?.getStringExtra("resId").toString()
            resName = intent?.getStringExtra("resName").toString()
            selectedIds = intent?.getStringArrayListExtra("idsOfSelectedItems") as ArrayList<String>
        }

        toolbar = findViewById(R.id.cartToolbar)
        rvCart = findViewById(R.id.rvOrderList)
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder)
        progressBarLayout = findViewById(R.id.progressBarLayout)

        setSupportActionBar(toolbar)
        supportActionBar?.title = resName
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        layoutManagerCart = LinearLayoutManager(this@CartActivity)

        cartAdapter = CartAdapter(this@CartActivity, listCart)
        rvCart.adapter = cartAdapter
        rvCart.layoutManager = layoutManagerCart

        sharedPreferences = getSharedPreferences(
            getString(R.string.ZestySharedPreferences),
            MODE_PRIVATE
        )

        getCartData()

        btnPlaceOrder.setOnClickListener {
            connectAPI()
            btnPlaceOrder.visibility = View.INVISIBLE
        }
    }

    fun connectAPI() {

        progressBarLayout.visibility = View.VISIBLE
        if (ConnectivityManager().checkConnectivity(this@CartActivity)) {
            val queue = Volley.newRequestQueue(this@CartActivity)

            val url = "http://13.235.250.119/v2/place_order/fetch_result/"

            val jsonParams = JSONObject()
            jsonParams.put("user_id", sharedPreferences.getString("ID", ""))
            jsonParams.put("restaurant_id", resId)
            jsonParams.put("total_cost", totalAmount)

            val foodArray = JSONArray()

            for (foodItem in selectedIds) {
                val foodId = JSONObject()
                foodId.put("food_item_id", foodItem)
                foodArray.put(foodId)
            }
            jsonParams.put("food", foodArray)

            val jsonObjectRequest = object : JsonObjectRequest(
                Method.POST, url, jsonParams,
                Response.Listener {
                    try {
                        val jsonObject = it.getJSONObject("data")

                        if (jsonObject.getBoolean("success")) {
                            startActivity(Intent(this@CartActivity, OrderPlaced::class.java))
                            finishAffinity()
                        } else {
                            Toast.makeText(
                                this@CartActivity,
                                "Something went Wrong",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (ex: JSONException) {
                        Toast.makeText(
                            this@CartActivity,
                            "Some Error Occurred",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    progressBarLayout.visibility = View.INVISIBLE
                }, Response.ErrorListener {

                    Toast.makeText(this@CartActivity, "Unable to fetch Data", Toast.LENGTH_SHORT)
                        .show()
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>() //Hashmap is derived from mutrablemap
                    headers["Content-type"] =
                        "application/json"
                    headers["token"] = "0aee2c7976e28b"
                    return headers
                }
            }
            queue.add(jsonObjectRequest)
        } else {

            val dialog = AlertDialog.Builder(this@CartActivity)
            dialog.setTitle("No Internet")
            dialog.setMessage("Please check your Internet Connection")
            dialog.setPositiveButton("Open Settings") { text, listener ->

                val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(intent)
            }
            dialog.setNegativeButton("Exit") { text, listener ->
                ActivityCompat.finishAffinity(this@CartActivity)
            }

            dialog.create()
            dialog.show()
        }
    }

    fun getCartData() {

        if (ConnectivityManager().checkConnectivity(this)) {

            progressBarLayout.visibility = View.VISIBLE

            try {
                val queue = Volley.newRequestQueue(this)
                val url = "http://13.235.250.119/v2/restaurants/fetch_result/$resId"

                val jsonObjectRequest = object : JsonObjectRequest(
                    Method.GET,
                    url,
                    null,
                    Response.Listener {

                        val jsonObject = it.getJSONObject("data")

                        if (jsonObject.getBoolean("success")) {

                            val data = jsonObject.getJSONArray("data")
                            cartListItems.clear()
                            totalAmount = 0

                            for (i in 0 until data.length()) {
                                val cartItem = data.getJSONObject(i)
                                if (selectedIds.contains(cartItem.getString("id"))) {
                                    val menuObject = FoodItem(
                                        cartItem.getString("id"),
                                        cartItem.getString("name"),
                                        cartItem.getString("cost_for_one"),
                                        cartItem.getString("restaurant_id")
                                    )

                                    totalAmount += cartItem.getString("cost_for_one").toInt()
                                    btnPlaceOrder.text =
                                        "Place Order (Total = Rs. ${totalAmount.toString()})"
                                    cartListItems.add(menuObject)

                                }
                                cartAdapter = CartAdapter(this, cartListItems)
                                rvCart.adapter = cartAdapter
                                rvCart.layoutManager = layoutManagerCart
                            }
                        } else {
                            Toast.makeText(
                                this@CartActivity,
                                "Something went Wrong",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        progressBarLayout.visibility = View.INVISIBLE
                    },
                    Response.ErrorListener {

                        Toast.makeText(
                            this,
                            "Some Error occurred!!!",
                            Toast.LENGTH_SHORT
                        ).show()

                        progressBarLayout.visibility = View.INVISIBLE
                    }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        headers["token"] = "0aee2c7976e28b"
                        return headers
                    }
                }

                queue.add(jsonObjectRequest)

            } catch (e: JSONException) {
                Toast.makeText(
                    this,
                    "Some Unexpected error occurred!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }

        } else {

            val alterDialog = androidx.appcompat.app.AlertDialog.Builder(this)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Check Internet Connection!")
            alterDialog.setPositiveButton("Open Settings") { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(settingsIntent)
            }
            alterDialog.setNegativeButton("Exit") { _, _ ->
                finishAffinity()
            }
            alterDialog.setCancelable(false)
            alterDialog.create()
            alterDialog.show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}