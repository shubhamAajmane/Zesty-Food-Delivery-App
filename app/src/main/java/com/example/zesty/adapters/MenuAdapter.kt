package com.example.zesty.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zesty.R
import com.example.zesty.activities.CartActivity
import com.example.zesty.dataclass.MenuItem

class MenuAdapter(
    val context: Context,
    val resId:String,
    val resName:String,
    val menuList: ArrayList<MenuItem>,
    val btnProceedCart: Button,
    val rvMenuItem:RecyclerView
) :
    RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    var idsOfSelectedItems = arrayListOf<String>()
    var noOfItemsSelected:Int = 0

    class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFoodName: TextView = itemView.findViewById(R.id.tvFoodName)
        val tvFoodPrice: TextView = itemView.findViewById(R.id.tvFoodPrice)
        val tvItemNo: TextView = itemView.findViewById(R.id.tvItemNo)
        val btnAdd: Button = itemView.findViewById(R.id.btnAddToCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.menu_item, parent, false)
        return MenuViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {

        btnProceedCart.setOnClickListener {

            val intent = Intent(context,CartActivity::class.java)
            intent.putExtra("resId",resId)
            intent.putExtra("resName",resName)
            intent.putExtra("idsOfSelectedItems",idsOfSelectedItems)
            context.startActivity(intent)
            btnProceedCart.visibility = View.INVISIBLE
        }
        holder.btnAdd.setOnClickListener {

            if(holder.btnAdd.text.toString().equals("Remove")) {
                noOfItemsSelected--
                idsOfSelectedItems.remove(holder.btnAdd.tag.toString())

                holder.btnAdd.text = "Add"

                holder.btnAdd.setBackgroundColor(context.resources.getColor(R.color.colorPrimary))
            }
            else {
                noOfItemsSelected++
                idsOfSelectedItems.add(holder.btnAdd.tag.toString())

                holder.btnAdd.text = "Remove"

                holder.btnAdd.setBackgroundColor(context.resources.getColor(R.color.colorAccent))

            }
            if(noOfItemsSelected>0) {
                btnProceedCart.visibility = View.VISIBLE
                rvMenuItem.setPadding(0,0,0,100)
            }
            else {
                btnProceedCart.visibility = View.INVISIBLE
                rvMenuItem.setPadding(0,0,0,0)
            }
        }

        holder.tvFoodName.text = menuList[position].name
        holder.tvFoodPrice.text = "Rs." + menuList[position].cost
        holder.tvItemNo.text = (position + 1).toString()
        holder.btnAdd.tag = menuList[position].id
    }

    override fun getItemCount() = menuList.size
}