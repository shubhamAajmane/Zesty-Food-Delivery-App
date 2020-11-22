package com.example.zesty.fragments

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.zesty.R
import com.example.zesty.adapters.OrderAdapter
import com.example.zesty.util.ConnectivityManager
import com.example.zesty.dataclass.OrderData
import org.json.JSONException

class OrderHistory : Fragment() {

    lateinit var rvOrderHistory: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var orderAdapter: OrderAdapter
    lateinit var progressBarLayout:RelativeLayout
    lateinit var emptyCart:RelativeLayout
    var orderList = arrayListOf<OrderData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: View = inflater.inflate(R.layout.fragment_orders, container, false)
        rvOrderHistory = view.findViewById(R.id.rvOrderHistory)
        layoutManager = LinearLayoutManager(context)
        progressBarLayout = view.findViewById(R.id.progressBarLayout)
        emptyCart = view.findViewById(R.id.emptyCart)

        val sharedPreferences =
            context?.getSharedPreferences(getString(R.string.ZestySharedPreferences), MODE_PRIVATE)

        val userId = sharedPreferences?.getString("ID", "")

        if (userId != null) {
            connectAPI(userId)
        }
        return view
    }

    fun connectAPI(userId: String) {

        if (ConnectivityManager().checkConnectivity(activity as Context)) {
            val queue = Volley.newRequestQueue(context)
            val url = "http://13.235.250.119/v2/orders/fetch_result/$userId"
            val jsonObjectRequest = object : JsonObjectRequest(
                Method.GET, url, null,
                Response.Listener {
                    try {
                        val jsonObject = it.getJSONObject("data")

                        if (jsonObject.getBoolean("success")) {

                            val data = jsonObject.getJSONArray("data")

                            for (i in 0 until data.length()) {

                                val orderHistoryObject = data.getJSONObject(i)

                                val orderObject = OrderData(
                                    orderHistoryObject.getString("order_id"),
                                    orderHistoryObject.getString("restaurant_name"),
                                    orderHistoryObject.getString("total_cost"),
                                    orderHistoryObject.getString("order_placed_at")
                                )
                                orderList.add(orderObject)

                                orderAdapter =
                                    OrderAdapter(activity as Context, orderList, userId)
                                rvOrderHistory.layoutManager = layoutManager
                                rvOrderHistory.adapter = orderAdapter
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

                    if(orderList.isEmpty()) {
                        progressBarLayout.visibility = View.INVISIBLE
                        emptyCart.visibility = View.VISIBLE
                    }
                    else {
                        progressBarLayout.visibility = View.INVISIBLE
                        emptyCart.visibility = View.INVISIBLE
                    }

                }, Response.ErrorListener {

                    Toast.makeText(context, "Unable to fetch Data", Toast.LENGTH_SHORT)
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

            val dialog = AlertDialog.Builder(activity as Context)
            dialog.setTitle("No Internet")
            dialog.setMessage("Please check your Internet Connection")
            dialog.setPositiveButton("Open Settings") { text, listener ->

                val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(intent)
            }
            dialog.setNegativeButton("Exit") { text, listener ->

            }
            dialog.create()
            dialog.show()
        }
    }
}