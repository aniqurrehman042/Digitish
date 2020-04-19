package com.example.test_04.utils;

import android.app.Dialog;
import android.view.View;
import android.widget.ImageView;

import com.example.test_04.R;

import java.util.ArrayList;

public class ReviewUtils {

    private ArrayList<ImageView> ratingImageViews = new ArrayList<>();
    private ArrayList<ImageView> responsiveRatingImageViews = new ArrayList<>();
    private ArrayList<ImageView> saleServiceRatingImageViews = new ArrayList<>();
    private ArrayList<ImageView> agentSupportRatingImageViews = new ArrayList<>();
    private ArrayList<ImageView> productInfoRatingImageViews = new ArrayList<>();
    private ArrayList<ImageView> valueRatingImageViews = new ArrayList<>();
    private int rating[] = {0};
    private int responsiveRating[] = {0};
    private int saleServiceRating[] = {0};
    private int agentSupportRating[] = {0};
    private int productInfoRating[] = {0};
    private int valueRating[] = {0};

    public ReviewUtils(View view, boolean mutable, boolean isSearch) {

        if (isSearch) {
            ratingImageViews.add((ImageView) view.findViewById(R.id.iv_rating1_search));
            ratingImageViews.add((ImageView) view.findViewById(R.id.iv_rating2_search));
            ratingImageViews.add((ImageView) view.findViewById(R.id.iv_rating3_search));
            ratingImageViews.add((ImageView) view.findViewById(R.id.iv_rating4_search));
            ratingImageViews.add((ImageView) view.findViewById(R.id.iv_rating5_search));
        } else {
            ratingImageViews.add((ImageView) view.findViewById(R.id.iv_rating1));
            ratingImageViews.add((ImageView) view.findViewById(R.id.iv_rating2));
            ratingImageViews.add((ImageView) view.findViewById(R.id.iv_rating3));
            ratingImageViews.add((ImageView) view.findViewById(R.id.iv_rating4));
            ratingImageViews.add((ImageView) view.findViewById(R.id.iv_rating5));
        }

        if (mutable)
            setListeners(ratingImageViews, rating);
    }

    public ReviewUtils(Dialog dialog, boolean mutable) {
        ratingImageViews.add((ImageView) dialog.findViewById(R.id.iv_merchant_rating1));
        ratingImageViews.add((ImageView) dialog.findViewById(R.id.iv_merchant_rating2));
        ratingImageViews.add((ImageView) dialog.findViewById(R.id.iv_merchant_rating3));
        ratingImageViews.add((ImageView) dialog.findViewById(R.id.iv_merchant_rating4));
        ratingImageViews.add((ImageView) dialog.findViewById(R.id.iv_merchant_rating5));

        responsiveRatingImageViews.add((ImageView) dialog.findViewById(R.id.iv_responsive_rating1));
        responsiveRatingImageViews.add((ImageView) dialog.findViewById(R.id.iv_responsive_rating2));
        responsiveRatingImageViews.add((ImageView) dialog.findViewById(R.id.iv_responsive_rating3));
        responsiveRatingImageViews.add((ImageView) dialog.findViewById(R.id.iv_responsive_rating4));
        responsiveRatingImageViews.add((ImageView) dialog.findViewById(R.id.iv_responsive_rating5));

        saleServiceRatingImageViews.add((ImageView) dialog.findViewById(R.id.iv_sale_service_rating1));
        saleServiceRatingImageViews.add((ImageView) dialog.findViewById(R.id.iv_sale_service_rating2));
        saleServiceRatingImageViews.add((ImageView) dialog.findViewById(R.id.iv_sale_service_rating3));
        saleServiceRatingImageViews.add((ImageView) dialog.findViewById(R.id.iv_sale_service_rating4));
        saleServiceRatingImageViews.add((ImageView) dialog.findViewById(R.id.iv_sale_service_rating5));

        agentSupportRatingImageViews.add((ImageView) dialog.findViewById(R.id.iv_agent_support_rating1));
        agentSupportRatingImageViews.add((ImageView) dialog.findViewById(R.id.iv_agent_support_rating2));
        agentSupportRatingImageViews.add((ImageView) dialog.findViewById(R.id.iv_agent_support_rating3));
        agentSupportRatingImageViews.add((ImageView) dialog.findViewById(R.id.iv_agent_support_rating4));
        agentSupportRatingImageViews.add((ImageView) dialog.findViewById(R.id.iv_agent_support_rating5));

        productInfoRatingImageViews.add((ImageView) dialog.findViewById(R.id.iv_product_info_rating1));
        productInfoRatingImageViews.add((ImageView) dialog.findViewById(R.id.iv_product_info_rating2));
        productInfoRatingImageViews.add((ImageView) dialog.findViewById(R.id.iv_product_info_rating3));
        productInfoRatingImageViews.add((ImageView) dialog.findViewById(R.id.iv_product_info_rating4));
        productInfoRatingImageViews.add((ImageView) dialog.findViewById(R.id.iv_product_info_rating5));

        valueRatingImageViews.add((ImageView) dialog.findViewById(R.id.iv_value_rating1));
        valueRatingImageViews.add((ImageView) dialog.findViewById(R.id.iv_value_rating2));
        valueRatingImageViews.add((ImageView) dialog.findViewById(R.id.iv_value_rating3));
        valueRatingImageViews.add((ImageView) dialog.findViewById(R.id.iv_value_rating4));
        valueRatingImageViews.add((ImageView) dialog.findViewById(R.id.iv_value_rating5));

        if (false) {
            setListeners(ratingImageViews, rating);
        }

        if (mutable) {
            setListeners(responsiveRatingImageViews, responsiveRating);
            setListeners(saleServiceRatingImageViews, saleServiceRating);
            setListeners(agentSupportRatingImageViews, agentSupportRating);
            setListeners(productInfoRatingImageViews, productInfoRating);
            setListeners(valueRatingImageViews, valueRating);
        }

    }

