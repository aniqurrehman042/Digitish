package com.example.test_04.ui;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.test_04.R;
import com.example.test_04.async.DownloadImageTask;
import com.example.test_04.models.Chat;
import com.example.test_04.models.CurrentCustomer;
import com.example.test_04.models.Merchant;
import com.example.test_04.models.ProductReview;
import com.example.test_04.models.ProductReviewChat;
import com.example.test_04.ui.fragments.AccountFragment;
import com.example.test_04.ui.fragments.ChatFragment;
import com.example.test_04.ui.fragments.MerchantDetailsFragment;
import com.example.test_04.ui.fragments.MessagesFragment;
import com.example.test_04.ui.fragments.ProductReviewChatFragment;
import com.example.test_04.ui.fragments.ReviewsFragment;
import com.example.test_04.ui.fragments.ScanFragment;
import com.example.test_04.ui.fragments.SearchFragment;
import com.example.test_04.utils.DBUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class CustomerHome extends AppCompatActivity {

    private ImageView ivSearch;
    private ImageView ivThumbs;
    private LinearLayoutCompat llQr;
    private ImageView ivMessages;
    private ImageView ivAccount;
    private LinearLayout llSearch;
    private LinearLayout llThumbs;
    private LinearLayout llMessages;
    private LinearLayout llAccount;
    private FragmentManager fragmentManager;
    private ArrayList<Fragment> fragments = new ArrayList<>();
    private String lastFragment = "Search";
    private String currentFragment = "Search";
    private CardView cvSearch;
    private LinearLayout llSearchIcons;
    private TextView tvTitle;
    private LinearLayout llBarSearch;
    private ConstraintLayout clBottombar;
    private ImageView ivSearchBack;
    private View vBottombarT;
    private EditText etSearch;
    private ImageView ivBack;
    private LinearLayout llCall;
    private LinearLayout llBack;
    private LinearLayout llMore;
    private ConstraintLayout clFragment;

    private Bitmap profileBmp;
    private MerchantDetailsFragment merchantDetailsFragment = new MerchantDetailsFragment();
    private View.OnClickListener logoutMenuListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            PopupMenu popup = new PopupMenu(CustomerHome.this, llMore);
//            //Inflating the Popup using xml file
//            popup.getMenuInflater()
//                    .inflate(R.menu.menu_logout, popup.getMenu());
//
//            //registering popup with OnMenuItemClickListener
//            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                public boolean onMenuItemClick(MenuItem item) {
//                    switch (item.getItemId()) {
//                        case R.id.logout:
//                            signOut();
//                            finish();
//                    }
//                    return true;
//                }
//            });
//
//            popup.show();

            LayoutInflater inflater = (LayoutInflater)
                    getSystemService(LAYOUT_INFLATER_SERVICE);
            final View popupView = inflater.inflate(R.layout.layout_textview_menu, null);

            int width = LinearLayout.LayoutParams.WRAP_CONTENT;
            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
            boolean focusable = true; // lets taps outside the popup also dismiss it
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
            popupWindow.setContentView(popupView);
            popupWindow.showAsDropDown(v, -50, 0);

            popupView.findViewById(R.id.ll_menu).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signOut();
                    finish();
                    popupWindow.dismiss();
                }
            });
        }
    };

    private View.OnClickListener showMerchantMenuListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            PopupMenu popup = new PopupMenu(CustomerHome.this, llMore);
//            //Inflating the Popup using xml file
//            popup.getMenuInflater()
//                    .inflate(R.menu.menu_show_merchant, popup.getMenu());
//
//            //registering popup with OnMenuItemClickListener
//            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                public boolean onMenuItemClick(MenuItem item) {
//                    switch (item.getItemId()) {
//                        case R.id.show_merchant:
//                            fragmentManager.beginTransaction()
//                                    .replace(R.id.cl_fragment, merchantDetailsFragment)
//                                    .addToBackStack("Merchant Details")
//                                    .commit();
//                    }
//                    return true;
//                }
//            });
//
//            popup.show();

            LayoutInflater inflater = (LayoutInflater)
                    getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.layout_textview_menu, null);

            int width = LinearLayout.LayoutParams.WRAP_CONTENT;
            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
            boolean focusable = true; // lets taps outside the popup also dismiss it
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
            popupWindow.setContentView(popupView);
            popupWindow.showAsDropDown(v, -50, 0);

            popupView.findViewById(R.id.ll_menu).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragmentManager.beginTransaction()
                            .replace(R.id.cl_fragment, merchantDetailsFragment)
                            .addToBackStack("Merchant Details")
                            .commit();
                    popupWindow.dismiss();
                }
            });

            ((TextView) popupView.findViewById(R.id.tv_menu)).setText("Show Merchant");
        }
    };

    private View.OnClickListener merchantProfileMenuListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            PopupMenu popup = new PopupMenu(CustomerHome.this, llMore);
