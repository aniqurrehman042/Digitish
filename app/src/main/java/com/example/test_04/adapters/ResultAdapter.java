package com.example.test_04.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_04.R;
import com.example.test_04.models.Merchant;
import com.example.test_04.ui.CustomerHome;
import com.example.test_04.utils.SwitchUtils;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {

    ArrayList<Merchant> merchants;
    CustomerHome customerHome;

    public ResultAdapter(CustomerHome customerHome, ArrayList<Merchant> merchants) {
        this.merchants = merchants;
        this.customerHome = customerHome;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_result_item, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.llMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customerHome.startMerchantDetailsFragment(merchants.get(position));
            }
        });

        holder.tvResultName.setText(merchants.get(position).getName());
        holder.tvResultDesc.setText(merchants.get(position).getWebsite());
        holder.rivResultImg.setImageResource(SwitchUtils.getMerchantImgId(merchants.get(position).getName()));
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llMain;
        RoundedImageView rivResultImg;
        TextView tvResultName;
        TextView tvResultDesc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            llMain = itemView.findViewById(R.id.ll_main);
            rivResultImg = itemView.findViewById(R.id.riv_result_img);
            tvResultName = itemView.findViewById(R.id.tv_result_name);
            tvResultDesc = itemView.findViewById(R.id.tv_result_desc);
        }
    }
}
