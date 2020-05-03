package com.example.test_04.ui.fragments;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test_04.R;
import com.example.test_04.adapters.SuggestionsAdapter;
import com.example.test_04.async.DownloadImageTask;
import com.example.test_04.models.CurrentCustomer;
import com.example.test_04.models.Product;
import com.example.test_04.ui.CustomerHome;
import com.example.test_04.ui.MerchantLogin;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {

    private TextView tvCustomerName;
    private TextView tvCustomerPoints;
    private TextView tvCallback;
    private ImageView ivHeadset;
    private RoundedImageView rivProfilePicture;

    private CustomerHome customerHome;
    private Bitmap profileBmp = null;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ProgressDialog progressDialog;

    private View.OnClickListener requestCallbackClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showProgressDialog("Requesting Feedback");
            Map<String, Object> feedbackData = new HashMap<>();
            feedbackData.put("Customer Email", CurrentCustomer.email);
            feedbackData.put("Customer Name", CurrentCustomer.name);
            feedbackData.put("Date", Timestamp.now());
            db.collection("Callbacks")
                    .add(feedbackData)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(customerHome, "Callback requested successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(customerHome, "Failed to request callback", Toast.LENGTH_SHORT).show();
                            }
                            progressDialog.dismiss();
                        }
                    });
        }
    };

    public AccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_account, container, false);

        findViews(view);

        setViewValues();
        setListeners();

        view.findViewById(R.id.cl_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ((ViewGroup)getActivity().findViewById(R.id.cl_fragment)).removeAllViews();
                getActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.cl_fragment, new ProfileFragment())
                        .addToBackStack("Profile")
                        .commit();
            }
        });

        return view;
    }

    private void showProgressDialog(String title) {
        progressDialog = new ProgressDialog(customerHome);
        progressDialog.setTitle(title);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void setListeners() {
        tvCallback.setOnClickListener(requestCallbackClickListener);
        ivHeadset.setOnClickListener(requestCallbackClickListener);
    }

    private void setViewValues() {
        customerHome = (CustomerHome) getActivity();

        tvCustomerName.setText(CurrentCustomer.name);
        tvCustomerPoints.setText(CurrentCustomer.points + " Points");
        Bitmap profileBmp = customerHome.getProfileBmp();
        if (profileBmp != null)
            rivProfilePicture.setImageBitmap(profileBmp);
        else
            new DownloadImageTask(this.profileBmp, rivProfilePicture).execute(CurrentCustomer.profilePicture);

    }

    private void findViews(View view) {
        tvCustomerName = view.findViewById(R.id.tv_customer_name);
        tvCustomerPoints = view.findViewById(R.id.tv_customer_points);
        rivProfilePicture = view.findViewById(R.id.riv_profile_pic);
        tvCallback = view.findViewById(R.id.tv_callback);
        ivHeadset = view.findViewById(R.id.iv_headset);
    }

    @Override
    public void onResume() {
        super.onResume();

        ((TextView) getActivity().findViewById(R.id.tv_title)).setText("Account");
    }
}
