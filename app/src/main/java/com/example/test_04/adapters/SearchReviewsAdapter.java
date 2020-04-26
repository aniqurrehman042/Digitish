package com.example.test_04.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_04.R;
import com.example.test_04.models.CurrentCustomer;
import com.example.test_04.models.Customer;
import com.example.test_04.models.Merchant;
import com.example.test_04.models.ProductReview;
import com.example.test_04.models.ProductReviewChat;
import com.example.test_04.ui.CustomerHome;
import com.example.test_04.ui.MerchantHome;
import com.example.test_04.utils.SwitchUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SearchReviewsAdapter extends RecyclerView.Adapter<SearchReviewsAdapter.ViewHolder> {
    private final ArrayList<ProductReview> productReviews;
    private CustomerHome customerHome;
    private MerchantHome merchantHome;
    private int done = 0;
    private Merchant merchant;
    private Customer customer;
    private ArrayList<ProductReviewChat> productReviewChats = new ArrayList<>();
    private ProgressDialog progressDialog;
    private Context context;

    public SearchReviewsAdapter(ArrayList<ProductReview> productReviews, CustomerHome customerHome, MerchantHome merchantHome) {
        this.productReviews = productReviews;
        this.customerHome = customerHome;
        this.merchantHome = merchantHome;

        if (customerHome == null)
            context = merchantHome;
        else
            context = customerHome;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_search_review_item, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.clMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getChatBetween(productReviews.get(position));
            }
        });

        holder.tvCustomerName.setText(productReviews.get(position).getReviewTitle());
        holder.tvProductName.setText(productReviews.get(position).getProductName());
        holder.tvProductCode.setText(productReviews.get(position).getProductCode());
        holder.tvRating.setText(String.valueOf(productReviews.get(position).getProductRating()));
        holder.tvDate.setText(productReviews.get(position).getDate());

        holder.ivProductImg.setImageResource(SwitchUtils.getProductImgId(productReviews.get(position).getProductCategory()));

    }

    @Override
    public int getItemCount() {
        return productReviews.size();
    }

    private void getChatBetween(final ProductReview productReview) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        done = 0;

        showProgressDialog("Loading review");

        productReviewChats = new ArrayList<>();

        db.collection("ProductReviewChat")
                .whereEqualTo("Merchant Name", productReview.getMerchantName())
                .whereEqualTo("Customer Email", productReview.getCustomerEmail())
                .whereEqualTo("QR Id", productReview.getQrId())
                .whereEqualTo("Product Code", productReview.getProductCode())
                .orderBy("Date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult().getDocuments()) {
                                String customerName = document.get("Customer Name").toString();
                                String productCode = document.get("Product Code").toString();
                                String qrId = document.get("QR Id").toString();
                                String image = document.get("Image").toString();
                                String message = document.get("Message").toString();
                                String sender = document.get("Sender").toString();
                                String customerProfilePic = document.get("Customer Profile Picture").toString();
                                Timestamp timestamp = (Timestamp) document.get("Date");
                                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy - HH:mm");
                                Date dateObj = timestamp.toDate();
                                String date = sdf.format(dateObj);
                                ProductReviewChat productReviewChat = new ProductReviewChat(productReview.getCustomerEmail(), customerName, productReview.getMerchantName(), productCode, qrId, image, message, sender, date, customerProfilePic);
                                productReviewChats.add(productReviewChat);
                            }

                        } else {
                            Toast.makeText(context, "Couldn't load chat", Toast.LENGTH_SHORT).show();
                        }

                        checkAndStartFragment(productReview.getProductCategory(), productReview);
                    }
                });

        if (customerHome != null) {

            db.collection("Merchants")
                    .whereEqualTo("Merchant Name", productReview.getMerchantName())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                String merchantDesc = documentSnapshot.get("Merchant Description").toString();
                                String merchantEmail = documentSnapshot.get("Email").toString();
                                String merchantRating = documentSnapshot.get("Merchant Rating").toString();
                                String merchantProducts = documentSnapshot.get("Products").toString();
                                String website = documentSnapshot.get("Website").toString();
                                merchant = new Merchant(productReview.getMerchantName(), merchantDesc, merchantEmail, merchantRating, merchantProducts, website);

                            } else
                                Toast.makeText(context, "Couldn't load chat", Toast.LENGTH_SHORT).show();

                            checkAndStartFragment(productReview.getProductCategory(), productReview);
                        }
                    });
        } else {

            db.collection("Customers")
                    .whereEqualTo("Email", productReview.getCustomerEmail())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                String customerName = documentSnapshot.get("Customer Name").toString();
                                Object phoneObj = documentSnapshot.get("Phone");
                                String phone = "";
                                if (phoneObj != null)
                                    phone = phoneObj.toString();
                                String points = documentSnapshot.getLong("Points").toString();
                                String profilePicture = documentSnapshot.get("Profile Picture").toString();
                                customer = new Customer(productReview.getCustomerEmail(), customerName, phone, points, profilePicture);

                            } else
                                Toast.makeText(context, "Couldn't load chat", Toast.LENGTH_SHORT).show();

                            checkAndStartFragment(productReview.getProductCategory(), productReview);
                        }
                    });
        }
    }

    private void checkAndStartFragment(String productCategory, ProductReview productReview) {
        done++;
        if (done > 1) {
            progressDialog.dismiss();
            if (customerHome != null)
                customerHome.startProductReviewChatFragment(productReviewChats, merchant, productReview, true);
            else
                merchantHome.startProductReviewChatFragment(productReviewChats, productReview, customer);
        }
    }

    private void showProgressDialog(String title) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(title);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout clMain;
        TextView tvProductName;
        TextView tvProductCode;
        ImageView ivProductImg;
        TextView tvReply;
        ImageView ivRightArrow;
        TextView tvCustomerName;
        TextView tvDate;
        TextView tvRating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            clMain = itemView.findViewById(R.id.cl_main);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductCode = itemView.findViewById(R.id.tv_product_code);
            ivProductImg = itemView.findViewById(R.id.iv_product_img);
            tvReply = itemView.findViewById(R.id.tv_reply);
            ivRightArrow = itemView.findViewById(R.id.iv_right_arrow);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvRating = itemView.findViewById(R.id.tv_rating);
        }
    }
}
