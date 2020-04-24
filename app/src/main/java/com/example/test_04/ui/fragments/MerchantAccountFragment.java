package com.example.test_04.ui.fragments;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test_04.R;
import com.example.test_04.models.CurrentMerchant;
import com.example.test_04.ui.MerchantHome;
import com.example.test_04.utils.ReviewUtils;
import com.example.test_04.utils.StringUtils;
import com.example.test_04.utils.SwitchUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 */
public class MerchantAccountFragment extends Fragment {

    private MerchantHome merchantHome;
    private ProgressDialog progressDialog;
    private InputMethodManager imm;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String merchantDesc = "";
    private String products = "";
    private ReviewUtils reviewUtils;

    private RoundedImageView rivMerchantLogo;
    private TextView tvMerchantName;
    private TextView tvMerchantDesc;
    private TextView tvProducts;
    private ImageView ivMoreDesc;
    private ImageView ivMoreProducts;
    private EditText etMerchantDesc;
    private EditText etProducts;
    private LinearLayout llMerchantDesc;
    private LinearLayout llProducts;
    private TextView tvDoneMerchantDesc;
    private TextView tvDoneProducts;


    public MerchantAccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_merchant_account, container, false);
        reviewUtils = new ReviewUtils(view, false, false);

        merchantHome = (MerchantHome) getActivity();

        findViews(view);

        init();

        return view;
    }

    private void init() {
        imm = (InputMethodManager) merchantHome.getSystemService(INPUT_METHOD_SERVICE);
        setListeners();
        setViewValues();
    }

    private void setListeners() {
        ivMoreDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(merchantHome, ivMoreDesc);
                popupMenu.inflate(R.menu.menu_edit);
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()) {
                            case R.id.edit:
                                editField(tvMerchantDesc, etMerchantDesc, llMerchantDesc);
                                break;
                        }

                        return true;
                    }
                });
            }
        });

        ivMoreProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(merchantHome, ivMoreProducts);
                popupMenu.inflate(R.menu.menu_edit);
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()) {
                            case R.id.edit:
                                editField(tvProducts, etProducts, llProducts);
                                break;
                        }

                        return true;
                    }
                });
            }
        });

        tvDoneProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llProducts.setVisibility(View.GONE);
                tvProducts.setVisibility(View.VISIBLE);
                String newText = etProducts.getText().toString();
                if (!newText.isEmpty() && !newText.equals(tvProducts.getText())) {
                    tvProducts.setText(newText);

                    updateMerchant("Products", newText);
                    CurrentMerchant.products = newText;

                }

                imm.hideSoftInputFromWindow(etProducts.getWindowToken(), 0);
            }
        });

        tvDoneMerchantDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llMerchantDesc.setVisibility(View.GONE);
                tvMerchantDesc.setVisibility(View.VISIBLE);
                String newText = etMerchantDesc.getText().toString();
                if (!newText.isEmpty() && !newText.equals(tvMerchantDesc.getText())) {
                    tvMerchantDesc.setText(newText);

                    updateMerchant("Merchant Description", newText);
                    CurrentMerchant.description = newText;

                }

                imm.hideSoftInputFromWindow(etMerchantDesc.getWindowToken(), 0);
            }
        });
    }

    private void updateMerchant(final String key, final String value) {

        showProgressDialog("Updating profile");

        db.collection("Merchants")
                .whereEqualTo("Merchant Name", CurrentMerchant.name)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            Map<String, Object> merchantData = new HashMap<>();
                            merchantData.put(key, value);

                            db.collection("Merchants")
                                    .document(task.getResult().getDocuments().get(0).getId())
                                    .update(merchantData)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(merchantHome, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                                setMerchantDetail(key, value);

                                            } else {
                                                Toast.makeText(merchantHome, "Profile update failed", Toast.LENGTH_SHORT).show();
                                            }

                                            progressDialog.dismiss();
                                        }
                                    });

                        } else {
                            Toast.makeText(merchantHome, "Profile update failed", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });


    }

    private void setMerchantDetail(String key, String value) {
        SharedPreferences.Editor sp = merchantHome.getSharedPreferences("Merchant", merchantHome.MODE_PRIVATE).edit();
        sp.putString(key, value);
        sp.apply();
    }

    private void editField(TextView tv, EditText et, LinearLayout ll) {
        tv.setVisibility(View.GONE);
        ll.setVisibility(View.VISIBLE);
        et.setText(tv.getText());

        et.requestFocus();
        imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
    }

    private void showProgressDialog(String title) {
        progressDialog = new ProgressDialog(merchantHome);
        progressDialog.setTitle(title);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void setViewValues() {
        products = CurrentMerchant.products;
        merchantDesc = CurrentMerchant.description;
        String name = getMerchantDetail("Merchant Name");
        tvMerchantName.setText(name);
        rivMerchantLogo.setImageResource(SwitchUtils.getMerchantImgId(name));
        tvMerchantDesc.setText(merchantDesc);
        tvProducts.setText(products);

        setMerchantDescriptionRatingAndProducts();

    }

    private void setMerchantDescriptionRatingAndProducts() {

        showProgressDialog("Loading profile");

        db.collection("Merchants")
                .whereEqualTo("Merchant Name", CurrentMerchant.name)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                            String products = documentSnapshot.get("Products").toString();
                            String merchantDesc = documentSnapshot.get("Merchant Description").toString();
                            int merchantRating = (int) Math.rint(Double.valueOf(task.getResult().getDocuments().get(0).getDouble("Merchant Rating")));
                            products = StringUtils.addLineBreaks(products);

                            MerchantAccountFragment.this.products = products;
                            MerchantAccountFragment.this.merchantDesc = merchantDesc;

                            tvMerchantDesc.setText(merchantDesc);
                            tvProducts.setText(products);
                            reviewUtils.fillStars(merchantRating);

                            setMerchantDetail("Merchant Rating", String.valueOf(merchantRating));
                            setMerchantDetail("Merchant Description", merchantDesc);
                            setMerchantDetail("Products", products);

                            CurrentMerchant.description = merchantDesc;
                            CurrentMerchant.products = products;
                            CurrentMerchant.rating = String.valueOf(merchantRating);

                        } else {
                            Toast.makeText(merchantHome, "Couldn't load merchant details", Toast.LENGTH_SHORT).show();
                            merchantHome.onBackPressed();
                        }

                        progressDialog.dismiss();
                    }
                });

    }

    public String getMerchantDetail(String key) {
        SharedPreferences sp = getActivity().getSharedPreferences("Merchant", getActivity().MODE_PRIVATE);
        return sp.getString(key, null);
    }

    private void findViews(View view) {
        rivMerchantLogo = view.findViewById(R.id.riv_merchant_logo);
        tvMerchantName = view.findViewById(R.id.tv_merchant_name);
        tvMerchantDesc = view.findViewById(R.id.tv_merchant_desc);
        etMerchantDesc = view.findViewById(R.id.et_merchant_desc);
        tvProducts = view.findViewById(R.id.tv_products);
        etProducts = view.findViewById(R.id.et_products);
        ivMoreDesc = view.findViewById(R.id.iv_more_desc);
        ivMoreProducts = view.findViewById(R.id.iv_more_products);
        llMerchantDesc = view.findViewById(R.id.ll_merchant_desc);
        llProducts = view.findViewById(R.id.ll_products);
        tvDoneMerchantDesc = view.findViewById(R.id.tv_done_merchant_desc);
        tvDoneProducts = view.findViewById(R.id.tv_done_products);

    }

    @Override
    public void onResume() {
        super.onResume();

        merchantHome.onMerchantAccountResume();

    }

    @Override
    public void onPause() {
        super.onPause();

        merchantHome.onMerchantAccountPause();
    }
}
