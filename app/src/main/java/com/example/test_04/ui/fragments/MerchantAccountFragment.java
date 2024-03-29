package com.example.test_04.ui.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test_04.R;
import com.example.test_04.adapters.ProductsAdapter;
import com.example.test_04.models.CurrentMerchant;
import com.example.test_04.models.Merchant;
import com.example.test_04.models.Product;
import com.example.test_04.ui.MerchantHome;
import com.example.test_04.utils.DateUtils;
import com.example.test_04.utils.FCMUtils;
import com.example.test_04.utils.ReviewUtils;
import com.example.test_04.utils.StringUtils;
import com.example.test_04.utils.SwitchUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;

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
    private Dialog dialog;

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
    private Button btnCreateOffer;
    private EditText etDate;
    private Calendar myCalendar = Calendar.getInstance();
    private RecyclerView rvProducts;

    private DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }

    };

    private void updateLabel() {
        etDate.setText(DateUtils.dateToOfferString(myCalendar.getTime()));
    }

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
        setUpProductsRecycler();
    }

    private void setUpProductsRecycler() {
        ArrayList<Product> products = new ArrayList<>();
        products.add(new Product(null, "Washing Machine", "Washing Machines", null));
        products.add(new Product(null, "Television", "Televisions", null));
        products.add(new Product(null, "Microwave", "Microwaves", null));
        products.add(new Product(null, "Refrigerator", "Refrigerators", null));
        Merchant merchant = new Merchant(CurrentMerchant.name, CurrentMerchant.description, CurrentMerchant.email, CurrentMerchant.rating, CurrentMerchant.products, CurrentMerchant.website);
        ProductsAdapter adapter = new ProductsAdapter(merchantHome, products, merchant);
        LinearLayoutManager lm = new LinearLayoutManager(merchantHome);
        lm.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvProducts.setAdapter(adapter);
        rvProducts.setLayoutManager(lm);
    }

    private void setListeners() {
        ivMoreDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                PopupMenu popupMenu = new PopupMenu(merchantHome, ivMoreDesc);
//                popupMenu.inflate(R.menu.menu_edit);
//                popupMenu.show();
//                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                    @Override
//                    public boolean onMenuItemClick(MenuItem item) {
//
//                        switch (item.getItemId()) {
//                            case R.id.edit:
//                                editField(tvMerchantDesc, etMerchantDesc, llMerchantDesc);
//                                break;
//                        }
//
//                        return true;
//                    }
//                });

                LayoutInflater inflater = (LayoutInflater)
                        merchantHome.getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.layout_textview_menu, null);

                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
                popupWindow.setContentView(popupView);
                popupWindow.showAsDropDown(v, -50, 0);

                popupView.findViewById(R.id.ll_menu).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editField(tvMerchantDesc, etMerchantDesc, llMerchantDesc);
                        popupWindow.dismiss();
                    }
                });

                ((TextView)popupView.findViewById(R.id.tv_menu)).setText("Edit");
            }
        });

        ivMoreProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                PopupMenu popupMenu = new PopupMenu(merchantHome, ivMoreProducts);
//                popupMenu.inflate(R.menu.menu_edit);
//                popupMenu.show();
//                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                    @Override
//                    public boolean onMenuItemClick(MenuItem item) {
//
//                        switch (item.getItemId()) {
//                            case R.id.edit:
//                                editField(tvProducts, etProducts, llProducts);
//                                break;
//                        }
//
//                        return true;
//                    }
//                });

                LayoutInflater inflater = (LayoutInflater)
                        merchantHome.getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.layout_textview_menu, null);

                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
                popupWindow.setContentView(popupView);
                popupWindow.showAsDropDown(v, -50, 0);

                popupView.findViewById(R.id.ll_menu).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editField(tvProducts, etProducts, llProducts);
                        popupWindow.dismiss();
                    }
                });

                ((TextView)popupView.findViewById(R.id.tv_menu)).setText("Edit");
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

        btnCreateOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupOfferDialog();
            }
        });
    }

    private void setupOfferDialog() {
        dialog = new Dialog(merchantHome);
        dialog.setContentView(R.layout.layout_create_offer);
        dialog.show();

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(params);

        final EditText etOfferTitle = dialog.findViewById(R.id.et_offer_title);
        final EditText etDesc = dialog.findViewById(R.id.et_desc);
        etDate = dialog.findViewById(R.id.et_date);
        etDate.setInputType(InputType.TYPE_NULL);
        Spinner spnAudience = dialog.findViewById(R.id.spn_audience);
        TextView tvSend = dialog.findViewById(R.id.tv_send);
        TextView tvCancel = dialog.findViewById(R.id.tv_cancel);
        final TextView tvTitleLen = dialog.findViewById(R.id.tv_title_len);
        final TextView tvDescLen = dialog.findViewById(R.id.tv_desc_len);

        setOfferDialogListeners(etOfferTitle, etDesc, tvTitleLen, tvDescLen, tvSend, tvCancel, etDate, spnAudience);
        setSpinnerAdapter(spnAudience);

    }

    private void setSpinnerAdapter(Spinner spnAudience) {
        String[] audiences = {"General Audience", "Existing Customers"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(merchantHome, R.layout.layout_offer_spinner_textview, audiences);
        spnAudience.setAdapter(adapter);
    }

    private void setOfferDialogListeners(final EditText etOfferTitle, final EditText etDesc, final TextView tvTitleLen, final TextView tvDescLen, TextView tvSend, TextView tvCancel, final EditText etDate, final Spinner spnAudience) {
        etOfferTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvTitleLen.setText(String.valueOf(etOfferTitle.getText().toString().length()));
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

        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(merchantHome, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        tvSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = etOfferTitle.getText().toString();
                String desc = etDesc.getText().toString();
                String date = etDate.getText().toString();
                String audience = spnAudience.getSelectedItem().toString();

                if (title.isEmpty() || desc.isEmpty() || date.isEmpty()) {
                    Toast.makeText(merchantHome, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                uploadOfferAndSendNotification(title, desc, date, audience);
                dialog.dismiss();
            }
        });

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void uploadOfferAndSendNotification(final String title, String desc, String date, final String audience) {

        boolean generalAudience = true;

        if (audience.equals("Existing Customers"))
            generalAudience = false;

        Map<String, Object> offerData = new HashMap<>();
        offerData.put("Offer Title", title);
        offerData.put("Offer Description", desc);
        offerData.put("Valid Till", DateUtils.stringToOfferDate(date));
        offerData.put("Date", Timestamp.now());
        offerData.put("General Audience", generalAudience);
        offerData.put("Merchant Name", CurrentMerchant.name);

        db.collection("Offers")
                .add(offerData)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            FCMUtils.Companion.sendMessage(merchantHome, true, false, title, "You have received an offer from " + CurrentMerchant.name, audience, CurrentMerchant.name);
                            Toast.makeText(merchantHome, "Your offer has been sent", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(merchantHome, "Couldn't send offer", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void updateMerchant(final String key, final String value) {

        if (progressDialog != null)
            progressDialog.dismiss();

        showProgressDialog("Updating Profile");

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

        if (progressDialog != null)
            progressDialog.dismiss();

        showProgressDialog("Loading Profile");

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
        btnCreateOffer = view.findViewById(R.id.btn_create_offer);
        rvProducts = view.findViewById(R.id.rv_products);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressDialog != null)
            progressDialog.dismiss();
    }
}
