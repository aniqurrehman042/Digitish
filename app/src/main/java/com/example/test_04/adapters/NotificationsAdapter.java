package com.example.test_04.adapters;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_04.R;
import com.example.test_04.models.CustomerNotification;
import com.example.test_04.utils.DateUtils;
import com.example.test_04.utils.SwitchUtils;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    private Context context;
    private ArrayList<CustomerNotification> customerNotifications;
    private Date lastDate = Calendar.getInstance().getTime();

    public NotificationsAdapter(ArrayList<CustomerNotification> customerNotifications) {
        this.customerNotifications = customerNotifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_notification_item, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        TextView tvDateHeading = holder.tvDateHeading;
        TextView tvNotificationTitle = holder.tvNotificationTitle;
        TextView tvNotificationDesc = holder.tvNotificationDisc;
        ImageView ivNotificationImg = holder.ivNotificationImg;
        TextView tvUnread = holder.tvUnread;

        CustomerNotification customerNotification = customerNotifications.get(position);

        tvUnread.setVisibility(View.GONE);

        Date currentDate = Calendar.getInstance().getTime();
        Date msgDate = DateUtils.stringToDateWithTime(customerNotifications.get(position).getDate());

        if (DateUtils.isSameDay(currentDate, msgDate) && position < 1) {
            tvDateHeading.setVisibility(View.VISIBLE);
            tvDateHeading.setText("Today");

        } else if (DateUtils.isSameDay(msgDate, lastDate)) {
            tvDateHeading.setVisibility(View.GONE);
        } else {
            lastDate = msgDate;
            tvDateHeading.setVisibility(View.VISIBLE);
            tvDateHeading.setText(DateUtils.dateToString(msgDate));
        }

        tvNotificationTitle.setText(customerNotification.getTitle());
        tvNotificationDesc.setText(customerNotification.getMessage());
        ivNotificationImg.setImageResource(R.drawable.ic_star);

        if (customerNotification.getType().equals("Product Review")) {

            ivNotificationImg.setImageResource(R.drawable.ic_product_review);

        } else if (customerNotification.getType().equals("Merchant Review")) {

            ivNotificationImg.setImageResource(R.drawable.ic_merchant_rating);

        } else if (customerNotification.getType().equals("Product Review Chat")) {

            ivNotificationImg.setImageResource(R.drawable.ic_merchant_rating);

        }

}

    @Override
    public int getItemCount() {
        return customerNotifications.size();
    }

public class ViewHolder extends RecyclerView.ViewHolder {

    LinearLayout llMain;
    ImageView ivNotificationImg;
    TextView tvNotificationTitle;
    TextView tvNotificationDisc;
    TextView tvUnread;
    TextView tvDateHeading;
    CardView cvNotification;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        ivNotificationImg = itemView.findViewById(R.id.iv_notification_img);
        tvNotificationTitle = itemView.findViewById(R.id.tv_notification_title);
        tvNotificationDisc = itemView.findViewById(R.id.tv_notification_disc);
        cvNotification = itemView.findViewById(R.id.cv_notification);
        llMain = itemView.findViewById(R.id.ll_main);
        tvDateHeading = itemView.findViewById(R.id.tv_date_heading);
        tvUnread = itemView.findViewById(R.id.tv_unread);
    }
}
}
