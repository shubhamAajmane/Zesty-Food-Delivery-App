package com.example.zesty.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.zesty.R

class Profile : Fragment() {

    lateinit var tvName: TextView
    lateinit var tvMobile: TextView
    lateinit var tvEmailId: TextView
    lateinit var tvAddress: TextView

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_profile, container, false)

        tvName = view.findViewById(R.id.tvUserName)
        tvMobile = view.findViewById(R.id.tvUserNo)
        tvEmailId = view.findViewById(R.id.tvUserMailId)
        tvAddress = view.findViewById(R.id.tvUserAddress)

        sharedPreferences = context!!.getSharedPreferences(
            getString(R.string.ZestySharedPreferences),
            Context.MODE_PRIVATE
        )

        tvName.text = sharedPreferences.getString("Name", "")
        tvMobile.text = sharedPreferences.getString("Mobile", "")
        tvEmailId.text = sharedPreferences.getString("Email", "")
        tvAddress.text = sharedPreferences.getString("Address", "")

        return view
    }

}