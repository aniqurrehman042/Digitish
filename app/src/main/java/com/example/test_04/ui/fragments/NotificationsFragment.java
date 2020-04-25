package com.example.test_04.ui.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_04.R;
import com.example.test_04.adapters.MerchantNotificationsAdapter;
import com.example.test_04.comparators.DateComparator;
import com.example.test_04.db_callbacks.IGetMerchantReviews;
import com.example.test_04.db_callbacks.IGetOffers;
import com.example.test_04.db_callbacks.IGetProductReviewChats;
import com.example.test_04.db_callbacks.IGetProductReviews;
import com.example.test_04.db_callbacks.IIsOldCustomer;
import com.example.test_04.models.CurrentCustomer;
import com.example.test_04.models.CustomerNotification;
import com.example.test_04.models.MerchantReview;
import com.example.test_04.models.Offer;
import com.example.test_04.models.ProductReview;
import com.example.test_04.models.ProductReviewChat;
import com.example.test_04.ui.CustomerHome;
import com.example.test_04.utils.DBUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationsFragment extends Fragment {

    private RecyclerView rvNotifications;

    private MerchantNotificationsAdapter adapter;
    private ProgressDialog progressDialog;
    private CustomerHome customerHome;
    private ArrayList<CustomerNotification> customerNotificationsHolder = new ArrayList<>();
    private ArrayList<DateComparator> customerNotificationsData = new ArrayList<>();
    private int done = 0;
    private int offerDone = 0;

    public NotificationsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        customerHome = (CustomerHome) getActivity();

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

        done = 0;

        showProgressDialog("Loading notifications");

        customerNotificationsHolder = new ArrayList<>();
        customerNotificationsData = new ArrayList<>();

        DBUtils.getReviewedProductReviews(CurrentCustomer.email, new IGetProductReviews() {
            @Override
            public void onCallback(boolean successful, @NotNull ArrayList<ProductReview> productReviews) {
                if (successful) {
                    customerNotificationsData.addAll(productReviews);
                } else {
                    Toast.makeText(getContext(), "Couldn't load notifications", Toast.LENGTH_SHORT).show();
                }

                checkAndSetRecycler();
            }
        });

        DBUtils.getCustomerProductReviewChats(CurrentCustomer.email, new IGetProductReviewChats() {
            @Override
            public void onCallback(boolean successful, @NotNull ArrayList<ProductReviewChat> productReviewChats) {
                if (successful) {
                    customerNotificationsData.addAll(productReviewChats);
                } else {
                    Toast.makeText(getContext(), "Couldn't load notifications", Toast.LENGTH_SHORT).show();
                }

                checkAndSetRecycler();
            }
        });

        DBUtils.getCustomerMerchantReviews(CurrentCustomer.email, new IGetMerchantReviews() {
            @Override
            public void onCallback(boolean successful, @NotNull ArrayList<MerchantReview> merchantReviews) {
                if (successful) {
                    customerNotificationsData.addAll(merchantReviews);
                } else {
                    Toast.makeText(getContext(), "Couldn't load notifications", Toast.LENGTH_SHORT).show();
                }

                checkAndSetRecycler();
            }
        });

        DBUtils.getOffers(new IGetOffers() {
            @Override
            public void onCallback(boolean successful, @NotNull ArrayList<Offer> offers) {
                if (successful) {
                    addValidOffers(offers);
                } else {
                    Toast.makeText(getContext(), "Couldn't load notifications", Toast.LENGTH_SHORT).show();
                    checkAndSetRecycler();
                }
            }
        });
    }

    private void addValidOffers(final ArrayList<Offer> offers) {
        final boolean[] abansCustomer = {false};
        final boolean[] singerCustomer = {false};
        final boolean[] softlogicCustomer = {false};

        offerDone = 0;

        DBUtils.isOldCustomer(CurrentCustomer.email, "Abans", new IIsOldCustomer() {
            @Override
            public void onCallback(boolean successful, boolean oldCustomer) {
                if (successful && oldCustomer)
                    abansCustomer[0] = true;
                checkAndAddOffers(offers, abansCustomer, singerCustomer, softlogicCustomer);
            }
        });

        DBUtils.isOldCustomer(CurrentCustomer.email, "Singer", new IIsOldCustomer() {
            @Override
            public void onCallback(boolean successful, boolean oldCustomer) {
                if (successful && oldCustomer)
                    singerCustomer[0] = true;
                checkAndAddOffers(offers, abansCustomer, singerCustomer, softlogicCustomer);
            }
        });

        DBUtils.isOldCustomer(CurrentCustomer.email, "Softlogic Holdings PLC", new IIsOldCustomer() {
            @Override
            public void onCallback(boolean successful, boolean oldCustomer) {
                if (successful && oldCustomer)
                    softlogicCustomer[0] = true;
                checkAndAddOffers(offers, abansCustomer, singerCustomer, softlogicCustomer);
            }
        });
    }

    private void checkAndAddOffers(ArrayList<Offer> offers, boolean[] abansCustomer, boolean[] singerCustomer, boolean[] softlogicCustomer) {
        offerDone++;
        if (offerDone > 2) {
            for (Offer offer : offers) {
                if (offer.getGeneralAudience()) {
                    customerNotificationsData.add(offer);
                } else {
                    switch (offer.getMerchantName()) {
                        case "Abans":
                            if (abansCustomer[0]) {
                                customerNotificationsData.add(offer);
                            }
                            break;

                        case "Singer":
                            if (singerCustomer[0]) {
                                customerNotificationsData.add(offer);
                            }
                            break;

                        case "Softlogic Holdings PLC":
                            if (softlogicCustomer[0]) {
                                customerNotificationsData.add(offer);
                            }
                            break;
                    }
                }
            }

            checkAndSetRecycler();
        }
    }

    private void checkAndSetRecycler() {
        done++;
        if (done > 3) {
            Collections.sort(customerNotificationsData, Collections.<DateComparator>reverseOrder());
            addHolderFromData();
            setUpNotificationsRecycler();
            progressDialog.dismiss();
        }
    }

    private void addHolderFromData() {
        for (DateComparator dateComparator : customerNotificationsData) {

            String title = "";
            String customerName = "";
            String message = "Tap to view";
            String type = dateComparator.getType();
            String date = dateComparator.getDate();

            if (type.equals("Product Review")) {
                ProductReview productReview = (ProductReview) dateComparator;
                title = "Review Published - " + productReview.getMerchantName();
                message = "Your review for " + productReview.getProductName() + " has been published";
            } else if (type.equals("Merchant Review")) {
                MerchantReview merchantReview = (MerchantReview) dateComparator;
                title = "You have rated the " + merchantReview.getMerchantName();
                message = "Merchant rating for " + merchantReview.getMerchantName() + " has been published";
            } else if (type.equals("Product Review Chat")) {
                ProductReviewChat productReviewChat = (ProductReviewChat) dateComparator;
                title = "Re:Review";
                message = productReviewChat.getMerchantName() + " has replied to your review for " + productReviewChat.getProductName();
            } else if (type.equals("Offer")) {
                Offer offer = (Offer) dateComparator;
                title = offer.getOfferTitle();
                message = "You've received an offer from " + offer.getMerchantName();
            }

            customerNotificationsHolder.add(new CustomerNotification(customerName, title, message, type, date));
        }
    }

    private void setUpNotificationsRecycler() {
        rvNotifications.setVisibility(View.VISIBLE);
        adapter = new MerchantNotificationsAdapter(customerNotificationsHolder, customerNotificationsData, null, customerHome);
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNotifications.setAdapter(adapter);

        if (customerNotificationsHolder.isEmpty()) {
            rvNotifications.setVisibility(View.GONE);
        }
    }
}
