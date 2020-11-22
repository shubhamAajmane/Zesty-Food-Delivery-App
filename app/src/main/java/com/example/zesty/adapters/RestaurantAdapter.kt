package com.example.zesty.adapters

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.zesty.dataclass.RestaurantData
import com.example.zesty.R
import com.example.zesty.activities.RestaurantMenu
import com.example.zesty.database.RestaurantDatabase
import com.example.zesty.database.RestaurantEntity
import com.squareup.picasso.Picasso

class RestaurantAdapter(val context: Context, val restaurantDataList: ArrayList<RestaurantData>) :
    RecyclerView.Adapter<RestaurantAdapter.FoodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.restaurant_item, parent, false)

        return FoodViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.tvTitle.text = restaurantDataList[position].name
        holder.tvRating.text = restaurantDataList[position].rating
        holder.tvPrice.text = restaurantDataList[position].cost + "/person"
        Picasso.get().load(restaurantDataList[position].image).error(R.drawable.broken_image)
            .into(holder.ivImage)

        val foodEntity = RestaurantEntity(
            restaurantDataList[position].id,
            restaurantDataList[position].name
        )

        val isFav = DBAsyncTask(context, foodEntity, 1).execute().get()

        if (isFav) {
            holder.ivFav.setImageResource(R.drawable.ic_favselect)
        } else {
            holder.ivFav.setImageResource(R.drawable.ic_favfood)
        }

        holder.ivFav.setOnClickListener {

            val isfav = DBAsyncTask(context, foodEntity, 1).execute().get()

            if (isfav) {
                holder.ivFav.setImageResource(R.drawable.ic_favfood)
                DBAsyncTask(context, foodEntity, 3).execute().get()
                Toast.makeText(
                    context,
                    "${holder.tvTitle.text} Removed from Favourites",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                DBAsyncTask(context, foodEntity, 2).execute().get()
                holder.ivFav.setImageResource(R.drawable.ic_favselect)
                Toast.makeText(
                    context,
                    "${holder.tvTitle.text} added to Favourites",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        holder.restaurantItem.setOnClickListener {
            val intent = Intent(context, RestaurantMenu::class.java)
            intent.putExtra("resId", restaurantDataList[position].id)
            intent.putExtra("resName", restaurantDataList[position].name)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return restaurantDataList.size
    }

    class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvTitle: TextView = itemView.findViewById(R.id.tvFoodTitle)
        val tvRating: TextView = itemView.findViewById(R.id.tvFoodRating)
        val tvPrice: TextView = itemView.findViewById(R.id.tvFoodPrice)
        val ivImage: ImageView = itemView.findViewById(R.id.ivFoodImage)
        val ivFav: ImageView = itemView.findViewById(R.id.ivFavIcon)
        val restaurantItem: CardView = itemView.findViewById(R.id.cvRestaurantItem)
    }

    class DBAsyncTask(val context: Context, val restaurantEntity: RestaurantEntity, val mode: Int) :
        AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg params: Void?): Boolean {

            val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurants_db")
                .build()

            when (mode) {

                1 -> {
                    val restaurant: RestaurantEntity? =
                        db.restDao().getRestaurantById(restaurantEntity.resId)
                    db.close()
                    return restaurant != null
                }

                2 -> {
                    db.restDao().insertRestaurant(restaurantEntity)
                    db.close()
                    return true
                }
                3 -> {
                    db.restDao().deleteRestaurant(restaurantEntity)
                    db.close()
                    return true
                }

            }
            return false
        }
    }
}