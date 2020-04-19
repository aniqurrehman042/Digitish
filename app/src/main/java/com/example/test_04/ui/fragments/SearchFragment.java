package com.example.test_04.ui.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.test_04.R;
import com.example.test_04.adapters.ResultAdapter;
import com.example.test_04.adapters.SearchReviewsAdapter;
import com.example.test_04.adapters.FilterAdapter;
import com.example.test_04.models.Filter;
import com.example.test_04.models.Merchant;
import com.example.test_04.models.ProductReview;
import com.example.test_04.ui.CustomerHome;
import com.example.test_04.utils.DateUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {

    private RecyclerView rvSelect;
    private RecyclerView rvResults;
    private EditText etSearch;

    private CustomerHome customerHome;
    private ArrayList<Merchant> merchants = new ArrayList<>();
    private ArrayList<ProductReview> productReviews = new ArrayList<>();
    private ArrayList<Filter> filters = new ArrayList<>();
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;
    private SearchReviewsAdapter searchReviewsAdapter;
    private FilterAdapter filterAdapter;
    private LinearLayoutManager selectLayoutManager;
    private LinearLayoutManager reviewsLayoutManager;
    private int reviewsLoaded = 0;
    private boolean reviewsLoadingCompleted = true;

    public SearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        customerHome = (CustomerHome) getActivity();
        db = FirebaseFirestore.getInstance();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        findViews(view);

        setListeners();

        if (getArguments() != null) {
            boolean firstRun = getArguments().getBoolean("First Run", false);
            if (firstRun) {
                filters = new ArrayList<>();
                productReviews = new ArrayList<>();
                filterAdapter = null;
                searchReviewsAdapter = null;
            }
        }

        setArguments(null);

        if (filters.isEmpty())
            setUpFilters();
        setUpFilterRecycler();
        if (merchants.isEmpty())
            getMerchants();
        else if (!productReviews.isEmpty() && filterAdapter.getSelectedFilter() != 0) {
            setUpReviewsRecycler();
        } else {
            setUpMerchantsRecycler();
        }

        return view;
    }

    private void setUpReviewsRecycler() {
        String qrId = "";
        if (!productReviews.isEmpty())
            productReviews.get(0).getQrId();

        if (searchReviewsAdapter == null)
            searchReviewsAdapter = new SearchReviewsAdapter(productReviews, customerHome, null);
        else
            searchReviewsAdapter.notifyDataSetChanged();
        reviewsLayoutManager = new LinearLayoutManager(customerHome);
        rvResults.setLayoutManager(reviewsLayoutManager);
        rvResults.setAdapter(searchReviewsAdapter);
    }

    private void getMerchants() {

        showProgressDialog("Loading Search Results");

        db.collection("Merchants")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult().getDocuments()) {
                                String merchantName = document.get("Merchant Name").toString();
                                String merchantDescription = document.get("Merchant Description").toString();
                                String email = document.get("Email").toString();
                                String merchantRating = document.get("Merchant Rating").toString();
                                String products = document.get("Products").toString();
                                String website = document.get("Website").toString();
                                Merchant merchant = new Merchant(merchantName, merchantDescription, email, merchantRating, products, website);
                                merchants.add(merchant);
                            }

                            setUpMerchantsRecycler();

                        } else {
                            Toast.makeText(customerHome, "Failed to load search results", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showProgressDialog(String title) {
        progressDialog = new ProgressDialog(customerHome);
        progressDialog.setTitle(title);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void setUpMerchantsRecycler() {
        ResultAdapter resultsAdapter = new ResultAdapter(customerHome, merchants);
        rvResults.setLayoutManager(new LinearLayoutManager(getContext()));
        rvResults.setAdapter(resultsAdapter);
        progressDialog.dismiss();
    }

    private void setUpFilters() {
        String[] filterNames = new String[]{"Merchants", "Reviews", "5-Star Reviews", "4-Star Reviews", "3-Star Reviews", "2-Star Reviews", "1-Star Reviews"};

        filters = new ArrayList<>();

        for (String filterName : filterNames) {
            filters.add(new Filter(filterName, false));
        }

        filters.get(0).setSelected(true);
    }

    private void setUpFilterRecycler() {

        if (filterAdapter == null)
            filterAdapter = new FilterAdapter(filters, SearchFragment.this);
        selectLayoutManager = new LinearLayoutManager(getContext());
        selectLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvSelect.setLayoutManager(selectLayoutManager);
        rvSelect.setAdapter(filterAdapter);
    }

    private void setListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String searchPhrase = s.toString().trim();

                if (searchPhrase.equalsIgnoreCase("Television") || searchPhrase.equalsIgnoreCase("Televisions") || searchPhrase.equalsIgnoreCase("TV") || searchPhrase.equalsIgnoreCase("TVs")) {
                    filterAdapter.swapSelectedFilter(1);
                    getReviews("Product Category", "Televisions");
                } else if (searchPhrase.equalsIgnoreCase("Washing Machines") || searchPhrase.equalsIgnoreCase("Washing Machine")) {
                    filterAdapter.swapSelectedFilter(1);
                    getReviews("Product Category", "Mashing Machines");
                } else if (searchPhrase.equalsIgnoreCase("Microwaves") || searchPhrase.equalsIgnoreCase("Microwave")) {
                    filterAdapter.swapSelectedFilter(1);
                    getReviews("Product Category", "Microwaves");
                } else if (searchPhrase.equalsIgnoreCase("Refrigerator") || searchPhrase.equalsIgnoreCase("Refrigerators")) {
                    filterAdapter.swapSelectedFilter(1);
                    getReviews("Product Category", "Refrigerators");
                } else if (searchPhrase.trim().equals("")) {
                    setUpMerchantsRecycler();
                    filterAdapter.swapSelectedFilter(0);
                } else {

                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void getReviews(final String key, String value) {

        if (!reviewsLoadingCompleted)
            return;

        reviewsLoadingCompleted = false;

        reviewsLoaded = 0;
        productReviews.clear();

        showProgressDialog("Loading reviews");

        Query query = db.collection("Product Reviews");

        query = query.whereEqualTo("Reviewed", true);

        if (!key.isEmpty()) {
            if (key.equals("Product Rating"))
                query = query.whereEqualTo(key, Long.valueOf(value));
            else
                query = query.whereEqualTo(key, value);
        }

        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {

                            for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                                String merchantName = documentSnapshot.get("Merchant Name").toString();
                                String customerName = documentSnapshot.get("Customer Name").toString();
                                String customerEmail = documentSnapshot.get("Customer Email").toString();
                                String productCode = documentSnapshot.get("Product Code").toString();
                                String productName = documentSnapshot.get("Product Name").toString();
                                int productRating = Integer.valueOf(String.valueOf(documentSnapshot.get("Product Rating")));
                                String reviewDescription = documentSnapshot.get("Review Description").toString();
                                String reviewTitle = documentSnapshot.get("Review Title").toString();
                                String qrId = documentSnapshot.get("QR Id").toString();
                                String productCategory = documentSnapshot.get("Product Category").toString();
                                Timestamp date = documentSnapshot.getTimestamp("Date");
                                String dateString = DateUtils.dateToString(date.toDate());
                                boolean completed = (boolean) documentSnapshot.get("Completed");
                                boolean reviewed = (boolean) documentSnapshot.get("Reviewed");
                                ProductReview productReview = new ProductReview(customerEmail, customerName, merchantName, productCode, productName, productCategory, productRating, reviewDescription, reviewTitle, qrId, completed, reviewed, dateString);
                                productReviews.add(productReview);
                            }

                            if (!productReviews.isEmpty()) {
                                checkAndSetRecycler();
                            } else {
                                Toast.makeText(customerHome, "No reviews found", Toast.LENGTH_SHORT).show();
                                checkAndSetRecycler();
                            }

                        } else {
                            Toast.makeText(customerHome, "Couldn't load reviews", Toast.LENGTH_SHORT).show();
                        }

                        progressDialog.dismiss();
                        reviewsLoadingCompleted = true;
                    }
                });

    }

    private void checkAndSetRecycler() {
        reviewsLoaded++;

        if (reviewsLoaded > 0) {
            setUpReviewsRecycler();
        }
    }

    private void findViews(View view) {
        rvSelect = view.findViewById(R.id.rv_select);
        rvResults = view.findViewById(R.id.rv_results);
        etSearch = getActivity().findViewById(R.id.et_search);
    }

    @Override
    public void onResume() {
        super.onResume();

        customerHome.onSearchFragmentResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        customerHome.onSearchFragmentPause();
    }
}
