package com.example.zesty.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.EditText
import android.widget.TextView
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

class LoginActivity : AppCompatActivity() {

    lateinit var tvSignUp: TextView
    lateinit var tvForgotPass: TextView
    lateinit var btnLogin: TextView
    lateinit var etMobileNumber: EditText
    lateinit var etPassword: EditText
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        tvSignUp = findViewById(R.id.tvSignupNow)
        tvForgotPass = findViewById(R.id.tvForgetPassword)
        btnLogin = findViewById(R.id.btnLogin)
        etMobileNumber = findViewById(R.id.etMobileNumber)
        etPassword = findViewById(R.id.etPassword)

        sharedPreferences =
            getSharedPreferences(getString(R.string.ZestySharedPreferences), Context.MODE_PRIVATE)

        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        }

        tvSignUp.setOnClickListener {

            startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
        }

        tvForgotPass.setOnClickListener {

            startActivity(Intent(this@LoginActivity, ForgetActivity::class.java))
        }

        btnLogin.setOnClickListener {

            if (etMobileNumber.text.isEmpty() || etPassword.text.isEmpty()) {
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

        if (ConnectivityManager().checkConnectivity(this@LoginActivity)) {
            val queue = Volley.newRequestQueue(this@LoginActivity)

            val url = "http://13.235.250.119/v2/login/fetch_result/"

            val jsonParams = JSONObject()
            jsonParams.put("mobile_number", etMobileNumber.text.toString())
            jsonParams.put("password", etPassword.text.toString())

            val jsonObjectRequest = object : JsonObjectRequest(
                Method.POST, url, jsonParams,
                Response.Listener {

                    try {
                        val jsonObject = it.getJSONObject("data")

                        if (jsonObject.getBoolean("success")) {

                            val userData = jsonObject.getJSONObject("data")

                            val userId = userData.getString("user_id")
                            val userName = userData.getString("name")
                            val userEmail = userData.getString("email")
                            val userMobile = userData.getString("mobile_number")
                            val userAddress = userData.getString("address")

                            sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
                            sharedPreferences.edit().putString("ID", userId).apply()
                            sharedPreferences.edit().putString("Name", userName).apply()
                            sharedPreferences.edit().putString("Email", userEmail).apply()
                            sharedPreferences.edit().putString("Mobile", userMobile).apply()
                            sharedPreferences.edit().putString("Address", userAddress).apply()

                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()

                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                jsonObject.getString("errorMessage"),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (ex: JSONException) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Something went wrong, Please try again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }, Response.ErrorListener {

                    if (applicationContext != null) {
                        Toast.makeText(
                            this@LoginActivity,
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

            val dialog = AlertDialog.Builder(this@LoginActivity)
            dialog.setTitle("No Internet")
            dialog.setMessage("Please check your Internet Connection")
            dialog.setPositiveButton("Open Settings") { text, listener ->

                val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(intent)
            }
            dialog.setNegativeButton("Exit") { text, listener ->
                ActivityCompat.finishAffinity(this@LoginActivity)
            }
            dialog.create()
            dialog.show()
        }
    }

    override fun onBackPressed() {
        finishAffinity()
    }
}