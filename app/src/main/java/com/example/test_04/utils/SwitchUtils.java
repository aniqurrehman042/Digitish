package com.example.test_04.utils;

import com.example.test_04.R;

public class SwitchUtils {

    public static int getMerchantImgId(String merchantName) {
        switch (merchantName) {
            case "Abans":
                return R.drawable.abans_logo_round;
            case "Softlogic Holdings PLC":
                return R.drawable.softlogic_logo_round;
            case "Singer":
                return R.drawable.singer_logo_round;
            default:
                return 0;
        }
    }

    public static int getProductImgId(String productCategory) {
        switch (productCategory) {
            case "Televisions":
                return R.drawable.ic_tv;
            case "Washing Machines":
                return R.drawable.ic_laundry;
            case "Microwaves":
                return R.drawable.ic_microwave;
            case "Refrigerators":
                return R.drawable.ic_refregerator;
            default:
                return 0;
        }
    }

}
