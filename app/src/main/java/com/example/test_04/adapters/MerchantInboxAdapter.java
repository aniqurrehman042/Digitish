package com.example.test_04.adapters;

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
import com.example.test_04.ui.MerchantHome;
import com.example.test_04.utils.DateUtils;
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
import java.util.HashSet;
import java.util.Set;

public class MerchantInboxAdapter extends RecyclerView.Adapter<MerchantInboxAdapter.ViewHolder> {

    MerchantHome merchantHome;
    ArrayList<Chat> chatsWithLastMsg;
    private Date lastDate = Calendar.getInstance().getTime();

    public MerchantInboxAdapter(MerchantHome merchantHome, ArrayList<Chat> chatsWithLastMsg) {
        this.merchantHome = merchantHome;
        this.chatsWithLastMsg = chatsWithLastMsg;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_merchant_inbox_item, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        TextView tvDateHeading = holder.tvDateHeading;

        holder.clMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String merchantEmail = chatsWithLastMsg.get(position).getMerchantEmail();
                String customerEmail = chatsWithLastMsg.get(position).getCustomerEmail();
                getChatBetween(merchantEmail, customerEmail);
            }
        });

        String lastMessage = chatsWithLastMsg.get(position).getMessage();

        if (lastMessage.isEmpty()) {
            holder.ivAttachment.setVisibility(View.VISIBLE);
        } else {
            holder.ivAttachment.setVisibility(View.GONE);
            holder.tvMes1.setText(lastMessage);
        }

        holder.tvCustomerName.setText(chatsWithLastMsg.get(position).getCustomerName());
        holder.tvCustomerAlias.setText(String.valueOf(chatsWithLastMsg.get(position).getCustomerName().charAt(0)));
        holder.tvDate.setText(chatsWithLastMsg.get(position).getDate());

        Date currentDate = Calendar.getInstance().getTime();
        Date msgDate = DateUtils.stringToDateWithTime(chatsWithLastMsg.get(position).getDate());

        if (DateUtils.isSameDay(currentDate, msgDate)){

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
                            for (DocumentSnapshot document: task.getResult().getDocuments()) {
                                String customerName = document.get("Customer Name").toString();
                                String merchantName = document.get("Merchant Name").toString();
                                String image = document.get("Image").toString();
                                String message = document.get("Message").toString();
                                String sender = document.get("Sender").toString();
                                boolean read = document.getBoolean("Read");
                                Timestamp timestamp = (Timestamp) document.get("Date");
                                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy - HH:mm");
                                Date dateObj = timestamp.toDate();
                                String date = sdf.format(dateObj);
                                Chat chat = new Chat(customerEmail, customerName, merchantEmail, merchantName, image, message, sender, read, date);
                                chats.add(chat);
                            }
                            merchantHome.startChatFragment(chats);
                        } else {
                            Toast.makeText(merchantHome, "Couldn't load chat", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout clMain;
        RoundedImageView rivBgText;
        ImageView ivAttachment;
        TextView tvCustomerAlias;
        TextView tvCustomerName;
        TextView tvDate;
        TextView tvMes1;
        TextView tvDateHeading;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            clMain = itemView.findViewById(R.id.cl_main);
            rivBgText = itemView.findViewById(R.id.riv_bg_text);
            ivAttachment = itemView.findViewById(R.id.iv_attachment);
            tvCustomerAlias = itemView.findViewById(R.id.tv_customer_alias);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvMes1 = itemView.findViewById(R.id.tv_mes1);
            tvDateHeading = itemView.findViewById(R.id.tv_date_heading);

        }
    }
}
