package com.example.test_04.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.test_04.R;
import com.example.test_04.models.Chat;
import com.example.test_04.models.CurrentMerchant;
import com.example.test_04.models.Customer;
import com.example.test_04.models.ProductReview;
import com.example.test_04.models.ProductReviewChat;
import com.example.test_04.ui.fragments.ChatFragment;
import com.example.test_04.ui.fragments.MerchantAccountFragment;
import com.example.test_04.ui.fragments.MerchantDashboardFragment;
import com.example.test_04.ui.fragments.MerchantMessagesFragment;
import com.example.test_04.ui.fragments.MerchantNotificationsFragment;
import com.example.test_04.ui.fragments.MerchantReviewsFragment;
import com.example.test_04.ui.fragments.ProductReviewChatFragment;
import com.example.test_04.ui.fragments.SearchFragment;
import com.example.test_04.utils.DBUtils;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class MerchantHome extends AppCompatActivity {

    private TextView tvTitle;
    private ImageView ivDashboard;
    private ImageView ivNotifications;
    private ImageView ivMessages;
    private ImageView ivAccount;
    private ImageView ivBack;
    private LinearLayout llDashboard;
    private LinearLayout llNotifications;
    private LinearLayout llMessages;
    private LinearLayout llAccount;
    private LinearLayout llSearchIcons;
    private LinearLayout llMore;
    private LinearLayout llReviews;
    private LinearLayout llBack;
    private ConstraintLayout clBottombar;
    private View vBottombarT;

    private String currentFragment;
    private String lastFragment;
    private ArrayList<Fragment> fragments = new ArrayList<>();
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_home);

        init();

    }

    private void init() {
        DBUtils.setContext(this);
        setMerchantValues();
        findViews();
        setListeners();

        setStatusBar();
        setPageTitle("Account");
        populateFragments();
        checkNotificationIntent();
    }

    private void checkNotificationIntent() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            boolean review = extras.getBoolean("Review");
            if (review) {
                ProductReview productReview = (ProductReview) extras.getSerializable("Product Review");
                Bundle arguments = new Bundle();
                arguments.putSerializable("Product Review", productReview);
                fragments.get(2).setArguments(arguments);
                startFragment(fragments.get(2));
            }
            else {
                startFragment(fragments.get(1));
            }
        }
    }

    private void setMerchantValues() {
        SharedPreferences sp = getSharedPreferences("Merchant", MODE_PRIVATE);
        String[] keys = {"Merchant Name", "Merchant Description", "Email", "Products", "Merchant Rating", "Website"};
        CurrentMerchant.name = sp.getString(keys[0], "");
        CurrentMerchant.description = sp.getString(keys[1], "");
        CurrentMerchant.email = sp.getString(keys[2], "");
        CurrentMerchant.rating = sp.getString(keys[4], "");
        CurrentMerchant.products = sp.getString(keys[3], "");
        CurrentMerchant.website = sp.getString(keys[4], "");
    }

    public void hideSearchIcons() {
        llSearchIcons.setVisibility(View.GONE);
    }

    public void onChatResume() {
        ivBack.setVisibility(View.VISIBLE);
        llBack.setVisibility(View.VISIBLE);
        llSearchIcons.setVisibility(View.VISIBLE);
        hideBottomBar();
        llMore.setVisibility(View.GONE);
    }

    public void onChatPause() {
        ivBack.setVisibility(View.GONE);
        llBack.setVisibility(View.GONE);
        llSearchIcons.setVisibility(View.GONE);
        showBottomBar();
        llMore.setVisibility(View.VISIBLE);
    }

    public void onProductCategoryMerchantReviewsFragmentResume(String productCategory) {
        tvTitle.setText("Reviews for " + productCategory);
        llSearchIcons.setVisibility(View.GONE);
        llMore.setVisibility(View.GONE);
        showBackBtn();
    }

    public void onMerchantReviewsFragmentPause() {
        llSearchIcons.setVisibility(View.GONE);
        llMore.setVisibility(View.GONE);
        hideBackBtn();
    }

    public void onMerchantReviewsFragmentResume() {
        tvTitle.setText("Reviews");
        llSearchIcons.setVisibility(View.GONE);
        llMore.setVisibility(View.GONE);
        hideBackBtn();
    }

    public void onMerchantAccountResume() {
        setPageTitle("Account");
        llSearchIcons.setVisibility(View.VISIBLE);
        llMore.setVisibility(View.VISIBLE);
    }

    public void onMerchantAccountPause() {
        llSearchIcons.setVisibility(View.GONE);
    }

    private void populateFragments() {
        currentFragment = lastFragment = "Merchant Account";
        fragmentManager = getSupportFragmentManager();

        fragments.add(new MerchantAccountFragment());
        fragments.add(new MerchantMessagesFragment());
        fragments.add(new MerchantNotificationsFragment());
        fragments.add(new MerchantDashboardFragment());
        fragments.add(new MerchantReviewsFragment());

        fragmentManager.beginTransaction()
                .add(R.id.cl_fragment, fragments.get(0))
                .addToBackStack("Merchant Account")
                .commit();
        fragmentManager.executePendingTransactions();
    }

    public void startChatFragment(ArrayList<Chat> chats) {

        ChatFragment chatFragment = new ChatFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable("Chats", chats);
        chatFragment.setArguments(arguments);

        fragmentManager
                .beginTransaction()
                .replace(R.id.cl_fragment, chatFragment)
                .addToBackStack("Chat")
                .commit();
    }

    public void startProductCategorySearch(String productCategory, String merchantName) {
        MerchantReviewsFragment merchantReviewsFragment = new MerchantReviewsFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable("Product Category", productCategory);
        arguments.putSerializable("Merchant Name", merchantName);
        merchantReviewsFragment.setArguments(arguments);

        fragmentManager
                .beginTransaction()
                .replace(R.id.cl_fragment, merchantReviewsFragment)
                .addToBackStack("Merchant Reviews")
                .commit();
    }

    private void startFragment(Fragment fragment) {

        resetIcon();

        if (fragment == fragments.get(0))
            currentFragment = "Merchant Account";
        else if (fragment == fragments.get(1))
            currentFragment = "Merchant Messages";
        else if (fragment == fragments.get(2))
            currentFragment = "Merchant Notifications";
        else if (fragment == fragments.get(3))
            currentFragment = "Merchant Dashboard";
        else if (fragment == fragments.get(4))
            currentFragment = "Merchant Reviews";

        setIcon(false);

        try {
//            if (!(lastFragment.equals(currentFragment))) {
                reduceFragmentStack();
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.cl_fragment, fragment)
                        .commitNow();
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetIcon() {
//        if (fragmentManager.getBackStackEntryCount() > 0)
//            this.lastFragment = fragmentManage(r.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName();
        lastFragment = currentFragment;
        switch (lastFragment) {
            case "Merchant Account":
                ivAccount.setColorFilter(ContextCompat.getColor(this, R.color.dark_yellow));
                break;
            case "Merchant Messages":
                ivMessages.setColorFilter(ContextCompat.getColor(this, R.color.dark_yellow));
                break;
            case "Merchant Notifications":
                ivNotifications.setColorFilter(ContextCompat.getColor(this, R.color.dark_yellow));
                break;
            case "Merchant Dashboard":
                ivDashboard.setColorFilter(ContextCompat.getColor(this, R.color.dark_yellow));
                break;
            case "Merchant Reviews":
                llReviews.setBackgroundResource(R.drawable.bg_reviews);
                break;
        }
    }

    private void setIcon(boolean isBackpressed) {
        if (isBackpressed)
//            currentFragment = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName();
            currentFragment = lastFragment;
        switch (currentFragment) {
            case "Merchant Account":
                ivAccount.setColorFilter(ContextCompat.getColor(this, R.color.light_yellow));
                break;
            case "Merchant Messages":
                ivMessages.setColorFilter(ContextCompat.getColor(this, R.color.light_yellow));
                break;
            case "Merchant Notifications":
                ivNotifications.setColorFilter(ContextCompat.getColor(this, R.color.light_yellow));
                break;
            case "Merchant Dashboard":
                ivDashboard.setColorFilter(ContextCompat.getColor(this, R.color.light_yellow));
                break;
            case "Merchant Reviews":
                llReviews.setBackgroundResource(R.drawable.bg_reviews_light);
                break;
        }
    }

    private void reduceFragmentStack() {
        try {
            if (fragmentManager.getBackStackEntryCount() > 1)
                for (int i = fragmentManager.getBackStackEntryCount(); i > 1; i--) {
                    fragmentManager.popBackStack();
                }
            fragmentManager.executePendingTransactions();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPageTitle(String title) {
        tvTitle.setText(title);
    }

    private void showBackBtn() {
        ivBack.setVisibility(View.VISIBLE);
        llBack.setVisibility(View.VISIBLE);
    }

    private void hideBackBtn() {
        ivBack.setVisibility(View.GONE);
        llBack.setVisibility(View.GONE);
    }

    private void setListeners() {
        llAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFragment(fragments.get(0));
            }
        });

        llMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFragment(fragments.get(1));
            }
        });

        llNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFragment(fragments.get(2));
            }
        });

        llDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFragment(fragments.get(3));
            }
        });
        llReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("First Run", true);
                fragments.get(4).setArguments(bundle);
                ((MerchantReviewsFragment)fragments.get(4)).setMerchantReviewsLoadedOnce(false);
                startFragment(fragments.get(4));
            }
        });

        llMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                PopupMenu popup = new PopupMenu(MerchantHome.this, llMore);
