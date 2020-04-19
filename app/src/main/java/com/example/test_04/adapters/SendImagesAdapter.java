package com.example.test_04.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_04.R;

import java.util.ArrayList;

public class SendImagesAdapter extends RecyclerView.Adapter<SendImagesAdapter.ViewHolder> {

    private ArrayList<Bitmap> bitmaps;
    private View recycler;

    public SendImagesAdapter(ArrayList<Bitmap> bitmaps, View recycler) {
        this.bitmaps = bitmaps;
        this.recycler = recycler;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_send_images_item, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        holder.ivSendImg.setImageBitmap(bitmaps.get(position));

        holder.llClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitmaps.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, bitmaps.size());
                if (bitmaps.isEmpty())
                    recycler.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bitmaps.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivSendImg;
        LinearLayout llClose;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivSendImg = itemView.findViewById(R.id.iv_send_img);
            llClose = itemView.findViewById(R.id.ll_close);

        }
    }
}
