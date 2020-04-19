package com.example.test_04.ui.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.test_04.R;
import com.example.test_04.adapters.NotificationsAdapter;
import com.example.test_04.db_callbacks.IGetProductReviews;
import com.example.test_04.models.CurrentCustomer;
import com.example.test_04.models.CustomerNotification;
import com.example.test_04.models.ProductReview;
import com.example.test_04.utils.DBUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationsFragment extends Fragment {

    private RecyclerView rvNotifications;

    private NotificationsAdapter adapter;
    private ProgressDialog progressDialog;
    private ArrayList<CustomerNotification> customerNotifications = new ArrayList<>();

    public NotificationsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        findViews(view);
        init();

        return view;
    }

    private void findViews(View view) {
        rvNotifications = view.findViewById(R.id.rv_notifications);
    }

    private void init() {
        getCustomerNotifications();
    }

    private void showProgressDialog(String title) {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle(title);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void getCustomerNotifications() {

        showProgressDialog("Loading notifications");

        DBUtils.getProductReviews(CurrentCustomer.email, new IGetProductReviews() {
            @Override
            public void onCallback(boolean successful, @NotNull ArrayList<ProductReview> productReviews) {
                if (successful) {
                    for (ProductReview productReview : productReviews) {
                        customerNotifications.add(new CustomerNotification(productReview.getCustomerEmail(), productReview.getMerchantName(), "Review Published - " + productReview.getMerchantName(), "Your review for " + productReview.getProductName() + " has been published", "Review", productReview.getDate(), ""));
                    }

                    setUpNotificationsRecycler();
                } else {
                    Toast.makeText(getContext(), "Couldn't load notifications", Toast.LENGTH_SHORT).show();
                }

                progressDialog.dismiss();
            }
        });
    }

    private void setUpNotificationsRecycler() {
        adapter = new NotificationsAdapter(customerNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNotifications.setAdapter(adapter);
    }
}