    private void setListeners(final ArrayList<ImageView> ratingImageViews, final int rating[]) {
        for (int i = 0; i < ratingImageViews.size(); i++) {
            final int currentIndex = i;
            ratingImageViews.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fillStars(currentIndex + 1, ratingImageViews, rating);
                }
            });
        }
    }

    private void fillStars(int numOfStars, ArrayList<ImageView> ratingImageViews, int rating[]) {
        for (int i = 0; i < numOfStars; i++) {
            ratingImageViews.get(i).setImageResource(R.drawable.ic_star_filled);
        }

        for (int i = numOfStars; i < ratingImageViews.size(); i++) {
            ratingImageViews.get(i).setImageResource(R.drawable.ic_star_hollow);
        }

        rating[0] = numOfStars;
    }

    public void fillStars(int numOfStars) {
        for (int i = 0; i < numOfStars; i++) {
            ratingImageViews.get(i).setImageResource(R.drawable.ic_star_filled);
        }

        for (int i = numOfStars; i < ratingImageViews.size(); i++) {
            ratingImageViews.get(i).setImageResource(R.drawable.ic_star_hollow);
        }

        rating[0] = numOfStars;
    }

    public int getRating() {
        return rating[0];
    }

    public int getResponsiveRating() {
        return responsiveRating[0];
    }

    public int getSaleServiceRating() {
        return saleServiceRating[0];
    }

    public int getAgentSupportRating() {
        return agentSupportRating[0];
    }

    public int getProductInfoRating() {
        return productInfoRating[0];
    }

    public int getValueRating() {
        return valueRating[0];
    }

    public void setResponsiveRating(int responsiveRating) {
        int[] responsiveRatingArray = {responsiveRating};
        this.responsiveRating = responsiveRatingArray;
        fillStars(responsiveRating, responsiveRatingImageViews, responsiveRatingArray);
    }

    public void setSaleServiceRating(int saleServiceRating) {
        int[] saleServiceRatingArray = {saleServiceRating};
        this.saleServiceRating = saleServiceRatingArray;
        fillStars(saleServiceRating, saleServiceRatingImageViews, saleServiceRatingArray);
    }

    public void setAgentSupportRating(int agentSupportRating) {
        int[] agentSupportRatingArray = {agentSupportRating};
        this.agentSupportRating = agentSupportRatingArray;
        fillStars(agentSupportRating, agentSupportRatingImageViews, agentSupportRatingArray);
    }

    public void setProductInfoRating(int productInfoRating) {
        int[] productInfoRatingArray = {productInfoRating};
        this.productInfoRating = productInfoRatingArray;
        fillStars(productInfoRating, productInfoRatingImageViews, productInfoRatingArray);
    }

    public void setValueRating(int valueRating) {
        int[] valueRatingArray = {valueRating};
        this.valueRating = valueRatingArray;
        fillStars(valueRating, valueRatingImageViews, valueRatingArray);
    }
}
