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

class ResetActivity : AppCompatActivity() {

    lateinit var etOTP: EditText
    lateinit var etPassword: EditText
    lateinit var etConfirmPass: EditText
    lateinit var btnSubmit: Button
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset)

        etOTP = findViewById(R.id.etOTP)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPass = findViewById(R.id.etConfirmPass)
        btnSubmit = findViewById(R.id.btnSubmit)
        sharedPreferences = getSharedPreferences(
            getString(R.string.ZestySharedPreferences),
            MODE_PRIVATE
        )

        btnSubmit.setOnClickListener {

            if (etOTP.text.isEmpty() || etPassword.text.isEmpty() || etConfirmPass.text.isEmpty()) {
                Toast.makeText(
                    this@ResetActivity,
                    "Please enter all credentials",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (etPassword.text.toString() != etConfirmPass.text.toString()) {
                Toast.makeText(this@ResetActivity, "Password doesn't match", Toast.LENGTH_SHORT)
                    .show()
            } else {

                val mobileNumber = sharedPreferences.getString("Mobile", "")

                if (mobileNumber != null) {
                    connectAPI(mobileNumber)
                }

            }
        }
    }

    fun connectAPI(mobileNumber: String) {

        if (ConnectivityManager().checkConnectivity(this@ResetActivity)) {
            val queue = Volley.newRequestQueue(this@ResetActivity)

            val url = "http://13.235.250.119/v2/reset_password/fetch_result"

            val jsonParams = JSONObject()
            jsonParams.put("mobile_number", mobileNumber)
            jsonParams.put("password", etPassword.text.toString())
            jsonParams.put("otp", etOTP.text.toString())

            val jsonObjectRequest = object : JsonObjectRequest(
                Method.POST, url, jsonParams,
                Response.Listener {

                    try {
                        val jsonObject = it.getJSONObject("data")

                        if (jsonObject.getBoolean("success")) {
                            Toast.makeText(
                                this@ResetActivity,
                                jsonObject.getString("successMessage"),
                                Toast.LENGTH_SHORT
                            ).show()
                            startActivity(Intent(this@ResetActivity, LoginActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(
                                this@ResetActivity,
                                jsonObject.getString("errorMessage"),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (ex: JSONException) {
                        Toast.makeText(
                            this@ResetActivity,
                            "Something went wrong, Please try again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }, Response.ErrorListener {

                    if (applicationContext != null) {
                        Toast.makeText(
                            this@ResetActivity,
                            "Something went wrong, Please try again",
                            Toast.LENGTH_SHORT
                        ).show()
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

            val dialog = AlertDialog.Builder(this@ResetActivity)
            dialog.setTitle("No Internet")
            dialog.setMessage("Please check your Internet Connection")
            dialog.setPositiveButton("Open Settings") { text, listener ->

                val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(intent)
            }
            dialog.setNegativeButton("Exit") { text, listener ->
                ActivityCompat.finishAffinity(this@ResetActivity)
            }
            dialog.create()
            dialog.show()
        }
    }
}