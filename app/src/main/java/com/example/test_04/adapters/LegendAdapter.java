package com.example.test_04.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_04.R;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

public class LegendAdapter extends RecyclerView.Adapter<LegendAdapter.ViewHolder> {

    private ArrayList<PieEntry> entries;
    private int[] colors = {
            Color.argb(255, 46, 204, 113),
            Color.argb(255, 241, 196, 15),
            Color.argb(255, 231, 76, 60),
            Color.argb(255, 52, 152, 219)
    };

    public LegendAdapter(ArrayList<PieEntry> entries) {
        this.entries = entries;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_legend_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.ivLegendForm.setColorFilter(colors[position % 4]);
        holder.tvLegendName.setText(entries.get(position).getLabel());
        holder.tvLegendValue.setText(String.valueOf((int) entries.get(position).getValue()) + " Sales");
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivLegendForm;
        TextView tvLegendName;
        TextView tvLegendValue;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivLegendForm = itemView.findViewById(R.id.iv_legend_form);
            tvLegendName = itemView.findViewById(R.id.tv_legend_name);
            tvLegendValue = itemView.findViewById(R.id.tv_legend_value);

        }
    }
}
