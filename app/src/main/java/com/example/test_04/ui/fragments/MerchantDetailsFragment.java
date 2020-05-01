package com.example.test_04.ui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test_04.R;
import com.example.test_04.adapters.ProductsAdapter;
import com.example.test_04.adapters.SuggestionsAdapter;
import com.example.test_04.models.Chat;
import com.example.test_04.models.CurrentCustomer;
import com.example.test_04.models.Merchant;
import com.example.test_04.models.Product;
import com.example.test_04.ui.CustomerHome;
import com.example.test_04.utils.ReviewUtils;
import com.example.test_04.utils.StringUtils;
import com.example.test_04.utils.SwitchUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.makeramen.roundedimageview.RoundedImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class MerchantDetailsFragment extends Fragment {

    private Button btnInquire;
    private RoundedImageView rivMerchantLogo;
    private TextView tvMerchantName;
    private TextView tvMerchantDesc;
    private TextView tvProducts;
    private RecyclerView rvProducts;

    private CustomerHome customerHome;
    private Merchant merchant;
    private ReviewUtils reviewUtils;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public MerchantDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_merchant_details, container, false);
        reviewUtils = new ReviewUtils(view, false, false);

        findViews(view);

        init();

        return view;
    }

    private void init() {
        customerHome = (CustomerHome) getActivity();
        merchant = (Merchant) getArguments().getSerializable("Merchant");
        customerHome.setPageTitle("Profile");

        setViewValues();
        setListeners();
        setUpProductsRecycler();
    }

    private void setUpProductsRecycler() {
        ArrayList<Product> products = new ArrayList<>();
        products.add(new Product(null, "Washing Machine", "Washing Machines", null));
        products.add(new Product(null, "TV", "Televisions", null));
        products.add(new Product(null, "Microwave", "Microwaves", null));
        products.add(new Product(null, "Refrigerator", "Refrigerators", null));
        ProductsAdapter adapter = new ProductsAdapter(customerHome, products);
        LinearLayoutManager lm = new LinearLayoutManager(customerHome);
        lm.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvProducts.setAdapter(adapter);
        rvProducts.setLayoutManager(lm);
    }

    private void setViewValues() {
        rivMerchantLogo.setImageResource(SwitchUtils.getMerchantImgId(merchant.getName()));
        tvProducts.setText(StringUtils.addLineBreaks(merchant.getProducts()));
        tvMerchantName.setText(merchant.getName());
        tvMerchantDesc.setText(merchant.getDescription());

        reviewUtils.fillStars((int) Math.rint(Double.parseDouble(merchant.getRating())));
    }

    private void setListeners() {
        btnInquire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getChatBetween(merchant.getEmail(), CurrentCustomer.email);
            }
        });
    }

    private void getChatBetween(final String merchantEmail, final String customerEmail) {
        db.collection("Chat")
                .whereEqualTo("Merchant Email", merchantEmail)
                .whereEqualTo("Customer Email", customerEmail)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<Chat> chats = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult().getDocuments()) {
                                String customerName = document.get("Customer Name").toString();
                                String merchantName = document.get("Merchant Name").toString();
                                String image = document.get("Image").toString();
                                String message = document.get("Message").toString();
                                String sender = document.get("Sender").toString();
                                boolean read = document.getBoolean("Read");
                                Timestamp timestamp = (Timestamp) document.get("Date");
                                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy - HH:mm");
                                Date dateObj = timestamp.toDate();
                                String date = sdf.format(dateObj);
                                Chat chat = new Chat(customerEmail, customerName, merchantEmail, merchantName, image, message, sender, read, date);
                                chats.add(chat);
                            }
                            customerHome.startChatFragment(chats, merchant);
                        } else {
                            Toast.makeText(customerHome, "Couldn't load chat", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void findViews(View view) {
        btnInquire = view.findViewById(R.id.btn_inquire);
        rivMerchantLogo = view.findViewById(R.id.riv_merchant_logo);
        tvMerchantName = view.findViewById(R.id.tv_merchant_name);
        tvMerchantDesc = view.findViewById(R.id.tv_merchant_desc);
        tvProducts = view.findViewById(R.id.tv_products);
        rvProducts = view.findViewById(R.id.rv_products);
    }

    @Override
    public void onResume() {
        super.onResume();
        customerHome.hideSearchIcons();
        customerHome.showBackBtn();
    }

    @Override
    public void onPause() {
        super.onPause();
        customerHome.showSearchIcons();
        customerHome.hideBackBtn();
    }
}
