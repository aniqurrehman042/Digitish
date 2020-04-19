package com.example.test_04.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.test_04.db_callbacks.GetMerchantCallback;
import com.example.test_04.db_callbacks.GetRatingsCallback;
import com.example.test_04.db_callbacks.IGetMerchantReviews;
import com.example.test_04.db_callbacks.IGetProductReviews;
import com.example.test_04.db_callbacks.IUploadMerchantRating;
import com.example.test_04.db_callbacks.IUploadQrScan;
import com.example.test_04.db_callbacks.IsQRExpiredCallback;
import com.example.test_04.db_callbacks.UpdatePointsCallback;
import com.example.test_04.models.CurrentCustomer;
import com.example.test_04.models.Merchant;
import com.example.test_04.models.MerchantReview;
import com.example.test_04.models.Product;
import com.example.test_04.models.ProductReview;
import com.example.test_04.models.QRCode;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBUtils {

    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static ProgressDialog progressDialog;
    private static Context context;

    private DBUtils() {
    }

    public static void setContext(Context con) {
        context = con;
    }

    public static void updatePoints(boolean customerQr, final UpdatePointsCallback callback) {

        final long points[] = {0};

        if (customerQr)
            points[0] = 10;
        else
            points[0] = 20;

        db.collection("Customers")
                .whereEqualTo("Email", CurrentCustomer.email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        String id = documentSnapshot.getId();
                        long oldPoints = documentSnapshot.getLong("Points");

                        final Map<String, Object> customerData = new HashMap<>();
                        customerData.put("Points", points[0] + oldPoints);

                        db.collection("Customers")
                                .document(id)
                                .update(customerData)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            callback.onCallback("You got " + points[0] + " points for rating", true);
                                        } else {
                                            callback.onCallback("Failed to update points", false);
                                        }
                                    }
                                });

                    }
                });
    }

    public static void getMerchant(final String merchantName, final GetMerchantCallback callback) {

        db.collection("Merchants")
                .whereEqualTo("Merchant Name", merchantName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                            String merchantDescription = documentSnapshot.get("Merchant Description").toString();
                            String merchantEmail = documentSnapshot.get("Email").toString();
                            String merchantRating = documentSnapshot.get("Merchant Rating").toString();
                            String merchantProducts = documentSnapshot.get("Products").toString();
                            String merchantWebsite = documentSnapshot.get("Website").toString();
                            Merchant merchant = new Merchant(merchantName, merchantDescription, merchantEmail, merchantRating, merchantProducts, merchantWebsite);
                            callback.onCallback(merchant, true);
                        } else {
                            callback.onCallback(null, false);
                        }
                    }
                });

    }

    public static void isQrScanned(String qrCode, final IsQRExpiredCallback callback) {

        String qrId = QRUtils.getId(qrCode);

        db.collection("Purchase QRs")
                .whereEqualTo(FieldPath.documentId(), qrId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                            if (documentSnapshots.size() > 0) {
                                DocumentSnapshot documentSnapshot = documentSnapshots.get(0);

                                final String merchantName = documentSnapshot.getString("Merchant Name");

                                if (!documentSnapshot.getBoolean("Expired")) {
                                    Map<String, Object> purchaseQrsData = new HashMap<>();
                                    purchaseQrsData.put("Expired", true);
                                    purchaseQrsData.put("Customer Email", CurrentCustomer.email);
                                    db.collection("Purchase QRs")
                                            .document(documentSnapshot.getId())
                                            .update(purchaseQrsData)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    callback.onCallback(true, false, true, merchantName);
                                                }
                                            });

                                } else
                                    callback.onCallback(true, true, true, merchantName);
                            } else {
                                callback.onCallback(false, false, true, "");
                            }

                        } else {
                            callback.onCallback(false, false, false, "");
                        }
                    }
                });

    }

    private static void showProgressDialog(String title) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(title);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public static void getRatingsBetween(String merchantName, long rating, int daysMin, int daysMax, final GetRatingsCallback callback) {

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -daysMin);
        Date dateMin = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, daysMax);
        Date dateMax = calendar.getTime();
        Timestamp timestampMin = new Timestamp(dateMin);
        Timestamp timestampMax = new Timestamp(dateMax);

        db.collection("Product Reviews")
                .whereEqualTo("Product Rating", rating)
                .whereEqualTo("Merchant Name", merchantName)
                .whereGreaterThan("Date", timestampMin)
                .whereLessThan("Date", timestampMax)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            callback.onCallback(true, String.valueOf(task.getResult().getDocuments().size()));
                        } else {
                            callback.onCallback(false, "");
                        }
                    }
                });

    }

    public static void uploadQrScan(final QRCode qrCode, final String merchantName, final IUploadQrScan callback) {

        final ArrayList<Product> products = new ArrayList<>();

        db.collection("Products")
                .whereEqualTo("Merchant Name", merchantName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                            for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                                String productCode = documentSnapshot.getString("Product Code");

                                for (String productCodeQr : qrCode.getProductCodes()) {
                                    if (productCodeQr.equals(productCode)) {
                                        String productCategory = documentSnapshot.getString("Product Category");
                                        String productName = documentSnapshot.getString("Product Name");

                                        Product product = new Product(productCode, productName, productCategory, merchantName);
                                        products.add(product);
                                    }
                                }
                            }

                            for (final Product product : products) {

                                Map<String, Object> productReviewsData = new HashMap<>();
                                productReviewsData.put("Customer Email", CurrentCustomer.email);
                                productReviewsData.put("Customer Name", CurrentCustomer.name);
                                productReviewsData.put("Merchant Name", merchantName);
                                productReviewsData.put("Product Code", product.getProductCode());
                                productReviewsData.put("Product Name", product.getProductName());
                                productReviewsData.put("Product Category", product.getProductCategory());
                                productReviewsData.put("Product Rating", 0);
                                productReviewsData.put("Review Description", "");
                                productReviewsData.put("Review Title", "");
                                productReviewsData.put("QR Id", qrCode.getId());
                                productReviewsData.put("Completed", false);
                                productReviewsData.put("Reviewed", false);
                                productReviewsData.put("Date", new Date());

                                db.collection("Product Reviews")
                                        .add(productReviewsData)
                                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                if (task.isSuccessful()) {

                                                } else {
                                                    Toast.makeText(context, "Product with code " + product.getProductCode() + " couldn't be uploaded", Toast.LENGTH_SHORT).show();
                                                }

                                                if (products.indexOf(product) > products.size() - 2) {
                                                    callback.onCallback(true);
                                                }
                                            }
                                        });
                            }

                        } else {
                            callback.onCallback(false);
                        }
                    }
                });

    }

    public static void getProductReviews(String customerEmail, final IGetProductReviews callback) {

        final ArrayList<ProductReview> productReviews = new ArrayList<>();

        db.collection("Product Reviews")
                .whereEqualTo("Customer Email", customerEmail)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                            if (documentSnapshots.size() > 0) {
                                for (DocumentSnapshot documentSnapshot : documentSnapshots) {

                                    String merchantName = documentSnapshot.getString("Merchant Name");
                                    String productCode = documentSnapshot.getString("Product Code");
                                    String productName = documentSnapshot.getString("Product Name");
                                    String productCategory = documentSnapshot.getString("Product Category");
                                    int productRating = Integer.parseInt(documentSnapshot.get("Product Rating").toString());
                                    String reviewDescription = documentSnapshot.getString("Review Description");
                                    String reviewTitle = documentSnapshot.getString("Review Title");
                                    String qrId = documentSnapshot.getString("QR Id");
                                    boolean completed = documentSnapshot.getBoolean("Completed");
                                    boolean reviewed = documentSnapshot.getBoolean("Reviewed");
                                    String date = DateUtils.dateToStringWithTime(documentSnapshot.getTimestamp("Date").toDate());

                                    ProductReview productReview = new ProductReview(CurrentCustomer.email, CurrentCustomer.name, merchantName, productCode, productName, productCategory, productRating, reviewDescription, reviewTitle, qrId, completed, reviewed, date);
                                    productReviews.add(productReview);
                                }

                                callback.onCallback(true, productReviews);

                            } else {
                                callback.onCallback(true, productReviews);
                            }
                        } else {
                            callback.onCallback(false, null);
                        }
                    }
                });
    }

    public static void getMerchantProductReviews(final String merchantName, final IGetProductReviews callback) {

        final ArrayList<ProductReview> productReviews = new ArrayList<>();

        db.collection("Product Reviews")
                .whereEqualTo("Merchant Name", merchantName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                            if (documentSnapshots.size() > 0) {
                                for (DocumentSnapshot documentSnapshot : documentSnapshots) {

                                    String customerName = documentSnapshot.getString("Customer Name");
                                    String customerEmail = documentSnapshot.getString("Customer Email");
                                    String productCode = documentSnapshot.getString("Product Code");
                                    String productName = documentSnapshot.getString("Product Name");
                                    String productCategory = documentSnapshot.getString("Product Category");
                                    int productRating = Integer.parseInt(documentSnapshot.get("Product Rating").toString());
                                    String reviewDescription = documentSnapshot.getString("Review Description");
                                    String reviewTitle = documentSnapshot.getString("Review Title");
                                    String qrId = documentSnapshot.getString("QR Id");
                                    boolean completed = documentSnapshot.getBoolean("Completed");
                                    boolean reviewed = documentSnapshot.getBoolean("Reviewed");
                                    String date = DateUtils.dateToStringWithTime(documentSnapshot.getTimestamp("Date").toDate());

                                    ProductReview productReview = new ProductReview(customerEmail, customerName, merchantName, productCode, productName, productCategory, productRating, reviewDescription, reviewTitle, qrId, completed, reviewed, date);
                                    productReviews.add(productReview);
                                }

                                callback.onCallback(true, productReviews);

                            } else {
                                callback.onCallback(true, productReviews);
                            }
                        } else {
                            callback.onCallback(false, null);
                        }
                    }
                });
    }

    public static void getMerchantReviews(final String merchantName, final IGetMerchantReviews callback) {

        final ArrayList<MerchantReview> merchantReviews = new ArrayList<>();

        db.collection("Merchant Reviews")
                .whereEqualTo("Merchant Name", merchantName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                            if (documentSnapshots.size() > 0) {
                                for (DocumentSnapshot documentSnapshot : documentSnapshots) {

                                    String customerName = documentSnapshot.getString("Customer Name");
                                    String customerEmail = documentSnapshot.getString("Customer Email");
                                    int responsiveness = Integer.parseInt(String.valueOf(documentSnapshot.getLong("Responsiveness")));
                                    int afterSaleService = Integer.parseInt(String.valueOf(documentSnapshot.getLong("After Sale Service")));
                                    int salesAgentSupport = Integer.parseInt(String.valueOf(documentSnapshot.getLong("Sales Agent Support")));
                                    int productInfo = Integer.parseInt(String.valueOf(documentSnapshot.getLong("Product Info")));
                                    int valueForPrice = Integer.parseInt(String.valueOf(documentSnapshot.getLong("Value For Price")));
                                    String qrId = documentSnapshot.getString("QR Id");

                                    MerchantReview merchantReview = new MerchantReview(customerEmail, customerName, merchantName, responsiveness, afterSaleService, salesAgentSupport, productInfo, valueForPrice, qrId);
                                    merchantReviews.add(merchantReview);
                                }

                                callback.onCallback(true, merchantReviews);

                            } else {
                                callback.onCallback(true, merchantReviews);
                            }
                        } else {
                            callback.onCallback(false, null);
                        }
                    }
                });
    }

    public static void uploadMerchantRating(String qrId, String merchantName, final IUploadMerchantRating callback) {

        Map<String, Object> merchantRatingData = new HashMap<>();
        merchantRatingData.put("Customer Email", CurrentCustomer.email);
        merchantRatingData.put("Customer Name", CurrentCustomer.name);
        merchantRatingData.put("Merchant Name", merchantName);
        merchantRatingData.put("QR Id", qrId);
        merchantRatingData.put("Merchant Rated", false);
        merchantRatingData.put("Responsiveness", 0);
        merchantRatingData.put("After Sale Service", 0);
        merchantRatingData.put("Sales Agent Support", 0);
        merchantRatingData.put("Product Info", 0);
        merchantRatingData.put("Value For Price", 0);

        db.collection("Merchant Reviews")
                .add(merchantRatingData)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        callback.onCallback(task.isSuccessful());
                    }
                });

    }
}