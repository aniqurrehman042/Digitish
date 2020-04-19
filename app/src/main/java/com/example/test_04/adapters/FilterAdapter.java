package com.example.test_04.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_04.R;
import com.example.test_04.models.Filter;
import com.example.test_04.ui.fragments.SearchFragment;

import java.util.ArrayList;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.ViewHolder> {
    private ArrayList<Filter> filters;
    private Context context;
    private int selectedFilter;
    private SearchFragment searchFragment;

    public int getSelectedFilter() {
        return selectedFilter;
    }

    public FilterAdapter(ArrayList<Filter> filters, SearchFragment searchFragment) {
        this.filters = filters;
        selectedFilter = 0;
        this.searchFragment = searchFragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_select_item, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        holder.btnSelect.setText(filters.get(position).getFilterName());
        if (filters.get(position).getSelected()){
            holder.btnSelect.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.bg_btn_light_round, null));
            holder.btnSelect.setTextColor(ContextCompat.getColor(context, R.color.white));
        } else {
            holder.btnSelect.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.bg_btn_white_round, null));
            holder.btnSelect.setTextColor(ContextCompat.getColor(context, R.color.black));
        }

        holder.btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swapSelectedFilter(position);
                applyFilter(position);
            }
        });
    }

    private void applyFilter(int position) {
        switch (position) {
            case 0:
                searchFragment.setUpMerchantsRecycler();
                break;
            case 1:
                searchFragment.getReviews("", "");
                break;
            case 2:
                searchFragment.getReviews("Product Rating", "5");
                break;
            case 3:
                searchFragment.getReviews("Product Rating", "4");
                break;
            case 4:
                searchFragment.getReviews("Product Rating", "3");
                break;
            case 5:
                searchFragment.getReviews("Product Rating", "2");
                break;
            case 6:
                searchFragment.getReviews("Product Rating", "1");
                break;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void swapSelectedFilter(int position) {
        filters.get(selectedFilter).setSelected(false);
        notifyItemChanged(selectedFilter);
        filters.get(position).setSelected(true);
        selectedFilter = position;
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return filters.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        Button btnSelect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            btnSelect = itemView.findViewById(R.id.btn_select);
        }
    }
}
