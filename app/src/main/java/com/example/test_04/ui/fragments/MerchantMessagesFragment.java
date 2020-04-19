package com.example.test_04.ui.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.test_04.R;
import com.example.test_04.adapters.MerchantInboxAdapter;
import com.example.test_04.models.Chat;
import com.example.test_04.models.CurrentMerchant;
import com.example.test_04.ui.MerchantHome;
import com.example.test_04.utils.DateUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 */
public class MerchantMessagesFragment extends Fragment {

    private RecyclerView rvInbox;

    private MerchantHome merchantHome;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private MerchantInboxAdapter adapter;
    private ProgressDialog progressDialog;
    private final ArrayList<Chat> chatsWithLastMsg = new ArrayList<>();
    private LinearLayout llMessages;

    public MerchantMessagesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_merchant_messages, container, false);

        merchantHome = (MerchantHome) getActivity();

        findViews(view);

        setRecyclerView();



        return view;
    }

    private void findViews(View view) {
        rvInbox = view.findViewById(R.id.rv_indox);
        llMessages = view.findViewById(R.id.ll_messages);
    }

    private void setRecyclerView() {
        if (chatsWithLastMsg.isEmpty()) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setCancelable(false);
            progressDialog.setTitle("Loading chat");
            progressDialog.show();
            setRecyclerAdapter(CurrentMerchant.email);
        } else {
            rvInbox.setLayoutManager(new LinearLayoutManager(getContext()));
            rvInbox.setAdapter(adapter);
        }
    }

    private void setRecyclerAdapter(String email) {
        final ArrayList<Chat> chats = new ArrayList<>();
        final Set<String> customerEmails = new HashSet<>();
        final ArrayList<ArrayList<Chat>> chatsWithEachCustomer = new ArrayList<>();
        final ArrayList<ArrayList<Date>> chatsWithEachCustomerDates = new ArrayList<>();

        db.collection("Chat")
                .whereEqualTo("Merchant Email", email)
                .orderBy("Date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().getDocuments().size() > 0) {

                                llMessages.setVisibility(View.VISIBLE);

                                for (DocumentSnapshot chat : task.getResult().getDocuments()) {
                                    Timestamp timestamp = (Timestamp) chat.get("Date");
                                    Date dateObj = timestamp.toDate();
                                    String date = DateUtils.dateToStringWithTime(dateObj);
                                    String customerEmail = chat.get("Customer Email").toString();
                                    customerEmails.add(customerEmail);
                                    chats.add(new Chat(customerEmail, chat.get("Customer Name").toString(), chat.get("Merchant Email").toString(), chat.get("Merchant Name").toString(), chat.get("Image").toString(), chat.get("Message").toString(), chat.get("Sender").toString(), chat.getBoolean("Read"), date));
                                }

                                for (String customerEmail : customerEmails) {
                                    ArrayList<Chat> chatEach = new ArrayList<>();
                                    ArrayList<Date> chatEachDate = new ArrayList<>();

                                    for (Chat chat : chats) {
                                        if (chat.getCustomerEmail().equals(customerEmail)) {
                                            chatEach.add(chat);
                                            chatEachDate.add(DateUtils.stringToDateWithTime(chat.getDate()));
                                        }
                                    }
                                    chatsWithEachCustomerDates.add(chatEachDate);
                                    chatsWithEachCustomer.add(chatEach);
                                }

                                for (int i = 0; i < chatsWithEachCustomer.size(); i++) {
                                    Date lastDate = Collections.max(chatsWithEachCustomerDates.get(i));
                                    int LastChatIndex = chatsWithEachCustomerDates.get(i).indexOf(lastDate);
                                    Chat lastChat = chatsWithEachCustomer.get(i).get(LastChatIndex);
                                    chatsWithLastMsg.add(lastChat);
                                }

                                adapter = new MerchantInboxAdapter((MerchantHome) getActivity(), chatsWithLastMsg);
                                rvInbox.setLayoutManager(new LinearLayoutManager(getContext()));
                                rvInbox.setAdapter(adapter);
                            } else {
                                llMessages.setVisibility(View.GONE);
                            }
                        } else {
                            Toast.makeText(merchantHome, "Unable to load chat", Toast.LENGTH_SHORT).show();
                            llMessages.setVisibility(View.GONE);
                        }

                        Collections.sort(chatsWithLastMsg, Collections.<Chat>reverseOrder());
                        progressDialog.dismiss();
                    }
                });

    }

    @Override
    public void onResume() {
        super.onResume();

        merchantHome.setPageTitle("Messages");

    }
}
