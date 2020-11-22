package com.example.zesty.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.zesty.R
import com.example.zesty.util.ConnectivityManager
import org.json.JSONException
import org.json.JSONObject

class SignupActivity : AppCompatActivity() {

    lateinit var etName: EditText
    lateinit var etEmailAddress: EditText
    lateinit var etMobileNumber: EditText
    lateinit var etDeliveryAddress: EditText
    lateinit var etPassword: EditText
    lateinit var btnSignup: Button
    lateinit var etConfirmPass: EditText
    lateinit var toolbar: Toolbar
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        etName = findViewById(R.id.etName)
        etEmailAddress = findViewById(R.id.etEmailAddress)
        etMobileNumber = findViewById(R.id.etMobileNumber)
        etDeliveryAddress = findViewById(R.id.etAddress)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPass = findViewById(R.id.etConfirmPass)
        btnSignup = findViewById(R.id.btnSignUp)
        toolbar = findViewById(R.id.toolbarLayout)
        sharedPreferences =
            getSharedPreferences(getString(R.string.ZestySharedPreferences), Context.MODE_PRIVATE)

        setSupportActionBar(toolbar)
        supportActionBar?.title = "Register Yourself"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        btnSignup.setOnClickListener {

            if (etName.text.isEmpty() || etMobileNumber.text.isEmpty() || etEmailAddress.text.isEmpty() || etPassword.text.isEmpty() || etConfirmPass.text.isEmpty() || etDeliveryAddress.text.isEmpty()) {
                Toast.makeText(
                    applicationContext,
                    "Please Enter all the credentials",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (etPassword.text.toString() != etConfirmPass.text.toString()) {
                Toast.makeText(this@SignupActivity, "Password doesn't match", Toast.LENGTH_SHORT)
                    .show()
            } else {
                connectAPI(etPassword.text.toString())
            }
        }
    }

    fun connectAPI(password: String) {

        if (ConnectivityManager().checkConnectivity(this@SignupActivity)) {
            val queue = Volley.newRequestQueue(this@SignupActivity)

            val url = "http://13.235.250.119/v2/register/fetch_result"

            val jsonParams = JSONObject()
            jsonParams.put("name", etName.text.toString())
            jsonParams.put("mobile_number", etMobileNumber.text.toString())
            jsonParams.put("password", password)
            jsonParams.put("address", etDeliveryAddress.text.toString())
            jsonParams.put("email", etEmailAddress.text.toString())

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

                            startActivity(Intent(this@SignupActivity, MainActivity::class.java))
                            finish()

                        } else {
                            Toast.makeText(
                                this@SignupActivity,
                                jsonObject.getString("errorMessage"),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (ex: JSONException) {
                        Toast.makeText(
                            this@SignupActivity,
                            "Something went wrong, Please try again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }, Response.ErrorListener {

                    if (applicationContext != null) {
                        Toast.makeText(
                            this@SignupActivity,
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

            val dialog = AlertDialog.Builder(this@SignupActivity)
            dialog.setTitle("No Internet")
            dialog.setMessage("Please check your Internet Connection")
            dialog.setPositiveButton("Open Settings") { text, listener ->

                val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(intent)
            }
            dialog.setNegativeButton("Exit") { text, listener ->
                ActivityCompat.finishAffinity(this@SignupActivity)
            }
            dialog.create()
            dialog.show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}