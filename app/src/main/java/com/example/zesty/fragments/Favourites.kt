package com.example.zesty.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.zesty.R
import com.example.zesty.adapters.RestaurantAdapter
import com.example.zesty.database.RestaurantDatabase
import com.example.zesty.database.RestaurantEntity
import com.example.zesty.dataclass.RestaurantData
import com.example.zesty.util.ConnectivityManager
import org.json.JSONException

class Favourites : Fragment() {

    lateinit var recyclerView: RecyclerView
    lateinit var progressBarLayout: RelativeLayout
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var noFavLayout: RelativeLayout
    var restaurantList = arrayListOf<RestaurantData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_favourites, container, false)
        recyclerView = view.findViewById(R.id.rvFavFoodList)
        progressBarLayout = view.findViewById(R.id.progressBarLayout)
        noFavLayout = view.findViewById(R.id.noFavLayout)
        layoutManager = LinearLayoutManager(context)

        connectAPI()

        return view
    }

    fun connectAPI() {

        if (ConnectivityManager().checkConnectivity(activity as Context)) {
            val queue = Volley.newRequestQueue(activity as Context)
            val url = "http://13.235.250.119/v2/restaurants/fetch_result/"
            val jsonObjectRequest = object : JsonObjectRequest(
                Method.GET, url, null,
                Response.Listener {
                    try {
                        val jsonObject = it.getJSONObject("data")

                        if (jsonObject.getBoolean("success")) {

                            restaurantList.clear()

                            val data = jsonObject.getJSONArray("data")

                            for (i in 0 until data.length()) {

                                val resJsonObject = data.getJSONObject(i)

                                val restaurantEntity = RestaurantEntity(
                                    resJsonObject.getString("id"),
                                    resJsonObject.getString("name")
                                )

                                val isResFav = DBFavRestaurants(
                                    activity as Context,
                                    restaurantEntity
                                ).execute().get()

                                if (isResFav) {
                                    val resObject = RestaurantData(
                                        resJsonObject.getString("id"),
                                        resJsonObject.getString("name"),
                                        resJsonObject.getString("rating"),
                                        resJsonObject.getString("cost_for_one"),
                                        resJsonObject.getString("image_url")
                                    )
                                    restaurantList.add(resObject)
                                    recyclerView.layoutManager = layoutManager
                                    recyclerView.adapter =
                                        RestaurantAdapter(activity as Context, restaurantList)
                                }
                            }

                            if (restaurantList.isEmpty()) {
                                noFavLayout.visibility = View.VISIBLE
                                progressBarLayout.visibility = View.GONE
                                recyclerView.visibility = View.GONE
                            } else {
                                progressBarLayout.visibility = View.GONE
                                recyclerView.visibility = View.VISIBLE
                                noFavLayout.visibility = View.GONE
                            }

                        } else {
                            Toast.makeText(
                                activity as Context,
                                "Something went Wrong",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (ex: JSONException) {
                        Toast.makeText(context, "Some Error Occurred", Toast.LENGTH_SHORT).show()
                    }
                }, Response.ErrorListener {

                    if (activity != null) {
                        Toast.makeText(context, "Unable to fetch Data", Toast.LENGTH_SHORT).show()
                    }
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

            val dialog = AlertDialog.Builder(activity as Context)
            dialog.setTitle("No Internet")
            dialog.setMessage("Please check your Internet Connection")
            dialog.setPositiveButton("Open Settings") { text, listener ->

                val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(intent)
            }
            dialog.setNegativeButton("Exit") { text, listener ->
                ActivityCompat.finishAffinity(activity as Activity)
            }

            dialog.create()
            dialog.show()
        }
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