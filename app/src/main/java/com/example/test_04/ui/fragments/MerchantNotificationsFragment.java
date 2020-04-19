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
import com.example.test_04.adapters.MerchantNotificationsAdapter;
import com.example.test_04.adapters.NotificationsAdapter;
import com.example.test_04.db_callbacks.IGetMerchantReviews;
import com.example.test_04.db_callbacks.IGetProductReviews;
import com.example.test_04.models.CurrentMerchant;
import com.example.test_04.models.CustomerNotification;
import com.example.test_04.models.Merchant;
import com.example.test_04.models.MerchantReview;
import com.example.test_04.models.ProductReview;
import com.example.test_04.ui.MerchantHome;
import com.example.test_04.utils.DBUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class MerchantNotificationsFragment extends Fragment {

    private RecyclerView rvNotifications;

    private MerchantHome merchantHome;
    private ArrayList<CustomerNotification> customerNotifications = new ArrayList<>();
    private ArrayList<MerchantReview> merchantReviews = new ArrayList<>();
    private ArrayList<ArrayList<ProductReview>> notifications = new ArrayList<>();
    private ProgressDialog progressDialog;

    public MerchantNotificationsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_merchant_notifications, container, false);

        merchantHome = (MerchantHome) getActivity();
        findViews(view);
        init();

        return view;
    }

    private void showProgressDialog(String title) {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle(title);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void init() {
        getNotificationsReviews();
    }

    private void getNotificationsReviews() {

        showProgressDialog("Loading notifications");

        DBUtils.getMerchantProductReviews(CurrentMerchant.name, new IGetProductReviews() {
            @Override
            public void onCallback(boolean successful, @NotNull final ArrayList<ProductReview> productReviews) {
                if (successful) {

                    ArrayList<ProductReview> reviews1 = new ArrayList<>();
                    ArrayList<ProductReview> reviews2 = new ArrayList<>();
                    ArrayList<ProductReview> reviews3 = new ArrayList<>();
                    ArrayList<ProductReview> reviews4 = new ArrayList<>();
                    ArrayList<ProductReview> reviews5 = new ArrayList<>();

                    for (ProductReview productReview : productReviews) {
                        switch (productReview.getProductRating()) {
                            case 1:
                                reviews1.add(productReview);
                                break;
                            case 2:
                                reviews2.add(productReview);
                                break;
                            case 3:
                                reviews3.add(productReview);
                                break;
                            case 4:
                                reviews4.add(productReview);
                                break;
                            case 5:
                                reviews5.add(productReview);
                                break;
                        }
                    }

                    notifications.add(reviews1);
                    notifications.add(reviews2);
                    notifications.add(reviews3);
                    notifications.add(reviews4);
                    notifications.add(reviews5);

                    for (int i = 1; i < 6; i++) {
                        customerNotifications.add(new CustomerNotification("", "", i + "-Star Reviews", "Read all " + i + "-star reviews for products", "Product Review", "", String.valueOf(notifications.get(i-1).size())));
                    }

                    DBUtils.getMerchantReviews(CurrentMerchant.name, new IGetMerchantReviews() {
                        @Override
                        public void onCallback(boolean successful, @NotNull ArrayList<MerchantReview> merchantReviews) {
                            if (successful) {
                                MerchantNotificationsFragment.this.merchantReviews = merchantReviews;
                                for (MerchantReview merchantReview : merchantReviews) {
                                    customerNotifications.add(new CustomerNotification(merchantReview.getCustomerEmail(), merchantReview.getMerchantName(), "Company has been rated by " + merchantReview.getCustomerNAme(), "Tap to view", "Merchant Review", "", ""));
                                }

                                setUpNotificationsRecycler();
                            } else {
                                Toast.makeText(merchantHome, "Couldn't load notifications", Toast.LENGTH_SHORT).show();
                            }

                            progressDialog.dismiss();
                        }
                    });
                } else {
                    Toast.makeText(merchantHome, "Couldn't load notifications", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void findViews(View view) {
        rvNotifications = view.findViewById(R.id.rv_notifications);
    }

    private void setUpNotificationsRecycler() {
        MerchantNotificationsAdapter adapter = new MerchantNotificationsAdapter(customerNotifications, notifications, merchantReviews, merchantHome);
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNotifications.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        merchantHome.setPageTitle("Notifications");
    }
}
