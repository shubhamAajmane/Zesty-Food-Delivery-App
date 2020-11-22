package com.example.zesty.activities

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.zesty.R
import com.example.zesty.util.ConnectivityManager
import org.json.JSONException
import org.json.JSONObject

class ForgetActivity : AppCompatActivity() {

    lateinit var etMobileNumber: EditText
    lateinit var etEmailAddress: EditText
    lateinit var btnNext: Button
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget)

        etMobileNumber = findViewById(R.id.etMobileNumber)
        etEmailAddress = findViewById(R.id.etEmailAddress)
        btnNext = findViewById(R.id.btnNext)

        sharedPreferences = getSharedPreferences(
            getString(R.string.ZestySharedPreferences),
            MODE_PRIVATE
        )

        btnNext.setOnClickListener {

            if (etMobileNumber.text.isEmpty() || etEmailAddress.text.isEmpty()) {
                Toast.makeText(
                    applicationContext,
                    "Please Enter all the credentials",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                connectAPI()
            }
        }
    }

    fun connectAPI() {

        if (ConnectivityManager().checkConnectivity(this@ForgetActivity)) {
            val queue = Volley.newRequestQueue(this@ForgetActivity)

            val url = "http://13.235.250.119/v2/forgot_password/fetch_result"

            val jsonParams = JSONObject()
            jsonParams.put("mobile_number", etMobileNumber.text.toString())
            jsonParams.put("email", etEmailAddress.text.toString())

            val jsonObjectRequest = object : JsonObjectRequest(
                Method.POST, url, jsonParams,
                Response.Listener {

                    try {
                        val jsonObject = it.getJSONObject("data")

                        if (jsonObject.getBoolean("success")) {

                            val firstTry = jsonObject.getBoolean("first_try")

                            sharedPreferences.edit().putString("Mobile",etMobileNumber.text.toString()).apply()

                            if (firstTry) {
                                startActivity(Intent(this@ForgetActivity,ResetActivity::class.java))
                            } else {
                                startActivity(Intent(this@ForgetActivity,ResetActivity::class.java))
                                Toast.makeText(
                                    this@ForgetActivity,
                                    "OTP already sent to registered email address",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                this@ForgetActivity,
                                jsonObject.getString("errorMessage"),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (ex: JSONException) {
                        Toast.makeText(
                            this@ForgetActivity,
                            "Something went wrong, Please try again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }, Response.ErrorListener {

                    if (applicationContext != null) {
                        Toast.makeText(
                            this@ForgetActivity,
                            "Something went wrong, Please try again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
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

            val dialog = AlertDialog.Builder(this@ForgetActivity)
            dialog.setTitle("No Internet")
            dialog.setMessage("Please check your Internet Connection")
            dialog.setPositiveButton("Open Settings") { text, listener ->

                val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(intent)
            }
            dialog.setNegativeButton("Exit") { text, listener ->
                ActivityCompat.finishAffinity(this@ForgetActivity)
            }
            dialog.create()
            dialog.show()
        }
    }
}