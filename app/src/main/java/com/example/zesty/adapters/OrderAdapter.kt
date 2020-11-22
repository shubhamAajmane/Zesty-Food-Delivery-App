package com.example.zesty.adapters

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.zesty.R
import com.example.zesty.dataclass.FoodItem
import com.example.zesty.dataclass.OrderData
import com.example.zesty.util.ConnectivityManager
import org.json.JSONException

class OrderAdapter(
    val context: Context,
    val orderList: ArrayList<OrderData>,
    val userId: String
) : RecyclerView.Adapter<OrderAdapter.OrderHolder>() {

    class OrderHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvResName: TextView = itemView.findViewById(R.id.tvResName)
        val tvOrderDate: TextView = itemView.findViewById(R.id.tvOrderDate)
        val rvFoodDetails: RecyclerView = itemView.findViewById(R.id.rvFoodDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHolder {

        val itemView =
            LayoutInflater.from(context).inflate(R.layout.order_history_item, parent, false)
        return OrderHolder(itemView)
    }

    override fun getItemCount(): Int = orderList.size

    override fun onBindViewHolder(holder: OrderHolder, position: Int) {

        holder.tvResName.text = orderList[position].restaurantName
        holder.tvOrderDate.text = orderList[position].orderPlaced.substring(0, 8)

        connectAPI(userId, position, holder.rvFoodDetails)
    }

    fun connectAPI(userId: String, position: Int, rvFoodDetails: RecyclerView) {

        if (ConnectivityManager().checkConnectivity(context)) {

            val orderedItems = ArrayList<FoodItem>()

            val queue = Volley.newRequestQueue(context)
            val url = "http://13.235.250.119/v2/orders/fetch_result/$userId"
            val jsonObjectRequest = object : JsonObjectRequest(
                Method.GET, url, null,
                Response.Listener {
                    try {
                        val jsonObject = it.getJSONObject("data")

                        if (jsonObject.getBoolean("success")) {

                            val data = jsonObject.getJSONArray("data")

                            val restaurantObject = data.getJSONObject(position)

                            orderedItems.clear()

                            val foodOrdered = restaurantObject.getJSONArray("food_items")

                            for (i in 0 until foodOrdered.length()) {

                                val orderHistoryObject = foodOrdered.getJSONObject(i)

                                val menuItem = FoodItem(
                                    orderHistoryObject.getString("food_item_id"),
                                    orderHistoryObject.getString("name"),
                                    orderHistoryObject.getString("cost"),
                                    ""
                                )
                                orderedItems.add(menuItem)

                                rvFoodDetails.adapter = CartAdapter(context, orderedItems)
                                rvFoodDetails.layoutManager = LinearLayoutManager(context)
                            }

                        } else {
                            Toast.makeText(
                                context,
                                "Something went Wrong",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (ex: JSONException) {
                        Toast.makeText(
                            context,
                            "Some Error Occurred",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }, Response.ErrorListener {

                    Toast.makeText(context, "Unable to fetch Data", Toast.LENGTH_SHORT)
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

            val dialog = AlertDialog.Builder(context)
            dialog.setTitle("No Internet")
            dialog.setMessage("Please check your Internet Connection")
            dialog.setPositiveButton("Open Settings") { text, listener ->

                val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                context.startActivity(intent)
            }
            dialog.setNegativeButton("Exit") { text, listener ->

            }

            dialog.create()
            dialog.show()
        }
    }

}
