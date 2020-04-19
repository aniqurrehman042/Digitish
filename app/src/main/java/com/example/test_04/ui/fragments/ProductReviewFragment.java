package com.example.test_04.ui.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test_04.R;
import com.example.test_04.db_callbacks.UpdatePointsCallback;
import com.example.test_04.models.CurrentCustomer;
import com.example.test_04.models.ProductReview;
import com.example.test_04.ui.CustomerHome;
import com.example.test_04.utils.DBUtils;
import com.example.test_04.utils.DateUtils;
import com.example.test_04.utils.FCMUtils;
import com.example.test_04.utils.PreferencesUtils;
import com.example.test_04.utils.ReviewUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProductReviewFragment extends Fragment {

    private CustomerHome customerHome;
    private TextView tvTitleLen;
    private TextView tvDescLen;
    private EditText etTitle;
    private EditText etDesc;
    private TextView tvProductCode;
    private Button btnSubmit;
    private Button btnClear;

    private ProductReview productReview;
    private FirebaseFirestore db;
    private String qrId;
    private ReviewUtils reviewUtils;
    private ProgressDialog progressDialog;
    private String reviewId;

    public ProductReviewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_product_review, container, false);

        findViews(view);
        setUpRatingSystem(view);

        init();

        return view;
    }

    private void init() {
        customerHome = (CustomerHome) getActivity();

        db = FirebaseFirestore.getInstance();
        getProductAndQrId();
        setListeners();
    }

    private void setUpRatingSystem(View view) {
        reviewUtils = new ReviewUtils(view, true, false);
    }

    private void setViewValues() {
        tvProductCode.setText(productReview.getProductCode());

        if (productReview.isReviewed()) {
            showProgressDialog("Loading Review");

            db.collection("Product Reviews")
                    .whereEqualTo("QR Id", qrId)
                    .whereEqualTo("Product Code", productReview.getProductCode())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult().getDocuments().size() > 0) {
                                    DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                    int productRating = Integer.valueOf(documentSnapshot.get("Product Rating").toString());
                                    String reviewTitle = documentSnapshot.get("Review Title").toString();
                                    String reviewDescription = documentSnapshot.get("Review Description").toString();
                                    reviewId = documentSnapshot.getId();

                                    reviewUtils.fillStars(productRating);
                                    etTitle.setText(reviewTitle);
                                    etDesc.setText(reviewDescription);

                                } else {
                                    Toast.makeText(customerHome, "Failed to load review. Please try again.", Toast.LENGTH_SHORT).show();
                                    customerHome.onBackPressed();
                                }
                            } else {
                                Toast.makeText(customerHome, "Failed to load review. Please try again.", Toast.LENGTH_SHORT).show();
                                customerHome.onBackPressed();
                            }

                            progressDialog.dismiss();
                        }
                    });
        }
    }

    private void showProgressDialog(String title) {
        progressDialog = new ProgressDialog(customerHome);
        progressDialog.setTitle(title);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void getProductAndQrId() {
        productReview = (ProductReview) getArguments().getSerializable("Product Review");
        qrId = getArguments().getString("QR Id");
    }

    private void setListeners() {
        etTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvTitleLen.setText(String.valueOf(etTitle.getText().toString().length()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etDesc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvDescLen.setText(String.valueOf(etDesc.getText().toString().length()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etTitle.setText("");
                etDesc.setText("");
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendReview();
            }
        });

    }

    private void sendReview() {

        String reviewDescription = etDesc.getText().toString();
        String reviewTitle = etTitle.getText().toString();
        int productRating = reviewUtils.getRating();
        if (productRating > 0) {
            ProductReview productReview = new ProductReview(CurrentCustomer.email, CurrentCustomer.name, this.productReview.getMerchantName(), this.productReview.getProductCode(), this.productReview.getProductName(), this.productReview.getProductCategory(), productRating, reviewDescription, reviewTitle, qrId, false, true, DateUtils.getCurrentDateInString());
            uploadReview(productReview);
        } else {
            Toast.makeText(customerHome, "Please select a rating.", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadReview(final ProductReview productReview) {

        showProgressDialog("Submitting Review");

        final Map<String, Object> productReviewData = new HashMap<>();
        productReviewData.put("Customer Email", productReview.getCustomerEmail());
        productReviewData.put("Customer Name", productReview.getCustomerName());
        productReviewData.put("Merchant Name", productReview.getMerchantName());
        productReviewData.put("Product Code", productReview.getProductCode());
        productReviewData.put("Product Name", productReview.getProductName());
        productReviewData.put("Product Category", productReview.getProductCategory());
        productReviewData.put("Product Rating", productReview.getProductRating());
        productReviewData.put("QR Id", productReview.getQrId());
        productReviewData.put("Review Description", productReview.getReviewDescription());
        productReviewData.put("Review Title", productReview.getReviewTitle());
        productReviewData.put("Reviewed", true);
        productReviewData.put("Completed", false);
        productReviewData.put("Date", Timestamp.now());

        db.collection("Product Reviews")
                .whereEqualTo("QR Id", qrId)
                .whereEqualTo("Product Code", this.productReview.getProductCode())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            String id = task.getResult().getDocuments().get(0).getId();

                            db.collection("Product Reviews")
                                    .document(id)
                                    .update(productReviewData)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                customerHome.onBackPressed();
                                            } else {
                                                Toast.makeText(customerHome, "Failed to send review. Please try again.", Toast.LENGTH_SHORT).show();
                                            }

                                            productReview.setReviewed(true);
                                            PreferencesUtils.saveCustomerPoints(customerHome, false);
                                            DBUtils.updatePoints(true, new UpdatePointsCallback() {
                                                @Override
                                                public void onCallback(@NotNull String result, boolean successful) {
                                                    Toast.makeText(customerHome, result, Toast.LENGTH_SHORT).show();
                                                    progressDialog.dismiss();
                                                }
                                            });
                                        }
                                    });

                            FCMUtils.Companion.sendMessage(customerHome, true, true, "Review", "Your product has been rated by " + CurrentCustomer.name, productReview.getMerchantName(), CurrentCustomer.email);

                        } else {
                            Toast.makeText(customerHome, "Failed to send review. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void findViews(View view) {
        tvTitleLen = view.findViewById(R.id.tv_title_len);
        tvDescLen = view.findViewById(R.id.tv_desc_len);
        etTitle = view.findViewById(R.id.et_title);
        etDesc = view.findViewById(R.id.et_desc);
        tvProductCode = view.findViewById(R.id.tv_product_code);
        btnClear = view.findViewById(R.id.btn_clear);
        btnSubmit = view.findViewById(R.id.btn_submit);
    }

    @Override
    public void onResume() {
        super.onResume();
        customerHome.onProductReviewResume(productReview.getProductName());
        setViewValues();

    }

    @Override
    public void onPause() {
        super.onPause();
        customerHome.hideBackBtn();
    }
}
