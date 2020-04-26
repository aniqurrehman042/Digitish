package com.example.test_04.ui.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.test_04.R;
import com.example.test_04.adapters.InboxAdapter;
import com.example.test_04.models.Chat;
import com.example.test_04.models.CurrentCustomer;
import com.example.test_04.ui.CustomerHome;
import com.example.test_04.utils.DateUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
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
public class InboxFragment extends Fragment {

    private RecyclerView rvInbox;

    private CustomerHome customerHome;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private InboxAdapter adapter;
    private ProgressDialog progressDialog;
    private final ArrayList<Chat> chatsWithLastMsg = new ArrayList<>();
    private LinearLayout llMessages;

    public InboxFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);

        customerHome = (CustomerHome) getActivity();

        findViews(view);

        init();

        return view;
    }

    private void init() {
        setRecyclerView();

        setListeners();
    }

    private void setListeners() {

    }

    private void setRecyclerView() {
        if (chatsWithLastMsg.isEmpty()) {
            progressDialog = new ProgressDialog(customerHome);
            progressDialog.setCancelable(false);
            progressDialog.setTitle("Loading chat");
            progressDialog.show();
            setRecyclerAdapter(CurrentCustomer.email);
        } else {
            adapter = new InboxAdapter(customerHome, chatsWithLastMsg);
            rvInbox.setLayoutManager(new LinearLayoutManager(getContext()));
            rvInbox.setAdapter(adapter);
        }
    }

    private void findViews(View view) {
        rvInbox = view.findViewById(R.id.rv_indox);
        llMessages = view.findViewById(R.id.ll_messages);
    }

    private void setRecyclerAdapter(String email) {

        db.collection("Chat")
                .whereEqualTo("Customer Email", email)
                .orderBy("Date", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (queryDocumentSnapshots != null) {
                            if (queryDocumentSnapshots.getDocuments().size() > 0) {

                                final ArrayList<Chat> chats = new ArrayList<>();
                                final Set<String> merchantEmails = new HashSet<>();
                                final ArrayList<ArrayList<Chat>> chatsWithEachCustomer = new ArrayList<>();
                                final ArrayList<ArrayList<Date>> chatsWithEachCustomerDates = new ArrayList<>();
                                chats.clear();
                                merchantEmails.clear();
                                chatsWithEachCustomer.clear();
                                chatsWithEachCustomerDates.clear();
                                chatsWithLastMsg.clear();

                                llMessages.setVisibility(View.VISIBLE);

                                for (DocumentSnapshot chat : queryDocumentSnapshots.getDocuments()) {
                                    Timestamp timestamp = (Timestamp) chat.get("Date");
                                    Date dateObj = timestamp.toDate();
                                    String date = DateUtils.dateToStringWithTime(dateObj);
                                    String merchantEmail = chat.get("Merchant Email").toString();
                                    merchantEmails.add(merchantEmail);
                                    chats.add(new Chat(chat.get("Customer Email").toString(), chat.get("Customer Name").toString(), merchantEmail, chat.get("Merchant Name").toString(), chat.get("Image").toString(), chat.get("Message").toString(), chat.get("Sender").toString(), chat.getBoolean("Read"), date));
                                }

                                for (String merchantEmail : merchantEmails) {
                                    ArrayList<Chat> chatEach = new ArrayList<>();
                                    ArrayList<Date> chatEachDate = new ArrayList<>();

                                    for (Chat chat : chats) {
                                        if (chat.getMerchantEmail().equals(merchantEmail)) {
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

                                InboxAdapter adapter = new InboxAdapter(customerHome, chatsWithLastMsg);
                                rvInbox.setLayoutManager(new LinearLayoutManager(getContext()));
                                rvInbox.setAdapter(adapter);
                            } else {
                                llMessages.setVisibility(View.GONE);
                            }
                        } else {
                            Toast.makeText(customerHome, "Unable to load chat", Toast.LENGTH_SHORT).show();
                            llMessages.setVisibility(View.GONE);
                        }

                        Collections.sort(chatsWithLastMsg, Collections.<Chat>reverseOrder());
                        progressDialog.dismiss();
                    }
                });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressDialog != null)
            progressDialog.dismiss();
    }

}
