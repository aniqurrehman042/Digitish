package com.example.test_04.ui.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.test_04.R;
import com.example.test_04.adapters.SuggestionsAdapter;
import com.example.test_04.async.DownloadImageTask;
import com.example.test_04.models.CurrentCustomer;
import com.example.test_04.models.Product;
import com.example.test_04.ui.CustomerHome;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {

    private RecyclerView rvSuggestions;
    private TextView tvCustomerName;
    private TextView tvCustomerPoints;
    private RoundedImageView rivProfilePicture;

    private CustomerHome customerHome;

    private Bitmap profileBmp = null;

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

        rvSuggestions = view.findViewById(R.id.rv_suggestions);
        ArrayList<Product> products = new ArrayList<>();
        products.add(new Product(null, "Washing Machine", "Washing Machines", null));
        products.add(new Product(null, "TV", "Televisions", null));
        products.add(new Product(null, "Microwave", "Microwaves", null));
        products.add(new Product(null, "Smart TV", "Televisions1", null));
        products.add(new Product(null, "Refrigerator", "Refrigerators", null));
        SuggestionsAdapter adapter = new SuggestionsAdapter(getContext(), products);
        LinearLayoutManager lm = new LinearLayoutManager(getContext());
        lm.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvSuggestions.setAdapter(adapter);
        rvSuggestions.setLayoutManager(lm);

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
    }

    @Override
    public void onResume() {
        super.onResume();

        ((TextView) getActivity().findViewById(R.id.tv_title)).setText("Account");
    }
}
