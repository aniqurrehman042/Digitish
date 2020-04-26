package com.example.test_04.ui.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.test_04.R;
import com.example.test_04.adapters.ChatAdapter;
import com.example.test_04.adapters.SendImagesAdapter;
import com.example.test_04.db_callbacks.GetMerchantCallback;
import com.example.test_04.models.Chat;
import com.example.test_04.models.CurrentCustomer;
import com.example.test_04.models.Merchant;
import com.example.test_04.ui.CustomerHome;
import com.example.test_04.ui.MerchantHome;
import com.example.test_04.utils.DBUtils;
import com.example.test_04.utils.FCMUtils;
import com.fasterxml.uuid.Generators;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickCancel;
import com.vansuita.pickimage.listeners.IPickResult;

import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private RecyclerView rvChat;
    private RecyclerView rvSendImages;
    private LinearLayout llSend;
    private EditText etMessage;
    private ImageView ivAddPic;

    private CustomerHome customerHome;
    private MerchantHome merchantHome;
    private ChatAdapter chatAdapter;
    private FirebaseFirestore db;
    private ArrayList<Chat> chats = new ArrayList<>();
    private ArrayList<Bitmap> bitmaps = new ArrayList<>();
    private Merchant merchant;
    private LinearLayoutManager clm;
    private SendImagesAdapter imagesAdapter;
    private ProgressDialog progressDialog;
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getActivity().getClass().getSimpleName().equals("CustomerHome"))
            customerHome = (CustomerHome) getActivity();
        else
            merchantHome = (MerchantHome) getActivity();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        findViews(view);

        init();

        return view;
    }

    private void init() {
        getChats();
        setUpRecyclers();
        setListeners();
        db = FirebaseFirestore.getInstance();
    }

    private void getChats() {
        chats = (ArrayList<Chat>) getArguments().getSerializable("Chats");
        merchant = (Merchant) getArguments().getSerializable("Merchant");
    }

    private void setUpRecyclers() {
        imagesAdapter = new SendImagesAdapter(bitmaps, rvSendImages);
        LinearLayoutManager lm = new LinearLayoutManager(getContext());
        lm.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvSendImages.setLayoutManager(lm);
        rvSendImages.setAdapter(imagesAdapter);

        if (customerHome == null)
            chatAdapter = new ChatAdapter(chats, true);
        else
            chatAdapter = new ChatAdapter(chats, false);
        clm = new LinearLayoutManager(getContext());
        clm.setReverseLayout(true);
        rvChat.setLayoutManager(clm);
        rvChat.setAdapter(chatAdapter);
        clm.scrollToPosition(0);
    }

    private void findViews(View view) {
        rvSendImages = view.findViewById(R.id.rv_send_images);
        rvChat = view.findViewById(R.id.rv_chat);
        llSend = view.findViewById(R.id.ll_send);
        etMessage = view.findViewById(R.id.et_message);
        ivAddPic = view.findViewById(R.id.iv_add_pic);
    }

    private void setListeners() {
        llSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendImages();

                String message = etMessage.getText().toString();

                if (message.equals("")) {
                    return;
                }

                Chat chat = getChatObject(message, "");

                chats.add(0, chat);
                chatAdapter.notifyItemInserted(0);
                chatAdapter.notifyItemRangeChanged(chats.indexOf(chat), chats.size());
                clm.scrollToPosition(0);

                uploadChat(chat);

                etMessage.setText("");
            }
        });

        ivAddPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PickImageDialog.build(new PickSetup())
                        .setOnPickResult(new IPickResult() {
                            @Override
                            public void onPickResult(PickResult r) {
                                rvSendImages.setVisibility(View.VISIBLE);
                                bitmaps.add(r.getBitmap());
                                imagesAdapter.notifyItemInserted(bitmaps.size() - 1);
                            }
                        })
                        .setOnPickCancel(new IPickCancel() {
                            @Override
                            public void onCancelClick() {
                                //TODO: do what you have to if user clicked cancel
                            }
                        }).show(getActivity().getSupportFragmentManager());
            }
        });
    }

    private Chat getChatObject(String message, String image) {

        Date dateObj = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy - HH:mm");
        String date = sdf.format(dateObj);
        String sender = null;
        if (customerHome != null)
            sender = "Customer";
        else
            sender = "Merchant";

        String customerEmail;
        String customerName;
        String merchantEmail;
        String merchantName;

        if (!chats.isEmpty()) {
            customerEmail = chats.get(0).getCustomerEmail();
            customerName = chats.get(0).getCustomerName();
            merchantEmail = chats.get(0).getMerchantEmail();
            merchantName = chats.get(0).getMerchantName();
        } else {
            customerEmail = CurrentCustomer.email;
            customerName = CurrentCustomer.name;
            merchantEmail = merchant.getEmail();
            merchantName = merchant.getName();
        }

        Chat chat = new Chat(customerEmail, customerName, merchantEmail, merchantName, image, message, sender, false, date);
        return chat;
    }

    private void sendImages() {

        if (bitmaps.isEmpty())
            return;

        rvSendImages.setVisibility(View.GONE);

        for (Bitmap bitmap : bitmaps) {

            UUID uuid = Generators.timeBasedGenerator().generate();
            StorageReference storageRef = storage.getReference();
            StorageReference imageRef = storageRef.child("chatimages/" + uuid);

            final Chat chat = getChatObject("", uuid.toString());
            chat.setBitmap(bitmap);

            chats.add(0, chat);
            int chatIndex = chats.indexOf(chat);
            chatAdapter.notifyItemInserted(chatIndex);
            chatAdapter.notifyItemRangeChanged(chatIndex, chats.size());
            clm.scrollToPosition(0);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = imageRef.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    chat.setSent(false);
                    int newChatIndex = chats.indexOf(chat);
                    chatAdapter.notifyItemChanged(newChatIndex);
                    chatAdapter.notifyItemRangeChanged(newChatIndex, chats.size());
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    uploadChat(chat);
                }
            });
        }

        bitmaps.clear();
        imagesAdapter.notifyDataSetChanged();

    }

    private void uploadChat(final Chat chat) {

        Date dateObj = Calendar.getInstance().getTime();
        Timestamp dateTimestamp = new Timestamp(dateObj);

        Map<String, Object> data = new HashMap<>();
        data.put("Customer Email", chat.getCustomerEmail());
        data.put("Customer Name", chat.getCustomerName());
        data.put("Merchant Email", chat.getMerchantEmail());
        data.put("Merchant Name", chat.getMerchantName());
        data.put("Date", dateTimestamp);
        data.put("Message", chat.getMessage());
        data.put("Sender", chat.getSender());
        data.put("Read", chat.isRead());
        data.put("Image", chat.getImage());

        db.collection("Chat")
                .add(data)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            boolean receiverMerchant = true;
                            String name = chat.getCustomerName();
                            Context context = customerHome;
                            String receiverNameOrEmail = chat.getMerchantName();
                            String senderNameOrEmail = chat.getCustomerEmail();
                            if (customerHome == null) {
                                receiverMerchant = false;
                                name = chat.getMerchantName();
                                context = merchantHome;
                                receiverNameOrEmail = chat.getCustomerEmail();
                                senderNameOrEmail = chat.getMerchantName();
                            }

                            FCMUtils.Companion.sendMessage(context, false, receiverMerchant, name, chat.getMessage(), receiverNameOrEmail, senderNameOrEmail);
                        } else {
                            chat.setSent(false);
                            chatAdapter.notifyItemChanged(chats.indexOf(chat));
                        }
                    }
                });
    }

    private void showProgressDialog(String title) {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle(title);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (customerHome != null) {
            customerHome.hideBottomBar();
            if (!chats.isEmpty()) {
                String merchantName = chats.get(0).getMerchantName();
                customerHome.setPageTitle(merchantName);

                showProgressDialog("Loading merchant");
                DBUtils.getMerchant(merchantName, new GetMerchantCallback() {
                    @Override
                    public void onCallback(@Nullable Merchant merchant, boolean successful) {
                        if (successful) {
                            customerHome.onChatResume(merchant);
                            if (ChatFragment.this.merchant == null)
                                ChatFragment.this.merchant = merchant;
                        } else {
                            Toast.makeText(customerHome, "Failed to load merchant profile", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });
            } else {
                customerHome.setPageTitle(merchant.getName());
                customerHome.onChatResume(merchant);
            }
        } else {
            merchantHome.setPageTitle(chats.get(0).getCustomerName());
            merchantHome.onChatResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (customerHome != null) {
            customerHome.onChatPause();
        } else
            merchantHome.onChatPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressDialog != null)
            progressDialog.dismiss();
    }
}
