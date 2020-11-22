package com.example.zesty.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.zesty.R
import com.example.zesty.fragments.*
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    lateinit var toolbar: androidx.appcompat.widget.Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationView: NavigationView
    lateinit var drawerHeader: View
    lateinit var tvHeaderTitle: TextView
    lateinit var tvHeaderSubTitle:TextView
    lateinit var sharedPreferences: SharedPreferences
    var previousItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.navigationToolbar)
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        drawerHeader = navigationView.getHeaderView(0)
        tvHeaderTitle = drawerHeader.findViewById(R.id.tvHeaderTitle)
        tvHeaderSubTitle = drawerHeader.findViewById(R.id.tvHeaderSubTitle)
        sharedPreferences =
            getSharedPreferences(getString(R.string.ZestySharedPreferences), Context.MODE_PRIVATE)

        tvHeaderTitle.text = sharedPreferences.getString("Name", "")
        tvHeaderSubTitle.text = getString(R.string.number_code)+sharedPreferences.getString("Mobile","")

        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val actionBarDrawerToggle = ActionBarDrawerToggle(
            this@MainActivity,
            drawerLayout,
            R.string.open_drawer,
            R.string.close_drawer
        )
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        navigationView.setNavigationItemSelectedListener {

            if (previousItem != null) {
                previousItem?.isCheckable = false
            }
            it.isCheckable = true
            it.isChecked = true
            previousItem = it

            when (it.itemId) {

                R.id.menuHome -> {
                    supportFragmentManager.beginTransaction().replace(R.id.frameLayout, Restaurants()).commit()
                    drawerLayout.closeDrawers()
                    supportActionBar?.title = "All Restaurants"
                }
                R.id.menuProfile -> {
                    supportFragmentManager.beginTransaction().replace(R.id.frameLayout, Profile())
                        .commit()
                    drawerLayout.closeDrawers()
                    supportActionBar?.title = "Profile"
                }

                R.id.menuFavourites -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, Favourites()).commit()
                    drawerLayout.closeDrawers()
                    supportActionBar?.title = "Favourites"
                }

                R.id.menuOrders -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, OrderHistory()).commit()
                    drawerLayout.closeDrawers()
                    supportActionBar?.title = "Order History"
                }

                R.id.menuFaqs -> {
                    supportFragmentManager.beginTransaction().replace(R.id.frameLayout, Faq())
                        .commit()
                    drawerLayout.closeDrawers()
                    supportActionBar?.title = "Frequently Asked Questions"
                }

                R.id.menuLogout -> {
                    val dialog = AlertDialog.Builder(this@MainActivity)
                    dialog.setTitle("Confirmation")
                    dialog.setMessage("Are you sure you want to exit?")
                    dialog.setPositiveButton("Yes") { text, listener ->
                        sharedPreferences.edit().clear().apply()
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        finish()
                    }
                    dialog.setNegativeButton("No") { text, listener ->
                        openDefaultFrag()
                    }
                    dialog.create()
                    dialog.show()
                }
            }
            return@setNavigationItemSelectedListener true
        }
        navigationView.setCheckedItem(R.id.menuHome)
        openDefaultFrag()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return if (item.itemId == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START)
            true
        } else {
            false
        }
    }

    fun openDefaultFrag() {
        supportFragmentManager.beginTransaction().replace(R.id.frameLayout, Restaurants()).commit()
        drawerLayout.closeDrawers()
        navigationView.setCheckedItem(R.id.menuHome)
        supportActionBar?.title = "All Restaurants"
    }

    override fun onBackPressed() {

        val fragment = supportFragmentManager.findFragmentById(R.id.frameLayout)

        when (fragment) {
            !is Restaurants -> openDefaultFrag()

            else -> finishAffinity()
        }
    }
}