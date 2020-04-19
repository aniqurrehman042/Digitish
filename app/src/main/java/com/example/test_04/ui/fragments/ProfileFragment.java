package com.example.test_04.ui.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test_04.R;
import com.example.test_04.async.DownloadImageTask;
import com.example.test_04.models.CurrentCustomer;
import com.example.test_04.ui.CustomerHome;
import com.example.test_04.utils.PreferencesUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.makeramen.roundedimageview.RoundedImageView;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickCancel;
import com.vansuita.pickimage.listeners.IPickResult;

import net.glxn.qrgen.android.QRCode;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private TextView tvEditName;
    private TextView tvEditEmail;
    private TextView tvEditPhone;
    private TextView tvName;
    private TextView tvEmail;
    private TextView tvPoints;
    private TextView tvDoneName;
    private TextView tvDoneEmail;
    private TextView tvDonePhone;
    private TextView tvPhone;
    private EditText etName;
    private EditText etEmail;
    private EditText etPhone;
    private RoundedImageView rivProfilePic;
    private LinearLayout llAddPic;
    private ImageView ivQr;

    private CustomerHome customerHome;
    private Bitmap profileBmp;
    private FirebaseFirestore db;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        db = FirebaseFirestore.getInstance();

        findViews(view);

        setListeners();

        setViewValues();

        return view;
    }

    private void setViewValues() {
        customerHome = (CustomerHome) getActivity();

        Bitmap profileBmp = customerHome.getProfileBmp();
        if (profileBmp != null)
            rivProfilePic.setImageBitmap(profileBmp);
        else
            new DownloadImageTask(this.profileBmp, rivProfilePic).execute(CurrentCustomer.profilePicture);
        tvName.setText(CurrentCustomer.name);
        tvEmail.setText(CurrentCustomer.email);
        tvPoints.setText(CurrentCustomer.points);
        if (CurrentCustomer.phone != null && !CurrentCustomer.phone.isEmpty())
            tvPhone.setText(CurrentCustomer.phone);
        else {
            tvPhone.setText("");
            tvEditPhone.setText("Add your mobile number");
        }

        setQr();

    }

    private void setQr() {
        Bitmap qrBmp = QRCode.from(CurrentCustomer.email + "//" + CurrentCustomer.points).withSize(1000, 1000).bitmap();
        ivQr.setImageBitmap(qrBmp);
    }

    private void setListeners() {
        tvEditName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifyValues(tvName, etName, tvDoneName, tvEditName);
            }
        });

        tvEditEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifyValues(tvEmail, etEmail, tvDoneEmail, tvEditEmail);
            }
        });

        tvEditPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifyValues(tvPhone, etPhone, tvDonePhone, tvEditPhone);
            }
        });

        tvDonePhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPhone = tvPhone.getText().toString();
                String phone = etPhone.getText().toString();
                etPhone.setVisibility(View.GONE);
                if (oldPhone.isEmpty() && !phone.equals("+")) {
                    tvEditPhone.setText("EDIT");
                    if (phone.length() <= 1)
                        etPhone.setText("");
                }
                tvPhone.setText(etPhone.getText());
                tvPhone.setVisibility(View.VISIBLE);
                tvEditPhone.setVisibility(View.VISIBLE);
                tvDonePhone.setVisibility(View.GONE);
                if (!oldPhone.equals(phone))
                    updatePhone(phone);
            }
        });

        llAddPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PickImageDialog.build(new PickSetup())
                        .setOnPickResult(new IPickResult() {
                            @Override
                            public void onPickResult(PickResult r) {
                                profileBmp = r.getBitmap();
                                rivProfilePic.setImageBitmap(profileBmp);
                            }
                        })
                        .setOnPickCancel(new IPickCancel() {
                            @Override
                            public void onCancelClick() {
                                //TODO: do what you have to if user clicked cancel
                            }
                        }).show(getActivity().getSupportFragmentManager());
            }
        });
    }

    private void updatePhone(final String phone) {
        db.collection("Customers")
                .whereEqualTo("Email", CurrentCustomer.email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().getDocuments().size() > 0) {
                                Map<String, Object> data = new HashMap<>();
                                data.put("Phone", phone);
                                db.collection("Customers")
                                        .document(task.getResult().getDocuments().get(0).getId())
                                        .update(data)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(customerHome, "Updated successfully", Toast.LENGTH_SHORT).show();
                                                PreferencesUtils.saveCustomerAttribute(customerHome, "Phone", phone);
                                                CurrentCustomer.phone = phone;
                                            }
                                        });
                            } else {
                                Toast.makeText(customerHome, "Failed to update", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(customerHome, "Couldn't connect to server", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void modifyValues(TextView tv, EditText et, TextView tvDone, TextView tvEdit) {
        tv.setVisibility(View.GONE);
        tvDone.setVisibility(View.VISIBLE);
        tvEdit.setVisibility(View.GONE);
        et.setVisibility(View.VISIBLE);
        String text = tv.getText().toString();
        if (text.isEmpty())
            text = "+";
        et.setText(text);
        et.requestFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
    }

    private void findViews(View view) {
        tvEditName = view.findViewById(R.id.tv_edit_name);
        tvEditEmail = view.findViewById(R.id.tv_edit_email);
        tvEditPhone = view.findViewById(R.id.tv_edit_phone);
        tvName = view.findViewById(R.id.tv_name);
        tvEmail = view.findViewById(R.id.tv_email);
        tvPhone = view.findViewById(R.id.tv_phone);
        etName = view.findViewById(R.id.et_name);
        etEmail = view.findViewById(R.id.et_email);
        etPhone = view.findViewById(R.id.et_phone);
        rivProfilePic = view.findViewById(R.id.riv_profile_pic);
        tvPoints = view.findViewById(R.id.tv_points);
        tvDoneName = view.findViewById(R.id.tv_done_name);
        tvDoneEmail = view.findViewById(R.id.tv_done_email);
        tvDonePhone = view.findViewById(R.id.tv_done_phone);
        llAddPic = view.findViewById(R.id.ll_add_pic);
        ivQr = view.findViewById(R.id.iv_qr);
    }

    @Override
    public void onResume() {
        super.onResume();

        customerHome.onProfileResume();

    }

    @Override
    public void onPause() {
        super.onPause();

        customerHome.onProfilePause();

    }
}
