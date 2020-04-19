package com.example.test_04.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.test_04.R;
import com.example.test_04.models.Chat;
import com.example.test_04.models.CurrentMerchant;
import com.example.test_04.models.Customer;
import com.example.test_04.models.Merchant;
import com.example.test_04.models.ProductReview;
import com.example.test_04.models.ProductReviewChat;
import com.example.test_04.ui.fragments.ChatFragment;
import com.example.test_04.ui.fragments.MerchantAccountFragment;
import com.example.test_04.ui.fragments.MerchantDashboardFragment;
import com.example.test_04.ui.fragments.MerchantMessagesFragment;
import com.example.test_04.ui.fragments.MerchantNotificationsFragment;
import com.example.test_04.ui.fragments.MerchantReviewsFragment;
import com.example.test_04.ui.fragments.ProductReviewChatFragment;
import com.example.test_04.utils.DBUtils;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import javax.annotation.Nullable;

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
    private LinearLayout llBottombar;

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
            if (extras.getBoolean("Review"))
                startFragment(fragments.get(2));
            else
                startFragment(fragments.get(1));
        }
    }

    private void setMerchantValues() {
        SharedPreferences sp = getSharedPreferences("Merchant", MODE_PRIVATE);
        String[] keys = {"Merchant Name", "Merchant Description", "Email", "Products", "Merchant Rating"};
        CurrentMerchant.name = sp.getString(keys[0], "");
        CurrentMerchant.description = sp.getString(keys[1], "");
        CurrentMerchant.email = sp.getString(keys[2], "");
        CurrentMerchant.rating = sp.getString(keys[4], "");
        CurrentMerchant.products = sp.getString(keys[3], "");
    }

    public void hideSearchIcons() {
        llSearchIcons.setVisibility(View.GONE);
    }

    public void onChatResume() {
        ivBack.setVisibility(View.VISIBLE);
        llSearchIcons.setVisibility(View.VISIBLE);
        llBottombar.setVisibility(View.GONE);
        llMore.setVisibility(View.GONE);
    }

    public void onChatPause() {
        ivBack.setVisibility(View.GONE);
        llSearchIcons.setVisibility(View.GONE);
        llBottombar.setVisibility(View.VISIBLE);
        llMore.setVisibility(View.VISIBLE);
    }

    public void onMerchantAccountResume() {
        setPageTitle("Account");
        llSearchIcons.setVisibility(View.VISIBLE);
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

        setIcon(false);

        if (!(lastFragment.equals(currentFragment))) {
            reduceFragmentStack();
            if (!(currentFragment.equals("Merchant Account")))
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.cl_fragment, fragment)
                        .addToBackStack(currentFragment)
                        .commit();
        }
    }

    private void resetIcon() {
        this.lastFragment = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName();
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
        }
    }

    private void setIcon(boolean isBackpressed) {
        if (isBackpressed && fragmentManager.getBackStackEntryCount() > 0)
            currentFragment = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName();
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
        }
    }

    private void reduceFragmentStack() {
        if (fragmentManager.getBackStackEntryCount() > 1)
            for (int i = fragmentManager.getBackStackEntryCount(); i > 1; i--) {
                fragmentManager.popBackStack();
            }
    }

    public void setPageTitle(String title) {
        tvTitle.setText(title);
    }

    private void showBackBtn() {
        ivBack.setVisibility(View.VISIBLE);
    }

    private void hideBackBtn() {
        ivBack.setVisibility(View.GONE);
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

        llMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(MerchantHome.this, llMore);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                        .inflate(R.menu.menu_logout, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.logout:
                                FirebaseAuth.getInstance().signOut();
                                finish();
                        }
                        return true;
                    }
                });

                popup.show();
            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
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
        llBottombar = findViewById(R.id.ll_bottombar);
        llSearchIcons = findViewById(R.id.ll_search_icons);
        llMore = findViewById(R.id.ll_more);
    }

    public void hideBottomBar() {
        llBottombar.setVisibility(View.GONE);
    }

    public void showBottomBar() {
        llBottombar.setVisibility(View.VISIBLE);
    }

    private void setStatusBar() {
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.darker_darkest));
    }

    @Override
    public void onBackPressed() {
        if ((getSupportFragmentManager().getBackStackEntryCount() < 2)) {
            getSupportFragmentManager().popBackStack();
        }

        resetIcon();
        super.onBackPressed();
        setIcon(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    public void startProductReviewChatFragment(ArrayList<ProductReviewChat> chats, @Nullable Merchant merchant, ProductReview productReview, String productCategory, Customer customer, boolean fromSearch) {

        ProductReviewChatFragment productReviewChatFragment = new ProductReviewChatFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable("Chats", chats);
        arguments.putSerializable("Customer", customer);
        arguments.putSerializable("Product Review", productReview);
        arguments.putString("Product Category", productCategory);
        if (merchant != null) {
            arguments.putSerializable("Merchant", merchant);
        }
        productReviewChatFragment.setArguments(arguments);

        fragmentManager
                .beginTransaction()
                .replace(R.id.cl_fragment, productReviewChatFragment)
                .addToBackStack("Product Review Chat")
                .commit();
    }

    public void startMerchantReviewsFragment(ArrayList<ProductReview> productReviews) {

        MerchantReviewsFragment merchantReviewsFragment = new MerchantReviewsFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable("Product Reviews", productReviews);
        merchantReviewsFragment.setArguments(arguments);

        fragmentManager
                .beginTransaction()
                .replace(R.id.cl_fragment, merchantReviewsFragment)
                .addToBackStack("Product Review Chat")
                .commit();
    }
}