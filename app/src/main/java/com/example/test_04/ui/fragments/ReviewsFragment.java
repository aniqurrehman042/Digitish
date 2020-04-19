package com.example.test_04.ui.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_04.R;
import com.example.test_04.adapters.ReviewsAdapter;
import com.example.test_04.db_callbacks.IGetProductReviews;
import com.example.test_04.models.CurrentCustomer;
import com.example.test_04.models.ProductReview;
import com.example.test_04.ui.CustomerHome;
import com.example.test_04.utils.DBUtils;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class ReviewsFragment extends Fragment {

    private RecyclerView rvReviews;
    private LinearLayout llReviews;

    private ArrayList<ProductReview> productReviews = new ArrayList<>();
    private ReviewsAdapter adapter;
    private ProgressDialog progressDialog;

    public ReviewsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reviews, container, false);

        findViews(view);

        init();

        return view;
    }

    private void init() {
        llReviews.setVisibility(View.VISIBLE);
        checkQr();
    }

    private void checkQr() {
        withoutScan();
    }

    private void showProgressDialog(String title) {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle(title);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void withoutScan() {
        showProgressDialog("Loading reviews");

        productReviews = new ArrayList<>();

        DBUtils.getProductReviews(CurrentCustomer.email, new IGetProductReviews() {
            @Override
            public void onCallback(boolean successful, @Nullable ArrayList<ProductReview> productReviews) {
                progressDialog.dismiss();
                if (successful) {
                    ReviewsFragment.this.productReviews = productReviews;
                    if (productReviews.size() > 0)
                        setUpReviewsRecycler();
                    else
                        llReviews.setVisibility(View.GONE);
                } else {
                    Toast.makeText(getContext(), "Couldn't load reviews", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void setUpReviewsRecycler() {
        adapter = new ReviewsAdapter(productReviews, getFragmentManager(), (CustomerHome) getActivity());
        rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        rvReviews.setAdapter(adapter);
    }

    private void findViews(View view) {
        rvReviews = view.findViewById(R.id.rv_reviews);
        llReviews = view.findViewById(R.id.ll_reviews);
    }

    @Override
    public void onResume() {
        super.onResume();

        ((TextView) getActivity().findViewById(R.id.tv_title)).setText("My Reviews");

    }

    @Override
    public void onPause() {
        super.onPause();
        progressDialog.dismiss();
    }
}
