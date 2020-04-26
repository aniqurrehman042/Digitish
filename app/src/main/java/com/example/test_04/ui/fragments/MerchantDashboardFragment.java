package com.example.test_04.ui.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.test_04.R;
import com.example.test_04.adapters.LegendAdapter;
import com.example.test_04.models.CurrentMerchant;
import com.example.test_04.ui.MerchantHome;
import com.example.test_04.utils.DashboardSpinnerUtils;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MerchantDashboardFragment extends Fragment {

    private PieChart pcTopProducts;
    private RecyclerView rvLegend;
    private ArrayList<PieEntry> entries = new ArrayList<>();
    private MerchantHome merchantHome;
    private DashboardSpinnerUtils dashboardSpinnerUtils;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ProgressDialog progressDialog;

    public MerchantDashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_merchant_dashboard, container, false);
        dashboardSpinnerUtils = new DashboardSpinnerUtils(view);

        merchantHome = (MerchantHome) getActivity();

        findViews(view);

        getProducts();

        return view;
    }

    private void showProgressDialog(String title) {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle(title);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public static int findSum(int[] array) {
        int sum = 0;
        for (int value : array) {
            sum += value;
        }
        return sum;
    }

    private void getProducts() {

        if (progressDialog != null)
            progressDialog.dismiss();

        showProgressDialog("Loading products");

        final int[] products = new int[4];
        products[0] = 0;
        products[1] = 0;
        products[2] = 0;
        products[3] = 0;

        db.collection("Product Reviews")
                .whereEqualTo("Merchant Name", CurrentMerchant.name)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                            if (documentSnapshots.size() > 0) {
                                for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                                    String productCategory = documentSnapshot.getString("Product Category");
                                    switch (productCategory) {
                                        case "Washing Machines":
                                            products[0]++;
                                            break;
                                        case "Refrigerators":
                                            products[1]++;
                                            break;
                                        case "Microwaves":
                                            products[2]++;
                                            break;
                                        case "Televisions":
                                            products[3]++;
                                            break;
                                    }
                                }

                                pcTopProducts.setCenterText(findSum(products) + "\nSales");
                                initializePieChart(products[0], products[1], products[2], products[3]);

                            } else {
                                initializePieChart(0,0,0,0);
                            }
                        } else {
                            Toast.makeText(merchantHome, "Couldn't load products", Toast.LENGTH_SHORT).show();
                            initializePieChart(0,0,0,0);
                        }

                        progressDialog.dismiss();
                    }
                });

    }

    private void initializePieChart(int washingMachines, int refrigerators, int microwaves, int tvs) {
        entries = new ArrayList<>();
        entries.add(new PieEntry(washingMachines, "Washing Machines", ResourcesCompat.getDrawable(merchantHome.getResources(), R.drawable.bg_btn_darkest, null)));
        entries.add(new PieEntry(refrigerators, "Refrigerators"));
        entries.add(new PieEntry(microwaves, "Microwaves"));
        entries.add(new PieEntry(tvs, "TVs"));
        PieDataSet dataSet = new PieDataSet(entries, null);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setDrawValues(false);
        dataSet.setForm(Legend.LegendForm.CIRCLE);
        PieData pieData = new PieData(dataSet);
        pcTopProducts.setDrawEntryLabels(false);
        pcTopProducts.getDescription().setEnabled(false);
        pcTopProducts.getLegend().setOrientation(Legend.LegendOrientation.HORIZONTAL);
        pcTopProducts.getLegend().setEnabled(false);
        pcTopProducts.setData(pieData);
        pcTopProducts.invalidate();

        initializeLegend(pcTopProducts.getLegend());
    }

    private void initializeLegend(Legend legend) {
        LegendAdapter legendAdapter = new LegendAdapter(entries);
        rvLegend.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLegend.setAdapter(legendAdapter);

    }

    private void findViews(View view) {
        pcTopProducts = view.findViewById(R.id.pc_top_products);
        rvLegend = view.findViewById(R.id.rv_legend);

    }

    @Override
    public void onResume() {
        super.onResume();
        merchantHome.setPageTitle("Dashboard");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressDialog != null)
            progressDialog.dismiss();
    }
}
