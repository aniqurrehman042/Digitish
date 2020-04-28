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
import com.example.test_04.comparators.DateComparator;
import com.example.test_04.db_callbacks.IGetMerchantReviews;
import com.example.test_04.db_callbacks.IGetProductReviewChats;
import com.example.test_04.db_callbacks.IGetProductReviews;
import com.example.test_04.models.CurrentMerchant;
import com.example.test_04.models.CustomerNotification;
import com.example.test_04.models.MerchantReview;
import com.example.test_04.models.ProductReview;
import com.example.test_04.models.ProductReviewChat;
import com.example.test_04.ui.MerchantHome;
import com.example.test_04.utils.DBUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A simple {@link Fragment} subclass.
 */
public class MerchantNotificationsFragment extends Fragment {

    private RecyclerView rvNotifications;

    private MerchantHome merchantHome;
    private ArrayList<CustomerNotification> customerNotificationsHolder = new ArrayList<>();
    private ArrayList<DateComparator> customerNotificationsData = new ArrayList<>();
    private ArrayList<ArrayList<ProductReview>> notifications = new ArrayList<>();
    private ProgressDialog progressDialog;
    private int done = 0;
    private boolean addingData = false;
    private boolean madeAsynchCalls = false;
    MerchantNotificationsAdapter adapter;

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
        if (!isProductReview())
            getNotifications();
    }

    private boolean isProductReview() {
        if (getArguments() != null) {
            ProductReview productReview = (ProductReview) getArguments().getSerializable("Product Review");

            if (productReview != null) {
                startProductReviewChatThroughNotificationsRecycler(productReview);
                setArguments(null);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void startProductReviewChatThroughNotificationsRecycler(ProductReview productReview) {
        rvNotifications.setVisibility(View.VISIBLE);
        adapter = new MerchantNotificationsAdapter(customerNotificationsHolder, null, merchantHome, null, productReview);
        rvNotifications.setLayoutManager(new LinearLayoutManager(merchantHome));
        rvNotifications.setAdapter(adapter);

        if (customerNotificationsHolder.isEmpty()) {
            rvNotifications.setVisibility(View.GONE);
        }
    }

    private void getNotifications() {

        if (madeAsynchCalls)
            return;

        madeAsynchCalls = true;

        done = 0;

        customerNotificationsData = new ArrayList<>();
        customerNotificationsHolder = new ArrayList<>();

        if (progressDialog != null)
            progressDialog.dismiss();

        showProgressDialog("Loading Notifications");

        DBUtils.getMerchantProductReviews(CurrentMerchant.name, new IGetProductReviews() {
            @Override
            public void onCallback(boolean successful, @NotNull ArrayList<ProductReview> productReviews) {
                if (successful) {

                    for (ProductReview productReview : productReviews) {
                        productReview.setType("Product Review");
                        customerNotificationsData.add(productReview);
                    }

                } else {
                    Toast.makeText(merchantHome, "Couldn't load notifications", Toast.LENGTH_SHORT).show();
                }

                checkAndSetRecycler();

            }
        });

        DBUtils.getMerchantReviews(CurrentMerchant.name, new IGetMerchantReviews() {
            @Override
            public void onCallback(boolean successful, @NotNull ArrayList<MerchantReview> merchantReviews) {
                if (successful) {

                    for (MerchantReview merchantReview : merchantReviews) {
                        merchantReview.setType("Merchant Review");
                        customerNotificationsData.add(merchantReview);
                    }

                } else {
                    Toast.makeText(merchantHome, "Couldn't load notifications", Toast.LENGTH_SHORT).show();
                }

                checkAndSetRecycler();
            }
        });

        DBUtils.getMerchantProductReviewChats(CurrentMerchant.name, new IGetProductReviewChats() {
            @Override
            public void onCallback(boolean successful, @NotNull ArrayList<ProductReviewChat> productReviewChats) {
                if (successful) {

                    for (ProductReviewChat productReviewChat : productReviewChats) {
                        productReviewChat.setType("Product Review Chat");
                        customerNotificationsData.add(productReviewChat);
                    }

                } else {
                    Toast.makeText(merchantHome, "Couldn't load notifications", Toast.LENGTH_SHORT).show();
                }

                checkAndSetRecycler();
            }
        });
    }

    private void checkAndSetRecycler() {
        done++;
        if (done > 2) {
            madeAsynchCalls = false;
            Collections.sort(customerNotificationsData, Collections.<DateComparator>reverseOrder());
            addHolderFromData();
            setUpNotificationsRecycler();
            progressDialog.dismiss();
        }
    }

    private void addHolderFromData() {
        customerNotificationsHolder.clear();

        if (addingData)
            return;

        addingData = true;

        for (DateComparator dateComparator : customerNotificationsData) {

            String title = "";
            String customerName = "";
            String message = "Tap to view";
            String type = dateComparator.getType();
            String date = dateComparator.getDate();

            if (type.equals("Product Review")) {
                ProductReview productReview = (ProductReview) dateComparator;
                title = productReview.getCustomerName() + " added a review";
                message = productReview.getProductName();
            } else if (type.equals("Merchant Review")) {
                MerchantReview merchantReview = (MerchantReview) dateComparator;
                title = "Company has been rated by " + merchantReview.getCustomerName();
            } else if (type.equals("Product Review Chat")) {
                ProductReviewChat productReviewChat = (ProductReviewChat) dateComparator;
                title = productReviewChat.getCustomerName() + " has commented on your product's review";
            }

            customerNotificationsHolder.add(new CustomerNotification(customerName, title, message, type, date));
        }

        addingData = false;
    }

    private void getProductReviews() {

        notifications = new ArrayList<>();

        if (progressDialog != null)
            progressDialog.dismiss();

        showProgressDialog("Loading Notifications");

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

                } else {
                    Toast.makeText(merchantHome, "Couldn't load notifications", Toast.LENGTH_SHORT).show();
                }

                progressDialog.dismiss();
            }
        });
    }

    private void findViews(View view) {
        rvNotifications = view.findViewById(R.id.rv_notifications);
    }

    private void setUpNotificationsRecycler() {
        rvNotifications.setVisibility(View.VISIBLE);
        adapter = new MerchantNotificationsAdapter(customerNotificationsHolder, customerNotificationsData, merchantHome, null, null);
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNotifications.setAdapter(adapter);

        if (customerNotificationsHolder.isEmpty()) {
            rvNotifications.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        merchantHome.setPageTitle("Notifications");
        done = 0;
    }

    @Override
    public void onPause() {
        super.onPause();
        done = 0;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressDialog != null)
            progressDialog.dismiss();
    }
}
