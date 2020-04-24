package com.example.test_04.ui.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.example.test_04.R;
import com.example.test_04.db_callbacks.IUploadMerchantRating;
import com.example.test_04.db_callbacks.IUploadQrScan;
import com.example.test_04.db_callbacks.IsQRExpiredCallback;
import com.example.test_04.ui.CustomerHome;
import com.example.test_04.utils.DBUtils;
import com.example.test_04.utils.QRUtils;
import com.google.zxing.Result;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScanFragment extends Fragment {

    private CodeScannerView scannerView;

    private CodeScanner mCodeScanner;
    private CustomerHome customerHome;
    private int done = 0;
    private ProgressDialog progressDialog;

    public ScanFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        findViews(view);

        init();

        return view;
    }

    private void showProgressDialog(String title) {
        progressDialog = new ProgressDialog(customerHome);
        progressDialog.setTitle(title);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void init() {
        customerHome = (CustomerHome) getActivity();

        mCodeScanner = new CodeScanner(customerHome, scannerView);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                customerHome.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        final String qrCode = result.getText();
                        done = 0;
                        if (qrCode.contains("//")) {
                            if (QRUtils.hasEmail(qrCode)) {
                                String customerEmail = QRUtils.extractCustomerEmail(qrCode);
                            } else {
                                showProgressDialog("Verifying Qr code");
                                DBUtils.isQrScanned(qrCode, new IsQRExpiredCallback() {
                                    @Override
                                    public void onCallback(boolean found, boolean expired, boolean successful, String merchantName) {
                                        if (successful) {

                                            if (found) {
                                                if (expired) {
                                                    Toast toast = Toast.makeText(customerHome, "The QR code has expired. Touch the screen to try again", Toast.LENGTH_SHORT);
                                                    TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                                                    if (v != null) v.setGravity(Gravity.CENTER);
                                                    toast.show();
                                                    progressDialog.dismiss();
                                                } else {
                                                    DBUtils.uploadQrScan(QRUtils.getQr(qrCode), merchantName, new IUploadQrScan() {
                                                        @Override
                                                        public void onCallback(boolean successful) {
                                                            done++;
                                                            if (done > 1) {
                                                                customerHome.loadReview(qrCode);
                                                                progressDialog.dismiss();
                                                            }
                                                        }
                                                    });

                                                    DBUtils.uploadMerchantRating(QRUtils.getId(qrCode), merchantName, new IUploadMerchantRating() {
                                                        @Override
                                                        public void onCallback(boolean successful) {
                                                            done++;
                                                            if (done > 1) {
                                                                customerHome.loadReview(qrCode);
                                                                progressDialog.dismiss();
                                                            }
                                                        }
                                                    });
                                                }
                                            } else {
                                                Toast.makeText(customerHome, "QR code doesn't exist. Touch the screen to try again", Toast.LENGTH_SHORT).show();
                                                progressDialog.dismiss();
                                            }
                                        } else {
                                            Toast.makeText(customerHome, "Failed to connect to server", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }
                                    }
                                });

                            }
                        } else {
                            Toast toast = Toast.makeText(customerHome, "Wrong QR code. Touch the screen to try again", Toast.LENGTH_SHORT);
                            TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                            if (v != null) v.setGravity(Gravity.CENTER);
                            toast.show();
                        }
                    }
                });
            }
        });
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });
    }

    private void findViews(View view) {
        scannerView = view.findViewById(R.id.scanner_view);
    }

    @Override
    public void onResume() {
        super.onResume();

        customerHome.onScanResume();
        mCodeScanner.startPreview();

    }

    @Override
    public void onPause() {
        super.onPause();

        if (progressDialog != null)
            progressDialog.dismiss();

    }
}