//            //Inflating the Popup using xml file
//            popup.getMenuInflater()
//                    .inflate(R.menu.menu_merchant_profile, popup.getMenu());
//
//            //registering popup with OnMenuItemClickListener
//            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                public boolean onMenuItemClick(MenuItem item) {
//                    switch (item.getItemId()) {
//                        case R.id.profile:
//                            fragmentManager.beginTransaction()
//                                    .replace(R.id.cl_fragment, merchantDetailsFragment)
//                                    .addToBackStack("Merchant Details")
//                                    .commit();
//                    }
//                    return true;
//                }
//            });
//
//            popup.show();

            LayoutInflater inflater = (LayoutInflater)
                    getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.layout_textview_menu, null);

            int width = LinearLayout.LayoutParams.WRAP_CONTENT;
            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
            boolean focusable = true; // lets taps outside the popup also dismiss it
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
            popupWindow.setContentView(popupView);
            popupWindow.showAsDropDown(v, -50, 0);

            popupView.findViewById(R.id.ll_menu).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragmentManager.beginTransaction()
                            .replace(R.id.cl_fragment, merchantDetailsFragment)
                            .addToBackStack("Merchant Details")
                            .commit();
                    popupWindow.dismiss();
                }
            });

            ((TextView) popupView.findViewById(R.id.tv_menu)).setText("Profile");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_home);

        init();

    }

    public Bitmap getProfileBmp() {
        return profileBmp;
    }

    public void showBackBtn() {
        ivBack.setVisibility(View.VISIBLE);
        llBack.setVisibility(View.VISIBLE);
    }

    public void hideBackBtn() {
        ivBack.setVisibility(View.GONE);
        llBack.setVisibility(View.GONE);
    }

    public void hideSearchIcons() {
        llSearchIcons.setVisibility(View.GONE);
    }

    public void showSearchIcons() {
        llSearchIcons.setVisibility(View.VISIBLE);
    }

    public void setPageTitle(String title) {
        tvTitle.setText(title);
    }

    public void onProfileResume() {
        setPageTitle("Profile");
        ivBack.setVisibility(View.VISIBLE);
        llBack.setVisibility(View.VISIBLE);
        llSearchIcons.setVisibility(View.VISIBLE);
        llBarSearch.setVisibility(View.GONE);
    }

    public void onProfilePause() {
        ivBack.setVisibility(View.GONE);
        llBack.setVisibility(View.GONE);
        llSearchIcons.setVisibility(View.GONE);
        llBarSearch.setVisibility(View.VISIBLE);
    }

    public void onSearchFragmentPause() {
        tvTitle.setText("Search");
        llSearchIcons.setVisibility(View.GONE);
        llMore.setVisibility(View.VISIBLE);
    }

    public void onProductCategorySearchFragmentResume(String productCategory) {
        tvTitle.setText("Reviews for " + productCategory);
        llSearchIcons.setVisibility(View.GONE);
        llMore.setVisibility(View.GONE);
        showBackBtn();
    }

    public void onSearchFragmentResume() {
        searchBackClick();
        hideBackBtn();
        llSearchIcons.setVisibility(View.VISIBLE);
        llMore.setVisibility(View.GONE);
        tvTitle.setText("Search");
    }

    public void startProductCategorySearch(String productCategory, String merchantName) {
        SearchFragment searchFragment = new SearchFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable("Product Category", productCategory);
        arguments.putSerializable("Merchant Name", merchantName);
        searchFragment.setArguments(arguments);

        fragmentManager
                .beginTransaction()
                .replace(R.id.cl_fragment, searchFragment)
                .addToBackStack("Search")
                .commit();
    }

    public void startChatFragment(ArrayList<Chat> chats, @Nullable Merchant merchant) {

        ChatFragment chatFragment = new ChatFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable("Chats", chats);
        if (merchant != null) {
            arguments.putSerializable("Merchant", merchant);
        }
        chatFragment.setArguments(arguments);

        fragmentManager
                .beginTransaction()
                .replace(R.id.cl_fragment, chatFragment)
                .addToBackStack("Chat")
                .commit();
    }

    public void startProductReviewChatFragment(ArrayList<ProductReviewChat> chats, @Nullable Merchant merchant, ProductReview productReview, boolean fromSearch) {

        if (fromSearch)
            searchBackClick();

        ProductReviewChatFragment productReviewChatFragment = new ProductReviewChatFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable("Chats", chats);
        arguments.putSerializable("Product Review", productReview);
        arguments.putString("Product Category", productReview.getProductCategory());
        arguments.putBoolean("From Search", fromSearch);
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

    public void hideBottomBar() {
        clBottombar.setVisibility(View.GONE);
        vBottombarT.setVisibility(View.GONE);
    }

    public void showBottomBar() {
        clBottombar.setVisibility(View.VISIBLE);
        vBottombarT.setVisibility(View.VISIBLE);
    }

    public void onChatResume(Merchant merchant) {
        ivBack.setVisibility(View.VISIBLE);
        llBack.setVisibility(View.VISIBLE);
        llSearchIcons.setVisibility(View.VISIBLE);
        llCall.setVisibility(View.GONE);
        llBarSearch.setVisibility(View.GONE);

        setMerchantDetailsArguments(merchant);
        setMerchantProfileMenuListener();
    }

    private void setMerchantDetailsArguments(Merchant merchant) {
        Bundle arguments = new Bundle();
        arguments.putSerializable("Merchant", merchant);
        merchantDetailsFragment.setArguments(arguments);
    }

    public void onChatPause() {
        ivBack.setVisibility(View.GONE);
        llBack.setVisibility(View.GONE);
        llSearchIcons.setVisibility(View.GONE);
        llCall.setVisibility(View.GONE);
        llBarSearch.setVisibility(View.VISIBLE);
        clBottombar.setVisibility(View.VISIBLE);
        vBottombarT.setVisibility(View.VISIBLE);

        setLogoutMenuListener();
    }

    private void init() {
        DBUtils.setContext(this);
        setCustomerValues();

        findViews();

        initializeObjects();

        setPageTitle("Search");

        fragmentManager.beginTransaction()
                .add(R.id.cl_fragment, fragments.get(3))
                .addToBackStack("Search")
                .commit();
        fragmentManager.executePendingTransactions();

        setListeners();

        checkNotificationIntent();
    }

    private void checkNotificationIntent() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            boolean review = extras.getBoolean("Review");
            if (review) {
                ProductReview productReview = (ProductReview) extras.getSerializable("Product Review");
                Bundle arguments = new Bundle();
                arguments.putBoolean("Review", true);
                arguments.putSerializable("Product Review", productReview);
                fragments.get(1).setArguments(arguments);
                startFragment(fragments.get(1));
            } else
                startFragment(fragments.get(1));
        }
    }

    private void setCustomerValues() {
        SharedPreferences sp = getSharedPreferences("Customer", MODE_PRIVATE);
        CurrentCustomer.name = sp.getString("Customer Name", "");
        CurrentCustomer.profilePicture = sp.getString("Profile Picture", "");
        CurrentCustomer.points = sp.getString("Points", "");
        CurrentCustomer.email = sp.getString("Email", "");
        CurrentCustomer.phone = sp.getString("Phone", "");
        CurrentCustomer.username = sp.getString("Username", "");
        new DownloadImageTask(profileBmp, null).execute(CurrentCustomer.profilePicture);
    }

    private void initializeObjects() {
        fragmentManager = getSupportFragmentManager();
        initializeFragments();
    }

    private void initializeFragments() {
        fragments.add(new AccountFragment());
        fragments.add(new MessagesFragment());
        fragments.add(new ReviewsFragment());
        fragments.add(new SearchFragment());
        fragments.add(new ScanFragment());
    }

    private void setListeners() {

        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
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

        llThumbs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFragment(fragments.get(2));
            }
        });

        llSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentFragment.equals("Search")) {
                    onSearchClickAgain();
                }

                startFragment(fragments.get(3));
            }
        });

        llBarSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvTitle.setVisibility(View.GONE);
                llSearchIcons.setVisibility(View.GONE);
                cvSearch.setVisibility(View.VISIBLE);
                clBottombar.setVisibility(View.GONE);
                vBottombarT.setVisibility(View.GONE);
                etSearch.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        ivSearchBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBackClick();
            }
        });

        llMore.setOnClickListener(logoutMenuListener);

        llQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(CustomerHome.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CustomerHome.this, new String[]{Manifest.permission.CAMERA}, 0);
                } else {
                    startFragment(fragments.get(4));
                }

            }
        });

    }

    private void onSearchClickAgain() {
        ((SearchFragment) fragments.get(3)).onSearchClickAgain();
    }

    public void setMerchantProfileMenuListener() {
        llMore.setOnClickListener(merchantProfileMenuListener);
    }

    public void setShowMerchantMenuListener() {
        llMore.setOnClickListener(showMerchantMenuListener);
    }

    public void setLogoutMenuListener() {
        llMore.setOnClickListener(logoutMenuListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startFragment(fragments.get(4));
                } else {
                    Toast.makeText(CustomerHome.this, "Camera permission is required to scan QR code", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    private void signOut() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        GoogleSignIn.getClient(this, gso).signOut();
    }

    private void searchBackClick() {
        tvTitle.setVisibility(View.VISIBLE);
        llSearchIcons.setVisibility(View.VISIBLE);
        cvSearch.setVisibility(View.GONE);
        clBottombar.setVisibility(View.VISIBLE);
        vBottombarT.setVisibility(View.VISIBLE);

        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
    }

    private void startFragment(Fragment fragment) {

        resetIcon();

        if (fragment == fragments.get(0))
            currentFragment = "Account";
        else if (fragment == fragments.get(1))
            currentFragment = "Messages";
        else if (fragment == fragments.get(2))
            currentFragment = "Reviews";
        else if (fragment == fragments.get(3))
            currentFragment = "Search";
        else if (fragment == fragments.get(4))
            currentFragment = "Scan";

        setIcon(false);

        if (!currentFragment.equals("Reviews"))
            setFirstRunArguments(fragment);

        try {
            reduceFragmentStack();
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.cl_fragment, fragment, currentFragment)
                    .commitNow();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setFirstRunArguments(Fragment fragment) {
        Bundle arguments = new Bundle();
        arguments.putBoolean("First Run", true);
        if (fragment.getArguments() == null)
            fragment.setArguments(arguments);
    }

    private void resetIcon() {
        lastFragment = currentFragment;
        switch (lastFragment) {
            case "Profile":
            case "Account":
                ivAccount.setColorFilter(ContextCompat.getColor(this, R.color.darkest));
                break;
            case "Messages":
                ivMessages.setColorFilter(ContextCompat.getColor(this, R.color.darkest));
                break;
            case "Reviews":
            case "Product Review":
                ivThumbs.setColorFilter(ContextCompat.getColor(this, R.color.darkest));
                break;
            case "Search":
            case "Merchant Details":
                ivSearch.setColorFilter(ContextCompat.getColor(this, R.color.darkest));
                break;
            case "Product Review Chat":
                ivThumbs.setColorFilter(ContextCompat.getColor(this, R.color.darkest));
                ivSearch.setColorFilter(ContextCompat.getColor(this, R.color.darkest));
                break;
            case "Scan":
                llQr.setBackgroundResource(R.drawable.bg_qr);
                break;
        }
    }

    private void setIcon(boolean isBackpressed) {
        if (isBackpressed)
            currentFragment = lastFragment;
        switch (currentFragment) {
            case "Profile":
            case "Account":
                ivAccount.setColorFilter(ContextCompat.getColor(this, R.color.white));
                break;
            case "Messages":
                ivMessages.setColorFilter(ContextCompat.getColor(this, R.color.white));
                break;
            case "Reviews":
            case "Product Review":
                ivThumbs.setColorFilter(ContextCompat.getColor(this, R.color.white));
                break;
            case "Search":
            case "Merchant Details":
                ivSearch.setColorFilter(ContextCompat.getColor(this, R.color.white));
                break;
            case "Scan":
                llQr.setBackgroundResource(R.drawable.bg_qr_light);
                break;
        }
    }

    private void reduceFragmentStack() {
        try {
            if (fragmentManager.getBackStackEntryCount() > 1) {
                fragmentManager.executePendingTransactions();
                for (int i = fragmentManager.getBackStackEntryCount(); i > 1; i--) {
                    fragmentManager.popBackStack();
                }
            }
            fragmentManager.executePendingTransactions();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void findViews() {
        ivMessages = findViewById(R.id.iv_messages);
        ivThumbs = findViewById(R.id.iv_thumbs);
        ivSearch = findViewById(R.id.iv_search);
        llQr = findViewById(R.id.ll_qr);
        ivAccount = findViewById(R.id.iv_account);
        llAccount = findViewById(R.id.ll_account);
        llMessages = findViewById(R.id.ll_messages);
        llThumbs = findViewById(R.id.ll_thumbs);
        llSearch = findViewById(R.id.ll_search);
        cvSearch = findViewById(R.id.cv_search);
        llSearchIcons = findViewById(R.id.ll_search_icons);
        tvTitle = findViewById(R.id.tv_title);
        llBarSearch = findViewById(R.id.ll_bar_search);
        clBottombar = findViewById(R.id.cl_bottombar);
        ivSearchBack = findViewById(R.id.iv_search_back);
        vBottombarT = findViewById(R.id.v_bottombar_t);
        etSearch = findViewById(R.id.et_search);
        ivBack = findViewById(R.id.iv_back);
        llCall = findViewById(R.id.ll_call);
        llMore = findViewById(R.id.ll_more);
        llBack = findViewById(R.id.ll_back);
        clFragment = findViewById(R.id.cl_fragment);
    }

    @Override
    public void onBackPressed() {
        if ((fragmentManager.getBackStackEntryCount() < 2)) {
            finish();
        }
        if (cvSearch.getVisibility() == View.VISIBLE) {
            searchBackClick();
            return;
        }
//        resetIcon();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.executePendingTransactions();
            fragmentManager.popBackStack();
        } else
            super.onBackPressed();
//        setIcon(true);
    }

    public void startMerchantDetailsFragment(Merchant merchant) {

        MerchantDetailsFragment merchantDetailsFragment = new MerchantDetailsFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable("Merchant", merchant);
        merchantDetailsFragment.setArguments(arguments);

        fragmentManager
                .beginTransaction()
                .replace(R.id.cl_fragment, merchantDetailsFragment)
                .addToBackStack("Merchant Details")
                .commit();
    }

    public void onScanResume() {
        setPageTitle("Scan");
    }

    public void loadReview(String qrCode) {
        Bundle arguments = new Bundle();
        arguments.putString("QR", qrCode);
        fragments.get(2).setArguments(arguments);

        startFragment(fragments.get(2));
    }

    public void onProductReviewResume(String title) {
        setPageTitle(title);
        showBackBtn();
    }

    public void startProductReviewsThenProductReviewChat(ArrayList<ProductReviewChat> productReviewChats, Merchant merchant, ProductReview productReview, boolean fromSearch) {
        if (fromSearch)
            searchBackClick();

        Bundle arguments = new Bundle();
        arguments.putSerializable("Chats", productReviewChats);
        arguments.putSerializable("Product Review", productReview);
        arguments.putString("Product Category", productReview.getProductCategory());
        arguments.putBoolean("From Search", fromSearch);
        if (merchant != null) {
            arguments.putSerializable("Merchant", merchant);
        }
        fragments.get(2).setArguments(arguments);
        startFragment(fragments.get(2));
    }
}
