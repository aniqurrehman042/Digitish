package com.example.test_04.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_04.R;
import com.example.test_04.models.Product;

import java.util.ArrayList;

public class SuggestionsAdapter extends RecyclerView.Adapter<SuggestionsAdapter.ViewHolder> {

    ArrayList<Product> products;
    Context context;

    public SuggestionsAdapter(Context context, ArrayList<Product> products) {
        this.products = products;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_suggesstion_product_item, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvProductName.setText(products.get(position).getProductName());

        switch (products.get(position).getProductCategory()) {
            case "Washing Machines":
            holder.ivProductImg.setImageResource(R.drawable.item5);
            break;
            case "Televisions":
                holder.ivProductImg.setImageResource(R.drawable.item2);
                break;
            case "Microwaves":
                holder.ivProductImg.setImageResource(R.drawable.item4);
                break;
            case "Refrigerators":
                holder.ivProductImg.setImageResource(R.drawable.item3);
                break;
            case "Televisions1":
                holder.ivProductImg.setImageResource(R.drawable.item1);
                break;

        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivProductImg;
        TextView tvProductName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImg = itemView.findViewById(R.id.iv_product_img);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
        }
    }
}
