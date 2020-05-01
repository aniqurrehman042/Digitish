package com.example.test_04.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test_04.R
import com.example.test_04.models.Product
import com.example.test_04.ui.CustomerHome
import java.util.*

class ProductsAdapter(var context: Context, var products: ArrayList<Product>) : RecyclerView.Adapter<ProductsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivProductImg: ImageView = itemView.findViewById(R.id.iv_product_img)
        var tvProductName: TextView = itemView.findViewById(R.id.tv_product_name)
        var llMain: LinearLayout = itemView.findViewById(R.id.ll_main)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_product_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return products.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvProductName.text = products[position].productName
        when (products[position].productCategory) {
            "Washing Machines" -> holder.ivProductImg.setImageResource(R.drawable.item5)
            "Televisions" -> holder.ivProductImg.setImageResource(R.drawable.item2)
            "Microwaves" -> holder.ivProductImg.setImageResource(R.drawable.item4)
            "Refrigerators" -> holder.ivProductImg.setImageResource(R.drawable.item3)
        }

        holder.llMain.setOnClickListener {
            (context as CustomerHome).startProductCategorySearch(products[position].productCategory)
        }

    }
}