//                //Inflating the Popup using xml file
//                popup.getMenuInflater()
//                        .inflate(R.menu.menu_logout, popup.getMenu());
//
//                //registering popup with OnMenuItemClickListener
//                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                    public boolean onMenuItemClick(MenuItem item) {
//                        switch (item.getItemId()) {
//                            case R.id.logout:
//                                FirebaseAuth.getInstance().signOut();
//                                finish();
//                        }
//                        return true;
//                    }
//                });
//
//                popup.show();

                LayoutInflater inflater = (LayoutInflater)
                        getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.layout_textview_menu, null);

                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
                popupWindow.setOutsideTouchable(true);
                popupWindow.setContentView(popupView);
                popupWindow.showAsDropDown(v, -50, 0);

                popupView.findViewById(R.id.ll_menu).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseAuth.getInstance().signOut();
                        finish();
                        popupWindow.dismiss();
                    }
                });

                ((TextView)popupView.findViewById(R.id.tv_menu)).setText("Logout");
            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        llBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void findViews() {
        tvTitle = findViewById(R.id.tv_title);
        ivDashboard = findViewById(R.id.iv_dashboard);
        ivNotifications = findViewById(R.id.iv_notifications);
        ivAccount = findViewById(R.id.iv_account);
        ivMessages = findViewById(R.id.iv_messages);
        llDashboard = findViewById(R.id.ll_dashboard);
        llNotifications = findViewById(R.id.ll_notifications);
        llAccount = findViewById(R.id.ll_account);
        llMessages = findViewById(R.id.ll_messages);
        ivBack = findViewById(R.id.iv_back);
        clBottombar = findViewById(R.id.cl_bottombar);
        llSearchIcons = findViewById(R.id.ll_search_icons);
        llMore = findViewById(R.id.ll_more);
        llBack = findViewById(R.id.ll_back);
        llReviews = findViewById(R.id.ll_reviews);
        vBottombarT = findViewById(R.id.v_bottombar_t);
    }

    public void hideBottomBar() {
        clBottombar.setVisibility(View.GONE);
        vBottombarT.setVisibility(View.GONE);
    }

    public void showBottomBar() {
        clBottombar.setVisibility(View.VISIBLE);
        vBottombarT.setVisibility(View.VISIBLE);
    }

    private void setStatusBar() {
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.darker_darkest));
    }

    @Override
    public void onBackPressed() {
        if ((getSupportFragmentManager().getBackStackEntryCount() < 2)) {
            fragmentManager.executePendingTransactions();
            getSupportFragmentManager().popBackStack();
        }
        fragmentManager.executePendingTransactions();

//        resetIcon();
        super.onBackPressed();
//        setIcon(true);
    }

    public void startProductReviewChatFragment(ArrayList<ProductReviewChat> chats, ProductReview productReview, Customer customer) {

        ProductReviewChatFragment productReviewChatFragment = new ProductReviewChatFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable("Chats", chats);
        arguments.putSerializable("Customer", customer);
        arguments.putSerializable("Product Review", productReview);
        arguments.putBoolean("From Search", true);
        arguments.putString("Product Category", productReview.getProductCategory());
        productReviewChatFragment.setArguments(arguments);

        fragmentManager
                .beginTransaction()
                .replace(R.id.cl_fragment, productReviewChatFragment)
                .addToBackStack("Product Review Chat")
                .commit();
    }
}
