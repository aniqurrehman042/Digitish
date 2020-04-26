package com.example.test_04.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_04.R;
import com.example.test_04.models.Chat;
import com.example.test_04.ui.CustomerHome;
import com.example.test_04.utils.DateUtils;
import com.example.test_04.utils.SwitchUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.makeramen.roundedimageview.RoundedImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.ViewHolder> {

    private CustomerHome customerHome;
    private ArrayList<Chat> chatsWithLastMsg;
    private Date lastDate = Calendar.getInstance().getTime();

    public InboxAdapter(CustomerHome customerHome, ArrayList<Chat> chatsWithLastMsg) {
        this.customerHome = customerHome;
        this.chatsWithLastMsg = chatsWithLastMsg;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_inbox_item, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        TextView tvDateHeading = holder.tvDateHeading;

        holder.tvMes2.setVisibility(View.GONE);
        holder.clMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String merchantEmail = chatsWithLastMsg.get(position).getMerchantEmail();
                String customerEmail = chatsWithLastMsg.get(position).getCustomerEmail();
                getChatBetween(merchantEmail, customerEmail);
            }
        });

        holder.ivMerchantLogo.setImageResource(SwitchUtils.getMerchantImgId(chatsWithLastMsg.get(position).getMerchantName()));

        String lastMessage = chatsWithLastMsg.get(position).getMessage();

        if (lastMessage.isEmpty()) {
            holder.ivAttachment.setVisibility(View.VISIBLE);
        } else {
            holder.ivAttachment.setVisibility(View.GONE);
            holder.tvMes1.setText(lastMessage);
        }

        holder.tvMerchantName.setText(chatsWithLastMsg.get(position).getMerchantName());
        holder.tvDate.setText(chatsWithLastMsg.get(position).getDate());

        Date currentDate = Calendar.getInstance().getTime();
        Date msgDate = DateUtils.stringToDateWithTime(chatsWithLastMsg.get(position).getDate());

        if (DateUtils.isSameDay(currentDate, msgDate) && position < 1){

            tvDateHeading.setVisibility(View.VISIBLE);
            tvDateHeading.setText("Today");

        } else if (DateUtils.isSameDay(msgDate, lastDate)) {

            tvDateHeading.setVisibility(View.GONE);

        } else {
            lastDate = msgDate;
            tvDateHeading.setVisibility(View.VISIBLE);
            tvDateHeading.setText(DateUtils.dateToString(msgDate));
        }

    }

    @Override
    public int getItemCount() {
        return chatsWithLastMsg.size();
    }

    private void getChatBetween(final String merchantEmail, final String customerEmail) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Chat")
                .whereEqualTo("Merchant Email", merchantEmail)
                .whereEqualTo("Customer Email", customerEmail)
                .orderBy("Date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<Chat> chats = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult().getDocuments()) {
                                String customerName = document.get("Customer Name").toString();
                                String merchantName = document.get("Merchant Name").toString();
                                String image = document.get("Image").toString();
                                String message = document.get("Message").toString();
                                String sender = document.get("Sender").toString();
                                boolean read = document.getBoolean("Read");
                                Timestamp timestamp = (Timestamp) document.get("Date");
                                Date dateObj = timestamp.toDate();
                                String date = DateUtils.dateToStringWithTime(dateObj);
                                Chat chat = new Chat(customerEmail, customerName, merchantEmail, merchantName, image, message, sender, read, date);
                                chats.add(chat);
                            }
                            customerHome.startChatFragment(chats, null);
                        } else {
                            Toast.makeText(customerHome, "Couldn't load chat", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout clMain;
        RoundedImageView ivMerchantLogo;
        ImageView ivAttachment;
        TextView tvMerchantName;
        TextView tvDate;
        TextView tvMes1;
        TextView tvMes2;
        TextView tvDateHeading;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            clMain = itemView.findViewById(R.id.cl_main);
            ivMerchantLogo = itemView.findViewById(R.id.riv_merchant_logo);
            ivAttachment = itemView.findViewById(R.id.iv_attachment);
            tvMerchantName = itemView.findViewById(R.id.tv_merchant_name);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvMes1 = itemView.findViewById(R.id.tv_mes1);
            tvMes2 = itemView.findViewById(R.id.tv_mes2);
            tvDateHeading = itemView.findViewById(R.id.tv_date_heading);
        }
    }
}
