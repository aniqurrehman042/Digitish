package com.example.test_04.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_04.R;
import com.example.test_04.async.DownloadImageTask;
import com.example.test_04.models.Chat;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private ArrayList<Chat> chats;
    private boolean isMerchantChat;
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    public ChatAdapter(ArrayList<Chat> chats, boolean isMerchantChat) {
        this.chats = chats;
        this.isMerchantChat = isMerchantChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chat_item, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TextView tvMyMsg = holder.tvMyMsg;
        TextView tvOtherMsg = holder.tvOtherMsg;
        TextView tvDate = holder.tvDate;
        TextView tvNotSent = holder.tvNotSent;
        ImageView ivMyImg = holder.ivMyImg;
        ImageView ivOtherImg = holder.ivOtherImg;
        LinearLayout llChat = holder.llChat;

        Chat chat = chats.get(position);

        String message = chat.getMessage();
        String date = chat.getDate();
        String image = chat.getImage();
        String sender = chat.getSender();
        Bitmap bitmap = chat.getBitmap();

        if (chats.get(position).isSent())
            tvNotSent.setVisibility(View.GONE);
        else
            tvNotSent.setVisibility(View.VISIBLE);

        tvDate.setText(date);

        if (isMerchantChat) {
            if (sender.equals("Merchant")) {
                tvOtherMsg.setVisibility(View.GONE);
                ivOtherImg.setVisibility(View.GONE);

                if (image.equals("")) {
                    tvMyMsg.setVisibility(View.VISIBLE);
                    tvMyMsg.setText(message);
                } else {
                    llChat.setVisibility(View.GONE);
                    ivMyImg.setVisibility(View.VISIBLE);
                    if (bitmap == null)
                        setImage(image, ivMyImg, tvNotSent, chat);
                    else
                        ivMyImg.setImageBitmap(bitmap);
                }
            } else {
                tvMyMsg.setVisibility(View.GONE);

                if (image.equals("")) {
                    tvOtherMsg.setVisibility(View.VISIBLE);
                    tvOtherMsg.setText(message);
                } else {
                    llChat.setVisibility(View.GONE);
                    ivOtherImg.setVisibility(View.VISIBLE);
                    if (bitmap == null)
                        setImage(image, ivOtherImg, tvNotSent, chat);
                    else
                        ivOtherImg.setImageBitmap(bitmap);
                }
            }
        } else {
            if (sender.equals("Customer")) {
                tvOtherMsg.setVisibility(View.GONE);
                ivOtherImg.setVisibility(View.GONE);

                if (image.equals("")) {
                    tvMyMsg.setVisibility(View.VISIBLE);
                    tvMyMsg.setText(message);
                } else {
                    llChat.setVisibility(View.GONE);
                    ivMyImg.setVisibility(View.VISIBLE);
                    if (bitmap == null)
                        setImage(image, ivMyImg, tvNotSent, chat);
                    else
                        ivMyImg.setImageBitmap(bitmap);
                }
            } else {
                tvMyMsg.setVisibility(View.GONE);

                if (image.equals("")) {
                    tvOtherMsg.setVisibility(View.VISIBLE);
                    tvOtherMsg.setText(message);
                } else {
                    llChat.setVisibility(View.GONE);
                    ivOtherImg.setVisibility(View.VISIBLE);
                    if (bitmap == null)
                        setImage(image, ivOtherImg, tvNotSent, chat);
                    else
                        ivOtherImg.setImageBitmap(bitmap);
                }
            }
        }

    }

    private void setImage(String image, final ImageView imageView, final TextView notSent, final Chat chat) {

        final long ONE_MEGABYTE = 1024 * 1024;
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child("chatimages/" + image);

        imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Data for "images/island.jpg" is returns, use this as needed
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imageView.setImageBitmap(bitmap);
                chat.setBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                notSent.setVisibility(View.VISIBLE);
                notSent.setText("Couldn't load image");
            }
        });

    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvOtherMsg;
        TextView tvMyMsg;
        TextView tvDate;
        TextView tvNotSent;
        ImageView ivMyImg;
        ImageView ivOtherImg;
        LinearLayout llChat;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvMyMsg = itemView.findViewById(R.id.tv_my_msg);
            tvOtherMsg = itemView.findViewById(R.id.tv_other_msg);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvNotSent = itemView.findViewById(R.id.tv_not_sent);
            ivMyImg = itemView.findViewById(R.id.iv_my_img);
            ivOtherImg = itemView.findViewById(R.id.iv_other_img);
            llChat = itemView.findViewById(R.id.ll_chat);

        }
    }
}
