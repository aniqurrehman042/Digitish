package com.example.test_04.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_04.R;
import com.example.test_04.db_callbacks.UpdatePointsCallback;
import com.example.test_04.models.CurrentCustomer;
import com.example.test_04.models.CurrentMerchant;
import com.example.test_04.models.Merchant;
import com.example.test_04.models.MerchantReview;
import com.example.test_04.models.ProductReview;
import com.example.test_04.models.ProductReviewChat;
import com.example.test_04.ui.CustomerHome;
import com.example.test_04.ui.fragments.ProductReviewFragment;
import com.example.test_04.utils.DBUtils;
import com.example.test_04.utils.DateUtils;
import com.example.test_04.utils.FCMUtils;
import com.example.test_04.utils.PreferencesUtils;
import com.example.test_04.utils.ReviewUtils;
import com.example.test_04.utils.SwitchUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.makeramen.roundedimageview.RoundedImageView;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final ArrayList<ProductReview> productReviews;
    private final ArrayList<ProductReview> productsReviewsAbans = new ArrayList<>();
    private final ArrayList<ProductReview> productsReviewsSinger = new ArrayList<>();
    private final ArrayList<ProductReview> productsReviewsSoftlogic = new ArrayList<>();
    private ArrayList<ProductReviewChat> productReviewChats = new ArrayList<>();
    private FragmentManager fm;
    private ProductReviewFragment productReviewFragment = new ProductReviewFragment();
    private CustomerHome customerHome;
    private int done = 0;
    private Merchant merchant;
    private ProductReview productReview;
    private ProgressDialog progressDialog;
    private int merchantRating;
    private Dialog abansDialog;
    private Dialog singerDialog;
    private Dialog softlogicDialog;
    private ReviewUtils abansReviewUtils;
    private ReviewUtils singerReviewUtils;
    private ReviewUtils softlogicReviewUtils;
    private double[] perReviewAverage;
    private Context context;
    private int doneMerchant = 0;
    private Button btnRateMerchantAbans;
    private Button btnRateMerchantSinger;
    private Button btnRateMerchantSoftlogic;
    private String abansQrId;
    private String singerQrId;
    private String softlogicQrId;
    private boolean rateAbansDisabled = false;
    private boolean rateSingerDisabled = false;
    private boolean rateSoftlogicDisabled = false;

    public ReviewsAdapter(ArrayList<ProductReview> productReviews, FragmentManager fm, CustomerHome customerHome) {
        this.productReviews = productReviews;
        this.fm = fm;
        this.customerHome = customerHome;
        this.context = customerHome;

        segregateReviewsLists();

        setUpMerchantReviewSystem();
    }

    private void setUpMerchantReviewSystem() {
        showProgressDialog("Loading Review");

        doneMerchant = 0;

        initializeAbansDialog();
        initializeSingerDialog();
        initializeSoftlogicDialog();
        getMerchantRating();
        getMerchantReview();
    }

    private void segregateReviewsLists() {
        for (ProductReview productReview : productReviews) {
            switch (productReview.getMerchantName()) {
                case "Abans":
                    productsReviewsAbans.add(productReview);
                    break;
                case "Singer":
                    productsReviewsSinger.add(productReview);
                    break;
                case "Softlogic Holdings PLC":
                    productsReviewsSoftlogic.add(productReview);
                    break;
            }
        }

        productReviews.clear();
        productReviews.addAll(productsReviewsAbans);
        productReviews.addAll(productsReviewsSinger);
        productReviews.addAll(productsReviewsSoftlogic);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_review_item, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final ProductReview productReview = getCurrentItem(position);

        Button btnRateMerchant = holder.btnRateMerchant;
        TextView tvMerchantName = holder.tvMerchantName;

        btnRateMerchant.setVisibility(View.GONE);
        tvMerchantName.setVisibility(View.GONE);

        holder.clMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!productReview.isReviewed()) {
                    Bundle arguments = new Bundle();
                    arguments.putSerializable("Product Review", productReview);
                    arguments.putString("QR Id", productReview.getQrId());
                    productReviewFragment.setArguments(arguments);

                    fm
                            .beginTransaction()
                            .replace(R.id.cl_fragment, productReviewFragment)
                            .addToBackStack("Product Review")
                            .commit();
                } else {
                    String merchantName = productReview.getMerchantName();
                    String customerEmail = CurrentCustomer.email;
                    getChatBetween(merchantName, customerEmail, productReview);
                }
            }
        });

        holder.tvProductName.setText(productReview.getProductName());
        holder.tvProductCode.setText(productReview.getProductCode());

        if (productReview.isReviewed()) {
            holder.tvReply.setVisibility(View.VISIBLE);
            holder.ivRightArrow.setVisibility(View.GONE);
        } else {
            holder.tvReply.setVisibility(View.GONE);
            holder.ivRightArrow.setVisibility(View.VISIBLE);
        }

        holder.ivProductImg.setImageResource(SwitchUtils.getProductImgId(productReview.getProductCategory()));

        setMerchantNameAndRateMerchant(position, tvMerchantName, btnRateMerchant);

        if (position > productReviews.size() - 2) {
            setListeners();
        }

    }

    private void setMerchantNameAndRateMerchant(int position, TextView tvMerchantName, Button btnRateMerchant) {
        if (productsReviewsAbans.size() > 0 && position == productsReviewsAbans.size() - 1) {
            btnRateMerchant.setVisibility(View.VISIBLE);
            btnRateMerchantAbans = btnRateMerchant;
            if (rateAbansDisabled)
                disableAbansRateMerchant();
            else
                enableAbansRateMerchant();
        } else if (productsReviewsSinger.size() > 0 && (position == (productsReviewsAbans.size() + productsReviewsSinger.size() - 1))) {
            btnRateMerchant.setVisibility(View.VISIBLE);
            btnRateMerchantSinger = btnRateMerchant;
            if (rateSingerDisabled)
                disableSingerRateMerchant();
            else
                enableSingerRateMerchant();
        } else if (productsReviewsSoftlogic.size() > 0 && (position == (productsReviewsAbans.size() + productsReviewsSinger.size() + productsReviewsSoftlogic.size() - 1))) {
            btnRateMerchant.setVisibility(View.VISIBLE);
            btnRateMerchantSoftlogic = btnRateMerchant;
            if (rateSoftlogicDisabled)
                disableSoftlogicRateMerchant();
            else
                enableSoftlogicRateMerchant();
        }

        if (productsReviewsAbans.size() > 0 && position == 0) {
            tvMerchantName.setVisibility(View.VISIBLE);
            tvMerchantName.setText("Abans");
        } else if (productsReviewsSinger.size() > 0 && position == productsReviewsAbans.size()) {
            tvMerchantName.setVisibility(View.VISIBLE);
            tvMerchantName.setText("Singer");
        } else if (productsReviewsSoftlogic.size() > 0 && position == productsReviewsAbans.size() + productsReviewsSinger.size()) {
            tvMerchantName.setVisibility(View.VISIBLE);
            tvMerchantName.setText("Softlogic Holdings PLC");
        }
    }

    private ProductReview getCurrentItem(int position) {
        return productReviews.get(position);
    }

    @Override
    public int getItemCount() {
        return productsReviewsAbans.size() + productsReviewsSinger.size() + productsReviewsSoftlogic.size();
    }

    private void getChatBetween(final String merchantName, final String customerEmail, final ProductReview productReview) {
        showProgressDialog("Loading Review");

        done = 0;

        db.collection("ProductReviewChat")
                .whereEqualTo("Merchant Name", merchantName)
                .whereEqualTo("Customer Email", customerEmail)
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
                                Date dateObj = timestamp.toDate();
                                String date = DateUtils.dateToStringWithTime(dateObj);
                                ProductReviewChat productReviewChat = new ProductReviewChat(customerEmail, customerName, merchantName, productCode, qrId, image, message, sender, date, customerProfilePic);
                                productReviewChats.add(productReviewChat);
                            }

                        } else {
                            Toast.makeText(customerHome, "Couldn't load chat", Toast.LENGTH_SHORT).show();
                        }

                        checkAndStartFragment(productReview.getProductCategory());
                    }
                });

        db.collection("Merchants")
                .whereEqualTo("Merchant Name", merchantName)
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
                            merchant = new Merchant(merchantName, merchantDesc, merchantEmail, merchantRating, merchantProducts, website);

                        } else
                            Toast.makeText(customerHome, "Couldn't load chat", Toast.LENGTH_SHORT).show();

                        checkAndStartFragment(productReview.getProductCategory());
                    }
                });

        db.collection("Product Reviews")
                .whereEqualTo("QR Id", productReview.getQrId())
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
                                boolean completed = (boolean) documentSnapshot.get("Completed");
                                boolean reviewed = (boolean) documentSnapshot.get("Reviewed");
                                String id = documentSnapshot.getId();
                                String date = DateUtils.dateToStringWithTime(documentSnapshot.getTimestamp("Date").toDate());

                                ReviewsAdapter.this.productReview = productReview;
                                ReviewsAdapter.this.productReview.setId(id);

                            } else {
                                Toast.makeText(customerHome, "Failed to load review. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(customerHome, "Failed to load review. Please try again.", Toast.LENGTH_SHORT).show();
                        }

                        checkAndStartFragment(productReview.getProductCategory());
                    }
                });
    }

    private void checkAndStartFragment(String productCategory) {
        done++;
        if (done > 2) {
            progressDialog.dismiss();
            customerHome.startProductReviewChatFragment(productReviewChats, merchant, productReview, false);
        }
    }

    private void showProgressDialog(String title) {
        progressDialog = new ProgressDialog(customerHome);
        progressDialog.setTitle(title);
        progressDialog.setCancelable(false);
        if(!((Activity) customerHome).isFinishing())
        {
            progressDialog.show();
        }

    }

    private void getMerchantRating() {
        db.collection("Merchants")
                .whereEqualTo("Merchant Name", productReviews.get(0).getMerchantName())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            merchantRating = (int) (Math.rint(Double.valueOf(task.getResult().getDocuments().get(0).get("Merchant Rating").toString())));
                            abansReviewUtils.fillStars(merchantRating);
                        } else {
                            Toast.makeText(context, "Couldn't load merchant rating. Please try again.", Toast.LENGTH_SHORT).show();
                        }

                        doneMerchantHandle();
                    }
                });
    }

    private void getMerchantReview() {

        db.collection("Merchant Reviews")
                .whereEqualTo("Merchant Name", "Abans")
                .whereEqualTo("Merchant Rated", false)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                            if (documentSnapshots.size() < 1) {
                                rateAbansDisabled = true;
                                if (btnRateMerchantAbans != null) {
                                    disableAbansRateMerchant();
                                }

                            } else {
                                abansQrId = documentSnapshots.get(0).getId();
                                rateAbansDisabled = false;
                                if (btnRateMerchantAbans != null) {
                                    enableAbansRateMerchant();
                                }
                            }
                        } else {
                            Toast.makeText(context, "Couldn't load merchant review. Please try again.", Toast.LENGTH_SHORT).show();
                        }

                        doneMerchantHandle();
                    }
                });

        db.collection("Merchant Reviews")
                .whereEqualTo("Merchant Name", "Singer")
                .whereEqualTo("Merchant Rated", false)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                            if (documentSnapshots.size() < 1) {
                                rateSingerDisabled = true;
                                if (btnRateMerchantSinger != null) {
                                    disableSingerRateMerchant();
                                }
                            } else {
                                singerQrId = documentSnapshots.get(0).getId();
                                rateSingerDisabled = false;
                                if (btnRateMerchantSinger != null) {
                                    enableSingerRateMerchant();
                                }
                            }
                        } else {
                            Toast.makeText(context, "Couldn't load merchant review. Please try again.", Toast.LENGTH_SHORT).show();
                        }

                        doneMerchantHandle();
                    }
                });

        db.collection("Merchant Reviews")
                .whereEqualTo("Merchant Name", "Softlogic Holdings PLC")
                .whereEqualTo("Merchant Rated", false)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                            if (documentSnapshots.size() < 1) {
                                rateSoftlogicDisabled = true;
                                if (btnRateMerchantSoftlogic != null) {
                                    disableSoftlogicRateMerchant();
                                }
                            } else {
                                softlogicQrId = documentSnapshots.get(0).getId();
                                rateSoftlogicDisabled = false;
                                if (btnRateMerchantSoftlogic != null) {
                                    enableSoftlogicRateMerchant();
                                }
                            }
                        } else {
                            Toast.makeText(context, "Couldn't load merchant review. Please try again.", Toast.LENGTH_SHORT).show();
                        }

                        doneMerchantHandle();
                    }
                });
    }

    private void doneMerchantHandle() {
        doneMerchant++;
        if (doneMerchant > 6) {
            doneMerchant = 0;

            if (btnRateMerchantAbans != null)
            if (rateAbansDisabled)
                disableAbansRateMerchant();
            else
                enableAbansRateMerchant();

            if (btnRateMerchantSinger != null)
            if (rateSingerDisabled)
                disableSingerRateMerchant();
            else
                enableSingerRateMerchant();

            if (btnRateMerchantSoftlogic != null)
            if (rateSoftlogicDisabled)
                disableSoftlogicRateMerchant();
            else
                enableSoftlogicRateMerchant();

            progressDialog.dismiss();
        }
    }

    private void getAbansReview(final double averageReviewRating) {

        showProgressDialog("Loading Reviews");

        db.collection("Merchant Reviews")
                .whereEqualTo("Merchant Name", "Abans")
                .whereEqualTo("Merchant Rated", false)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                            if (documentSnapshots.size() < 1) {
                                rateAbansDisabled = true;
                                if (btnRateMerchantAbans != null) {
                                    disableAbansRateMerchant();
                                }

                            } else {
                                abansQrId = documentSnapshots.get(0).getId();
                                rateAbansDisabled = false;
                                if (btnRateMerchantAbans != null) {
                                    enableAbansRateMerchant();
                                }
                            }
                        } else {
                            Toast.makeText(context, "Couldn't load merchant review. Please try again.", Toast.LENGTH_SHORT).show();
                        }

                        progressDialog.dismiss();

                        updateMerchantRating(averageReviewRating, "Abans");
                    }
                });
    }

    private void getSingerReview(final double averageReviewRating) {

        showProgressDialog("Loading Reviews");

        db.collection("Merchant Reviews")
                .whereEqualTo("Merchant Name", "Singer")
                .whereEqualTo("Merchant Rated", false)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                            if (documentSnapshots.size() < 1) {
                                rateSingerDisabled = true;
                                if (btnRateMerchantSinger != null) {
                                    disableSingerRateMerchant();
                                }
                            } else {
                                singerQrId = documentSnapshots.get(0).getId();
                                rateSingerDisabled = false;
                                if (btnRateMerchantSinger != null) {
                                    enableSingerRateMerchant();
                                }
                            }
                        } else {
                            Toast.makeText(context, "Couldn't load merchant review. Please try again.", Toast.LENGTH_SHORT).show();
                        }

                        progressDialog.dismiss();

                        updateMerchantRating(averageReviewRating, "Singer");
                    }
                });
    }

    private void getSoftlogicReview(final double averageReviewRating) {

        showProgressDialog("Loading Reviews");

        db.collection("Merchant Reviews")
                .whereEqualTo("Merchant Name", "Softlogic Holdings PLC")
                .whereEqualTo("Merchant Rated", false)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                            if (documentSnapshots.size() < 1) {
                                rateSoftlogicDisabled = true;
                                if (btnRateMerchantSoftlogic != null) {
                                    disableSoftlogicRateMerchant();
                                }
                            } else {
                                softlogicQrId = documentSnapshots.get(0).getId();
                                rateSoftlogicDisabled = false;
                                if (btnRateMerchantSoftlogic != null) {
                                    enableSoftlogicRateMerchant();
                                }
                            }
                        } else {
                            Toast.makeText(context, "Couldn't load merchant review. Please try again.", Toast.LENGTH_SHORT).show();
                        }

                        progressDialog.dismiss();

                        updateMerchantRating(averageReviewRating, "Softlogic Holdings PLC");
                    }
                });
    }

    private void initializeAbansDialog() {
        abansDialog = new Dialog(context);
        abansDialog.setContentView(R.layout.layout_rate_merchant);
        abansReviewUtils = new ReviewUtils(abansDialog, true);

        final String merchantName = "Abans";
        final int merchantRating[] = new int[1];
        merchantRating[0] = 0;

        db.collection("Merchants")
                .whereEqualTo("Merchant Name", merchantName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            merchantRating[0] = (int) Math.rint(task.getResult().getDocuments().get(0).getDouble("Merchant Rating"));
                            abansReviewUtils.fillStars(merchantRating[0]);
                        } else {
                            abansReviewUtils.fillStars(merchantRating[0]);
                        }

                        doneMerchantHandle();
                    }
                });

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        abansDialog.getWindow().setAttributes(params);
        abansDialog.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abansDialog.dismiss();
            }
        });
        abansDialog.findViewById(R.id.tv_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abansDialogSubmitClick();
            }
        });

        ((TextView) abansDialog.findViewById(R.id.tv_merchant_name)).setText(merchantName);
        ((RoundedImageView) abansDialog.findViewById(R.id.riv_merchant_logo)).setImageResource(SwitchUtils.getMerchantImgId(merchantName));
    }

    private void initializeSingerDialog() {
        singerDialog = new Dialog(context);
        singerDialog.setContentView(R.layout.layout_rate_merchant);
        singerReviewUtils = new ReviewUtils(singerDialog, true);

        final String merchantName = "Singer";
        final int merchantRating[] = new int[1];
        merchantRating[0] = 0;

        db.collection("Merchants")
                .whereEqualTo("Merchant Name", merchantName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            merchantRating[0] = (int) Math.rint(task.getResult().getDocuments().get(0).getDouble("Merchant Rating"));
                            singerReviewUtils.fillStars(merchantRating[0]);
                        } else {
                            singerReviewUtils.fillStars(merchantRating[0]);
                        }

                        doneMerchantHandle();
                    }
                });

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        singerDialog.getWindow().setAttributes(params);
        singerDialog.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                singerDialog.dismiss();
            }
        });
        singerDialog.findViewById(R.id.tv_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                singerDialogSubmitClick();
            }
        });

        ((TextView) singerDialog.findViewById(R.id.tv_merchant_name)).setText(merchantName);
        ((RoundedImageView) singerDialog.findViewById(R.id.riv_merchant_logo)).setImageResource(SwitchUtils.getMerchantImgId(merchantName));
    }

    private void initializeSoftlogicDialog() {
        softlogicDialog = new Dialog(context);
        softlogicDialog.setContentView(R.layout.layout_rate_merchant);
        softlogicReviewUtils = new ReviewUtils(softlogicDialog, true);

        final String merchantName = "Softlogic Holdings PLC";
        final int merchantRating[] = new int[1];
        merchantRating[0] = 0;

        db.collection("Merchants")
                .whereEqualTo("Merchant Name", merchantName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            merchantRating[0] = (int) Math.rint(task.getResult().getDocuments().get(0).getDouble("Merchant Rating"));
                            softlogicReviewUtils.fillStars(merchantRating[0]);
                        } else {
                            softlogicReviewUtils.fillStars(merchantRating[0]);
                        }

                        doneMerchantHandle();
                    }
                });

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        softlogicDialog.getWindow().setAttributes(params);
        softlogicDialog.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                softlogicDialog.dismiss();
            }
        });
        softlogicDialog.findViewById(R.id.tv_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                softlogicDialogSubmitClick();
            }
        });

        ((TextView) softlogicDialog.findViewById(R.id.tv_merchant_name)).setText(merchantName);
        ((RoundedImageView) softlogicDialog.findViewById(R.id.riv_merchant_logo)).setImageResource(SwitchUtils.getMerchantImgId(merchantName));
    }

    private void abansDialogSubmitClick() {

        double responsiveRating = abansReviewUtils.getResponsiveRating();
        double saleServiceRating = abansReviewUtils.getSaleServiceRating();
        double agentSupportRating = abansReviewUtils.getAgentSupportRating();
        double productInfoRating = abansReviewUtils.getProductInfoRating();
        double valueRating = abansReviewUtils.getValueRating();

        final double averageReviewRating = (responsiveRating + saleServiceRating + agentSupportRating + productInfoRating + valueRating) / 5;

        showProgressDialog("Saving Merchant Rating");

        final Map<String, Object> merchantReviewData = new HashMap<>();
        merchantReviewData.put("Merchant Name", "Abans");
        merchantReviewData.put("Customer Email", CurrentCustomer.email);
        merchantReviewData.put("Customer Name", CurrentCustomer.name);
        merchantReviewData.put("QR Id", abansQrId);
        merchantReviewData.put("Merchant Rated", true);
        merchantReviewData.put("Responsiveness", responsiveRating);
        merchantReviewData.put("After Sale Service", saleServiceRating);
        merchantReviewData.put("Sales Agent Support", agentSupportRating);
        merchantReviewData.put("Product Info", productInfoRating);
        merchantReviewData.put("Value For Price", valueRating);
        merchantReviewData.put("Date", Timestamp.now());

        db.collection("Merchant Reviews")
                .document(abansQrId)
                .update(merchantReviewData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            PreferencesUtils.saveCustomerPoints(context, true);
                            DBUtils.updatePoints(false, new UpdatePointsCallback() {
                                @Override
                                public void onCallback(@NotNull String result, boolean successful) {
                                    Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                    abansDialog.dismiss();

                                    getAbansReview(averageReviewRating);
                                }
                            });

                            FCMUtils.Companion.sendMessage(context, true, true, "Review", "Company has been rated by " + CurrentCustomer.name, "Abans", CurrentCustomer.email);
                            FCMUtils.Companion.sendMessage(context, true, false, "Review published - " + "Abans", "Your review for " + "Abans" + " has been published", CurrentCustomer.email, CurrentCustomer.email);

                        } else {
                            Toast.makeText(context, "Failed to rate merchant. Please try again.", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            abansDialog.dismiss();
                        }
                    }
                });

    }

    private void singerDialogSubmitClick() {

        double responsiveRating = singerReviewUtils.getResponsiveRating();
        double saleServiceRating = singerReviewUtils.getSaleServiceRating();
        double agentSupportRating = singerReviewUtils.getAgentSupportRating();
        double productInfoRating = singerReviewUtils.getProductInfoRating();
        double valueRating = singerReviewUtils.getValueRating();

        final double averageReviewRating = (responsiveRating + saleServiceRating + agentSupportRating + productInfoRating + valueRating) / 5;

        showProgressDialog("Saving Merchant Rating");
        final Map<String, Object> merchantReviewData = new HashMap<>();
        merchantReviewData.put("Merchant Name", "Singer");
        merchantReviewData.put("Customer Email", CurrentCustomer.email);
        merchantReviewData.put("Customer Name", CurrentCustomer.name);
        merchantReviewData.put("QR Id", singerQrId);
        merchantReviewData.put("Merchant Rated", true);
        merchantReviewData.put("Responsiveness", responsiveRating);
        merchantReviewData.put("After Sale Service", saleServiceRating);
        merchantReviewData.put("Sales Agent Support", agentSupportRating);
        merchantReviewData.put("Product Info", productInfoRating);
        merchantReviewData.put("Value For Price", valueRating);
        merchantReviewData.put("Date", Timestamp.now());

        db.collection("Merchant Reviews")
                .document(singerQrId)
                .update(merchantReviewData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            PreferencesUtils.saveCustomerPoints(context, true);
                            DBUtils.updatePoints(false, new UpdatePointsCallback() {
                                @Override
                                public void onCallback(@NotNull String result, boolean successful) {
                                    Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                    singerDialog.dismiss();

                                    getSingerReview(averageReviewRating);
                                }
                            });

                            FCMUtils.Companion.sendMessage(context, true, true, "Review", "Company has been rated by " + CurrentCustomer.name, "Singer", CurrentCustomer.email);
                            FCMUtils.Companion.sendMessage(context, true, false, "Review published - " + "Singer", "Your review for " + "Singer" + " has been published", CurrentCustomer.email, CurrentCustomer.email);
                        } else {
                            Toast.makeText(context, "Failed to rate merchant. Please try again.", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            singerDialog.dismiss();
                        }
                    }
                });
    }

    private void softlogicDialogSubmitClick() {

        double responsiveRating = softlogicReviewUtils.getResponsiveRating();
        double saleServiceRating = softlogicReviewUtils.getSaleServiceRating();
        double agentSupportRating = softlogicReviewUtils.getAgentSupportRating();
        double productInfoRating = softlogicReviewUtils.getProductInfoRating();
        double valueRating = softlogicReviewUtils.getValueRating();

        final double averageReviewRating = (responsiveRating + saleServiceRating + agentSupportRating + productInfoRating + valueRating) / 5;

        showProgressDialog("Saving Merchant Rating");
        final Map<String, Object> merchantReviewData = new HashMap<>();
        merchantReviewData.put("Merchant Name", "Softlogic Holdings PLC");
        merchantReviewData.put("Customer Email", CurrentCustomer.email);
        merchantReviewData.put("Customer Name", CurrentCustomer.name);
        merchantReviewData.put("QR Id", softlogicQrId);
        merchantReviewData.put("Merchant Rated", true);
        merchantReviewData.put("Responsiveness", responsiveRating);
        merchantReviewData.put("After Sale Service", saleServiceRating);
        merchantReviewData.put("Sales Agent Support", agentSupportRating);
        merchantReviewData.put("Product Info", productInfoRating);
        merchantReviewData.put("Value For Price", valueRating);
        merchantReviewData.put("Date", Timestamp.now());

        db.collection("Merchant Reviews")
                .document(softlogicQrId)
                .update(merchantReviewData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            PreferencesUtils.saveCustomerPoints(context, true);
                            DBUtils.updatePoints(false, new UpdatePointsCallback() {
                                @Override
                                public void onCallback(@NotNull String result, boolean successful) {
                                    Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                    softlogicDialog.dismiss();

                                    getSoftlogicReview(averageReviewRating);
                                }
                            });

                            FCMUtils.Companion.sendMessage(context, true, true, "Review", "Company has been rated by " + CurrentCustomer.name, "Softlogic Holdings PLC", CurrentCustomer.email);
                            FCMUtils.Companion.sendMessage(context, true, false, "Review published - " + "Softlogic", "Your review for " + "Softlogic" + " has been published", CurrentCustomer.email, CurrentCustomer.email);
                        } else {
                            Toast.makeText(context, "Failed to rate merchant. Please try again.", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            softlogicDialog.dismiss();
                        }
                    }
                });
    }

    private void setListeners() {

        if (btnRateMerchantAbans != null)
            btnRateMerchantAbans.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    abansDialog.show();
                }
            });

        if (btnRateMerchantSinger != null)
            btnRateMerchantSinger.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    singerDialog.show();
                }
            });

        if (btnRateMerchantSoftlogic != null)
            btnRateMerchantSoftlogic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    softlogicDialog.show();
                }
            });

    }

    private void updateMerchantRating(final double averageReviewRating, final String merchantName) {

        final double[] merchantRating = new double[1];

        showProgressDialog("Updating Merchant Rating");

        db.collection("Merchant Reviews")
                .whereEqualTo("Merchant Name", merchantName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                            perReviewAverage = new double[documentSnapshots.size()];
                            if (documentSnapshots.size() > 0) {

                                for (int i = 0; i < documentSnapshots.size(); i++) {
                                    DocumentSnapshot documentSnapshot = documentSnapshots.get(i);
                                    double responsiveRating = documentSnapshot.getDouble("Responsiveness");
                                    double saleServiceRating = documentSnapshot.getDouble("After Sale Service");
                                    double agentSupportRating = documentSnapshot.getDouble("Sales Agent Support");
                                    double productInfoRating = documentSnapshot.getDouble("Product Info");
                                    double valueRating = documentSnapshot.getDouble("Value For Price");

                                    double averageReviewRating = (responsiveRating + saleServiceRating + agentSupportRating + productInfoRating + valueRating) / 5;

                                    perReviewAverage[i] = averageReviewRating;
                                }

                                merchantRating[0] = findAverage(perReviewAverage);

                            } else {
                                merchantRating[0] = averageReviewRating;
                            }

                            db.collection("Merchants")
                                    .whereEqualTo("Merchant Name", merchantName)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                Map<String, Object> merchantData = new HashMap<String, Object>();
                                                merchantData.put("Merchant Rating", merchantRating[0]);

                                                db.collection("Merchants")
                                                        .document(task.getResult().getDocuments().get(0).getId())
                                                        .update(merchantData)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (!task.isSuccessful()) {
                                                                    Toast.makeText(context, "Couldn't update merchant rating", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                            } else {
                                                Toast.makeText(context, "Couldn't update merchant rating", Toast.LENGTH_SHORT).show();
                                            }

                                            progressDialog.dismiss();
                                        }
                                    });

                        } else {
                            Toast.makeText(context, "Couldn't update merchant rating", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });
    }

    private static double findAverage(double[] array) {
        double sum = findSum(array);
        return sum / array.length;
    }

    public static double findSum(double[] array) {
        double sum = 0;
        for (double value : array) {
            sum += value;
        }
        return sum;
    }

    private void disableAbansRateMerchant() {
        btnRateMerchantAbans.setEnabled(false);
        btnRateMerchantAbans.setBackgroundColor(ContextCompat.getColor(context, R.color.light_grey));
        btnRateMerchantAbans.setTextColor(ContextCompat.getColor(context, R.color.dark_grey));
    }

    private void enableAbansRateMerchant() {
        btnRateMerchantAbans.setEnabled(true);
        btnRateMerchantAbans.setBackgroundColor(ContextCompat.getColor(context, R.color.darkest));
        btnRateMerchantAbans.setTextColor(ContextCompat.getColor(context, R.color.white));
    }

    private void disableSingerRateMerchant() {
        btnRateMerchantSinger.setEnabled(false);
        btnRateMerchantSinger.setBackgroundColor(ContextCompat.getColor(context, R.color.light_grey));
        btnRateMerchantSinger.setTextColor(ContextCompat.getColor(context, R.color.dark_grey));
    }

    private void enableSingerRateMerchant() {
        btnRateMerchantSinger.setEnabled(true);
        btnRateMerchantSinger.setBackgroundColor(ContextCompat.getColor(context, R.color.darkest));
        btnRateMerchantSinger.setTextColor(ContextCompat.getColor(context, R.color.white));
    }

    private void disableSoftlogicRateMerchant() {
        btnRateMerchantSoftlogic.setEnabled(false);
        btnRateMerchantSoftlogic.setBackgroundColor(ContextCompat.getColor(context, R.color.light_grey));
        btnRateMerchantSoftlogic.setTextColor(ContextCompat.getColor(context, R.color.dark_grey));
    }

    private void enableSoftlogicRateMerchant() {
        btnRateMerchantSoftlogic.setEnabled(true);
        btnRateMerchantSoftlogic.setBackgroundColor(ContextCompat.getColor(context, R.color.darkest));
        btnRateMerchantSoftlogic.setTextColor(ContextCompat.getColor(context, R.color.white));
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout clMain;
        TextView tvProductName;
        TextView tvProductCode;
        ImageView ivProductImg;
        TextView tvReply;
        TextView tvMerchantName;
        Button btnRateMerchant;
        ImageView ivRightArrow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            clMain = itemView.findViewById(R.id.cl_main);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductCode = itemView.findViewById(R.id.tv_product_code);
            ivProductImg = itemView.findViewById(R.id.iv_product_img);
            tvReply = itemView.findViewById(R.id.tv_reply);
            ivRightArrow = itemView.findViewById(R.id.iv_right_arrow);
            tvMerchantName = itemView.findViewById(R.id.tv_merchant_name);
            btnRateMerchant = itemView.findViewById(R.id.btn_rate_merchant);
        }
    }
}
