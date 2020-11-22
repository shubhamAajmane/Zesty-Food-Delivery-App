package com.example.zesty.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.zesty.dataclass.RestaurantData
import com.example.zesty.adapters.RestaurantAdapter
import com.example.zesty.R
import com.example.zesty.util.ConnectivityManager
import kotlinx.android.synthetic.main.sort_radio_buttons.view.*
import org.json.JSONException
import java.util.*
import kotlin.collections.HashMap

class Restaurants : Fragment() {

    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var resAdapter:RestaurantAdapter
    lateinit var radioButtonView: View
    val restaurantList = arrayListOf<RestaurantData>()
    lateinit var progressBarLayout: RelativeLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_restaurant, container, false)
        recyclerView = view.findViewById(R.id.rvFoodList)
        layoutManager = LinearLayoutManager(activity as Context)
        progressBarLayout = view.findViewById(R.id.progressBarLayout)
        setHasOptionsMenu(true)
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
                    progressBarLayout.visibility = View.GONE

                    try {
                        val jsonObject = it.getJSONObject("data")

                        if (jsonObject.getBoolean("success")) {

                            val data = jsonObject.getJSONArray("data")

                            for (i in 0 until data.length()) {

                                val foodJsonObject = data.getJSONObject(i)

                                val foodObject = RestaurantData(
                                    foodJsonObject.getString("id"),
                                    foodJsonObject.getString("name"),
                                    foodJsonObject.getString("rating"),
                                    foodJsonObject.getString("cost_for_one"),
                                    foodJsonObject.getString("image_url")
                                )
                                restaurantList.add(foodObject)
                                recyclerView.layoutManager = layoutManager
                                resAdapter = RestaurantAdapter(activity as Context, restaurantList)
                                recyclerView.adapter = resAdapter
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.restaurants_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.menuSort -> {
                radioButtonView = View.inflate(
                    context,
                    R.layout.sort_radio_buttons,
                    null
                )
                AlertDialog.Builder(activity as Context)
                    .setTitle("Sort By?")
                    .setView(radioButtonView)
                    .setPositiveButton("OK") { _, _ ->
                        if (radioButtonView.highToLow.isChecked) {
                            Collections.sort(restaurantList,costComparator)
                            restaurantList.reverse()
                            resAdapter.notifyDataSetChanged()
                        }
                        if (radioButtonView.lowToHigh.isChecked) {
                            Collections.sort(restaurantList, costComparator)
                            resAdapter.notifyDataSetChanged()
                        }
                        if (radioButtonView.rating.isChecked) {
                            Collections.sort(restaurantList, ratingComparator)
                            restaurantList.reverse()
                            resAdapter.notifyDataSetChanged()
                        }
                    }
                    .setNegativeButton("Cancel") { _, _ ->

                    }
                    .create()
                    .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    var ratingComparator = Comparator<RestaurantData> { rest1, rest2 ->

        if (rest1.rating.compareTo(rest2.rating, true) == 0) {
            rest1.name.compareTo(rest2.name, true)
        } else {
            rest1.rating.compareTo(rest2.rating, true)
        }
    }

    var costComparator = Comparator<RestaurantData> { rest1, rest2 ->

        rest1.cost.compareTo(rest2.cost, true)
    }
}