package com.example.test_04.ui.fragments

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.Toast.LENGTH_SHORT
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test_04.R
import com.example.test_04.adapters.ProductReviewChatAdapter
import com.example.test_04.adapters.SendImagesAdapter
import com.example.test_04.models.*
import com.example.test_04.ui.CustomerHome
import com.example.test_04.ui.MerchantHome
import com.example.test_04.utils.FCMUtils.Companion.sendMessage
import com.example.test_04.utils.ReviewUtils
import com.example.test_04.utils.SwitchUtils
import com.fasterxml.uuid.Generators
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.vansuita.pickimage.bundle.PickSetup
import com.vansuita.pickimage.dialog.PickImageDialog
import kotlinx.android.synthetic.main.fragment_product_review_chat.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * A simple [Fragment] subclass.
 */
class ProductReviewChatFragment : Fragment() {

    private lateinit var rvChat: RecyclerView
    private lateinit var rvSendImages: RecyclerView
    private lateinit var ivProductImg: ImageView
    private lateinit var tvProductCode: TextView
    private lateinit var tvReviewTitle: TextView
    private lateinit var tvReviewDesc: TextView
    private lateinit var tvMarkCompleted: TextView
    private lateinit var ivAddPic: ImageView
    private lateinit var etMessage: EditText
    private lateinit var llSend: LinearLayout
    private lateinit var tvMarkedCompleted: TextView
    private lateinit var llChat: LinearLayout
    private lateinit var vChatLine: View
    private lateinit var tvProductName: TextView
    private lateinit var tvProductCodeSearch: TextView
    private lateinit var llSearchReviewHeader: LinearLayout
    private lateinit var llReviewHeader: LinearLayout

    private lateinit var reviewUtils: ReviewUtils
    private lateinit var chatLayoutManager: LinearLayoutManager
    private lateinit var imagesAdapter: SendImagesAdapter
    private lateinit var productReviewChatAdapter: ProductReviewChatAdapter
    private var progressDialog: ProgressDialog? = null
    private var productCategory: String? = null
    private var productReview: ProductReview? = null
    private var customerHome: CustomerHome? = null
    private var merchantHome: MerchantHome? = null
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var productReviewChats: ArrayList<ProductReviewChat> = ArrayList()
    private var bitmaps = ArrayList<Bitmap>()
    private var merchant: Merchant? = null
    private var storage = FirebaseStorage.getInstance()
    private var fromSearch: Boolean? = false
    private var customer: Customer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var view: View = inflater.inflate(R.layout.fragment_product_review_chat, container, false)

        if (activity!!.javaClass.simpleName == "CustomerHome")
            customerHome = activity as CustomerHome?
        else
            merchantHome = activity as MerchantHome?

        findView(view)

        init(view)

