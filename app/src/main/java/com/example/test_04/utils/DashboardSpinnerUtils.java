package com.example.test_04.utils;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test_04.R;
import com.example.test_04.db_callbacks.GetRatingsCallback;
import com.example.test_04.models.CurrentMerchant;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class DashboardSpinnerUtils {

    private ArrayList<Spinner> spinners = new ArrayList<>();
    private ArrayList<TextView> tvReviews = new ArrayList<>();
    private Context context;
    private int[] daysMins;
    private int[] daysMaxs;

    public DashboardSpinnerUtils(View view) {

        this.context = view.getContext();

        int[] spinnerIds = {R.id.s_reviews_1_star, R.id.s_reviews_2_star, R.id.s_reviews_3_star, R.id.s_reviews_4_star, R.id.s_reviews_5_star};
        int[] tvReviewIds = {R.id.tv_reviews_1_star, R.id.tv_reviews_2_star, R.id.tv_reviews_3_star, R.id.tv_reviews_4_star, R.id.tv_reviews_5_star};
        String[] spinnerValues = {"All", "This Week", "Last Week", "This Month", "Last Month"};
        daysMins = new int[]{10000, 7, 14, 30, 60};
        daysMaxs = new int[]{10000, 7, 7, 30, 30};

        findViews(view, spinnerIds, spinners);
        findViews(view, tvReviewIds, tvReviews);

        addAdaptersAndListeners(spinnerValues);

    }

    private void addAdaptersAndListeners(String[] spinnerValues) {
        for (int i = 0; i < spinners.size(); i++) {
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(context, R.layout.layout_dashboard_review_spinner_item, spinnerValues);
            Spinner spinner = spinners.get(i);
            spinner.setAdapter(spinnerAdapter);
            setListener(spinner, i + 1, tvReviews.get(i));
        }
    }

    private void setListener(final Spinner spinner, final long rating, final TextView tvReview) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                DBUtils.getRatingsBetween(CurrentMerchant.name, rating, daysMins[position], daysMaxs[position], new GetRatingsCallback() {
                    @Override
                    public void onCallback(boolean isSuccessful, @NotNull String ratings) {
                        if (isSuccessful) {
                            tvReview.setText(ratings);
                        } else {
                            Toast.makeText(context, "Couldn't load reviews", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private <T extends View> void findViews(View view, int[] viewIds, ArrayList<T> views) {
        for (int i = 0; i < viewIds.length; i++) {
            T tView = (T) view.findViewById(viewIds[i]);
            views.add(tView);
        }
    }
}