        return view
    }

    private fun init(view: View) {
        getChats()
        initHeader()
        setUpRatingSystem(view)
        setUpRecyclers()
        setListeners()
        setViewValues()
    }

    private fun initHeader() {

    }

    private fun setUpRatingSystem(view: View) {
        reviewUtils = if (fromSearch!!)
            ReviewUtils(view, false, true)
        else
            ReviewUtils(view, false, false)
    }

    private fun setViewValues() {
        if (fromSearch!!) {
            tvProductCodeSearch.text = productReview!!.productCode
            tvProductName.text = productReview!!.productName
            showSearchHeader()
        } else {
            tvProductCode.text = productReview!!.productCode
        }
        tvReviewTitle.text = productReview!!.reviewTitle
        tvReviewDesc.text = productReview!!.reviewDescription
        reviewUtils.fillStars(productReview!!.productRating)
        ivProductImg.setImageResource(SwitchUtils.getProductImgId(productCategory))
    }

    private fun showSearchHeader() {
        llSearchReviewHeader.visibility = View.VISIBLE
        llReviewHeader.visibility = View.GONE
    }

    private fun setUpRecyclers() {
        imagesAdapter = SendImagesAdapter(bitmaps, rvSendImages)
        val lm = LinearLayoutManager(context)
        lm.orientation = LinearLayoutManager.HORIZONTAL
        rvSendImages.layoutManager = lm
        rvSendImages.adapter = imagesAdapter

        productReviewChatAdapter = if (customerHome == null)
            ProductReviewChatAdapter(productReviewChats, null, true)
        else
            ProductReviewChatAdapter(productReviewChats, customerHome!!.profileBmp, false)
        chatLayoutManager = LinearLayoutManager(context)
        chatLayoutManager.reverseLayout = true
        chatLayoutManager.stackFromEnd = true
        rvChat.layoutManager = chatLayoutManager
        rvChat.adapter = productReviewChatAdapter
        chatLayoutManager.scrollToPosition(0)
    }

    private fun getChats() {
        val productReviewChats: ArrayList<ProductReviewChat>? = arguments!!.getSerializable("Chats") as ArrayList<ProductReviewChat>?
        if (productReviewChats != null)
            this.productReviewChats = productReviewChats
        val merchant: Merchant? = arguments!!.getSerializable("Merchant") as Merchant?
        if (merchant != null)
            this.merchant = merchant
        val productReview: ProductReview? = arguments!!.getSerializable("Product Review") as ProductReview?
        if (productReview != null)
            this.productReview = productReview
        val productCategory: String? = arguments!!.getString("Product Category") as String?
        if (productReview != null)
            this.productCategory = productCategory
        val fromSearch: Boolean? = arguments!!.getBoolean("From Search") as Boolean?
        if (fromSearch != null)
            this.fromSearch = fromSearch
        val customer: Customer? = arguments!!.getSerializable("Customer") as Customer?
        if (customer != null)
            this.customer = customer
    }

    private fun findView(view: View) {
        rvChat = view.findViewById(R.id.rv_chat)
        rvSendImages = view.findViewById(R.id.rv_send_images)
        ivProductImg = view.findViewById(R.id.iv_product_img)
        tvProductCode = view.findViewById(R.id.tv_product_code)
        tvReviewTitle = view.findViewById(R.id.tv_review_title)
        tvReviewDesc = view.findViewById(R.id.tv_review_desc)
        tvMarkCompleted = view.findViewById(R.id.tv_mark_completed)
        ivAddPic = view.findViewById(R.id.iv_add_pic)
        etMessage = view.findViewById(R.id.et_message)
        llSend = view.findViewById(R.id.ll_send)
        vChatLine = view.findViewById(R.id.v_chat_line)
        llChat = view.findViewById(R.id.ll_chat)
        tvMarkedCompleted = view.findViewById(R.id.tv_marked_completed)
        tvProductName = view.findViewById(R.id.tv_product_name)
        tvProductCodeSearch = view.findViewById(R.id.tv_product_code_search)
        llSearchReviewHeader = view.findViewById(R.id.ll_search_review_header)
        llReviewHeader = view.findViewById(R.id.ll_review_header)
    }

    private fun setListeners() {
        llSend.setOnClickListener(View.OnClickListener {
            sendImages()
            val message = etMessage.text.toString()
            if (message == "") {
                return@OnClickListener
            }
            val chat = getChatObject(message, "")
            productReviewChats.add(0, chat)
            productReviewChatAdapter.notifyItemInserted(0)
            productReviewChatAdapter.notifyItemRangeChanged(productReviewChats.indexOf(chat), productReviewChats.size)
            chatLayoutManager.scrollToPosition(0)
            uploadChat(chat)
            etMessage.setText("")
        })

        ivAddPic.setOnClickListener {
            PickImageDialog.build(PickSetup())
                    .setOnPickResult { r ->
                        rvSendImages.visibility = View.VISIBLE
                        bitmaps.add(r.bitmap)
                        imagesAdapter.notifyItemInserted(bitmaps.size - 1)
                    }
                    .setOnPickCancel {
                        //TODO: do what you have to if user clicked cancel
                    }.show(activity!!.supportFragmentManager)
        }

        tvMarkCompleted.setOnClickListener {
            val dataMap: MutableMap<String, Any> = java.util.HashMap()
            dataMap["Completed"] = true

            showProgressDialog("Updating Review")

            db.collection("Product Reviews")
                    .document(productReview!!.id)
                    .update(dataMap)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(customerHome, "Update Successful", LENGTH_SHORT)
                            hideChatAndMarkComplete()
                        } else {
                            Toast.makeText(customerHome, "Update Failed", LENGTH_SHORT)
                        }
                        progressDialog!!.dismiss()
                    }
        }
    }

    private fun hideChatAndMarkComplete() {
        if (customerHome != null)
            customerHome!!.showBottomBar()
        else
            merchantHome!!.showBottomBar()
        markCompleted()
    }

    private fun markCompleted() {
        llChat.visibility = View.GONE
        vChatLine.visibility = View.GONE
        tvMarkCompleted.visibility = View.GONE
        tvMarkedCompleted.visibility = View.VISIBLE
    }

    private fun showProgressDialog(title: String) {
        progressDialog = ProgressDialog(customerHome)
        progressDialog!!.setTitle(title)
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()
    }

    private fun getChatObject(message: String, image: String): ProductReviewChat {
        val dateObj = Calendar.getInstance().time
        val sdf = SimpleDateFormat("dd-MM-yyyy - HH:mm")
        val date = sdf.format(dateObj)
        var sender: String? = null
        sender = if (customerHome != null) "Customer" else "Merchant"
        val customerEmail: String
        val customerName: String
        val merchantName: String
        val customerProfilePic: String
        val productCode: String = productReview!!.productCode
        val qrId: String = productReview!!.qrId
        if (productReviewChats.isNotEmpty()) {
            customerEmail = productReviewChats[0].customerEmail
            customerName = productReviewChats[0].customerName
            merchantName = productReviewChats[0].merchantName
            if (customer == null)
                customerProfilePic = productReviewChats[0].customerProfilePic
            else
                customerProfilePic = customer!!.profilePic
        } else if (customerHome != null) {
            customerEmail = CurrentCustomer.email
            customerName = CurrentCustomer.name
            merchantName = merchant!!.name
            customerProfilePic = CurrentCustomer.profilePicture
        } else {
            customerEmail = productReview!!.customerEmail
            customerName = productReview!!.customerName
            merchantName = CurrentMerchant.name
            customerProfilePic = customer!!.profilePic
        }
        return ProductReviewChat(customerEmail, customerName, merchantName, productCode, qrId, image, message, sender, date, customerProfilePic)
    }

    private fun sendImages() {
        if (bitmaps.isEmpty()) return
        rvSendImages.visibility = View.GONE
        for (bitmap in bitmaps) {
            val uuid = Generators.timeBasedGenerator().generate()
            val storageRef = storage.reference
            val imageRef = storageRef.child("chatimages/$uuid")
            val productReviewChat = getChatObject("", uuid.toString())
            productReviewChat.bitmap = bitmap
            productReviewChats.add(0, productReviewChat)
            val chatIndex: Int = productReviewChats.indexOf(productReviewChat)
            productReviewChatAdapter.notifyItemInserted(chatIndex)
            productReviewChatAdapter.notifyItemRangeChanged(chatIndex, productReviewChats.size)
            chatLayoutManager.scrollToPosition(0)
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            val uploadTask = imageRef.putBytes(data)
            uploadTask.addOnFailureListener { // Handle unsuccessful uploads
                productReviewChat.sent = false
                val newChatIndex: Int = productReviewChats.indexOf(productReviewChat)
                productReviewChatAdapter.notifyItemChanged(newChatIndex)
                productReviewChatAdapter.notifyItemRangeChanged(newChatIndex, productReviewChats.size)
            }.addOnSuccessListener { uploadChat(productReviewChat) }
        }
        bitmaps.clear()
        imagesAdapter.notifyDataSetChanged()
    }

    private fun uploadChat(productReviewChat: ProductReviewChat) {
        val dateObj = Calendar.getInstance().time
        val dateTimestamp = Timestamp(dateObj)
        val data: MutableMap<String, Any> = HashMap()
        data["Customer Email"] = productReviewChat.customerEmail
        data["Customer Name"] = productReviewChat.customerName
        data["Customer Profile Picture"] = productReviewChat.customerProfilePic
        data["Merchant Name"] = productReviewChat.merchantName
        data["Date"] = dateTimestamp
        data["Message"] = productReviewChat.message
        data["Sender"] = productReviewChat.sender
        data["Image"] = productReviewChat.image
        data["QR Id"] = productReview!!.qrId
        data["Product Code"] = productReview!!.productCode
        data["Product Name"] = productReview!!.productName
        db.collection("ProductReviewChat")
                .add(data)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        var context: Context? = if (customerHome != null) customerHome!! else merchantHome
                        var message = "${productReview!!.customerName} has commented on your product's review"
                        if (customerHome == null) {
                            message = "${productReview!!.merchantName} has replied to your review"
                        }
                        var receiverMerchant: Boolean = true
                        var senderNameOrEmail = productReview!!.customerEmail
                        var receiverNameOrEmail = productReview!!.merchantName
                        if (customer != null) {
                            receiverMerchant = false
                            senderNameOrEmail = productReview!!.merchantName
                            receiverNameOrEmail = productReview!!.customerEmail
                        }
                        sendMessage(context!!, true, receiverMerchant, "Review", message, receiverNameOrEmail, senderNameOrEmail)
                    } else {
                        productReviewChat.sent = false
                        productReviewChatAdapter.notifyItemChanged(productReviewChats.indexOf(productReviewChat))
                    }
                }
    }

    override fun onResume() {
        super.onResume()
        if (customerHome != null) {

            customerHome!!.hideSearchIcons()
            customerHome!!.onChatResume(merchant)
            if (!fromSearch!!)
                customerHome!!.hideBottomBar()
            if (fromSearch!!)
                customerHome!!.setPageTitle(productReview!!.customerName)
            else
                customerHome!!.setPageTitle(productReview!!.productName)
        } else {
            merchantHome!!.onChatResume()
            merchantHome!!.hideSearchIcons()
            merchantHome!!.setPageTitle(productReview!!.customerName)
        }

        if (productReview!!.isCompleted) {
            hideChatAndMarkComplete()
        }

        if (fromSearch!! && customerHome != null) {
            customerHome!!.setShowMerchantMenuListener();
            vChatLine.visibility = View.GONE
            llChat.visibility = View.GONE
            tv_mark_completed.visibility = View.GONE
        }

        if (customer != null)
            tvMarkCompleted.visibility = View.GONE

    }

    override fun onPause() {
        super.onPause()
        if (customerHome != null) {
            customerHome!!.onChatPause()
            customerHome!!.setLogoutMenuListener();
        } else
            merchantHome!!.onChatPause()
    }

}
