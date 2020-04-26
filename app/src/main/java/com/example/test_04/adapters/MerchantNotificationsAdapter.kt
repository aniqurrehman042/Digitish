package com.example.test_04.adapters

import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.test_04.R
import com.example.test_04.comparators.DateComparator
import com.example.test_04.db_callbacks.GetMerchantCallback
import com.example.test_04.db_callbacks.IGetProductReview
import com.example.test_04.models.*
import com.example.test_04.ui.CustomerHome
import com.example.test_04.ui.MerchantHome
import com.example.test_04.utils.DBUtils
import com.example.test_04.utils.DateUtils
import com.example.test_04.utils.ReviewUtils
import com.example.test_04.utils.SwitchUtils
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.makeramen.roundedimageview.RoundedImageView
import java.util.*
import kotlin.collections.ArrayList


class MerchantNotificationsAdapter(var customerNotificationsHolder: ArrayList<CustomerNotification>, var customerNotificationsData: ArrayList<DateComparator>, var merchantHome: MerchantHome?, var customerHome: CustomerHome?) : RecyclerView.Adapter<MerchantNotificationsAdapter.ViewHolder>() {

    private var lastDate = Calendar.getInstance().time
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var done: Int = 0
    private var toBeDone: Int = 1
    private var productReviewChats: ArrayList<ProductReviewChat> = ArrayList()
    private lateinit var progressDialog: ProgressDialog
    private var customer: Customer? = null
    private var merchant: Merchant? = null
    private var context: Context = if (customerHome != null) customerHome!! else merchantHome!!

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var ivNotificationImg: ImageView = itemView.findViewById(R.id.iv_notification_img)
        var tvNotificationTitle: TextView = itemView.findViewById(R.id.tv_notification_title)
        var tvNotificationDisc: TextView = itemView.findViewById(R.id.tv_notification_disc)
        var llMain: LinearLayout = itemView.findViewById(R.id.ll_main)
        var tvDateHeading: TextView = itemView.findViewById(R.id.tv_date_heading)
        var tvUnread: TextView = itemView.findViewById(R.id.tv_unread)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_notification_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return customerNotificationsHolder.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val customerNotificationHolder = customerNotificationsHolder[position]
        val customerNotificationData = customerNotificationsData[position]

        val tvDateHeading: TextView = holder.tvDateHeading
        val tvNotificationTitle: TextView = holder.tvNotificationTitle
        val tvNotificationDesc: TextView = holder.tvNotificationDisc
        val ivNotificationImg: ImageView = holder.ivNotificationImg
        val tvUnread: TextView = holder.tvUnread

        tvUnread.visibility = View.GONE

        val currentDate = Calendar.getInstance().time
        val msgDate = DateUtils.stringToDateWithTime(customerNotificationsHolder[position].date)

        if (DateUtils.isSameDay(currentDate, msgDate) && position < 1) {
            tvDateHeading.visibility = View.VISIBLE
            tvDateHeading.text = "Today"
        } else if (DateUtils.isSameDay(msgDate, lastDate)) {
            tvDateHeading.visibility = View.GONE
        } else {
            lastDate = msgDate
            tvDateHeading.visibility = View.VISIBLE
            tvDateHeading.text = DateUtils.dateToString(msgDate)
        }

        when (customerNotificationHolder.type) {
            "Product Review" -> {

                var productReview: ProductReview = customerNotificationData as ProductReview

                ivNotificationImg.setImageResource(R.drawable.ic_product_review)

                holder.llMain.setOnClickListener {
                    getProductReviewChat(productReview)
                }
            }
            "Merchant Review" -> {
                var merchantReview: MerchantReview = customerNotificationData as MerchantReview

                ivNotificationImg.setImageResource(R.drawable.ic_merchant_rating)

                holder.llMain.setOnClickListener {
                    val dialog = Dialog(context)
                    dialog.setContentView(R.layout.layout_rate_merchant)
                    dialog.show()
                    var reviewUtils = ReviewUtils(dialog, false)
                    reviewUtils.responsiveRating = merchantReview.responsiveness
                    reviewUtils.saleServiceRating = merchantReview.afterSaleService
                    reviewUtils.agentSupportRating = merchantReview.salesAgentSupport
                    reviewUtils.productInfoRating = merchantReview.productInfo
                    reviewUtils.valueRating = merchantReview.valueForPrice

                    DBUtils.getMerchant(merchantReview.merchantName, object : GetMerchantCallback {
                        override fun onCallback(merchant: Merchant?, successful: Boolean) {
                            if (successful) {
                                reviewUtils.fillStars(merchant!!.rating.toInt())
                            } else {
                                Toast.makeText(merchantHome, "Couldn't load merchant rating", Toast.LENGTH_SHORT).show()
                            }
                        }
                    })

                    val params = WindowManager.LayoutParams()
                    params.width = WindowManager.LayoutParams.MATCH_PARENT
                    params.height = WindowManager.LayoutParams.WRAP_CONTENT
                    dialog.window!!.attributes = params
                    dialog.findViewById<View>(R.id.tv_submit).setOnClickListener { dialog.dismiss() }
                    dialog.findViewById<RoundedImageView>(R.id.riv_merchant_logo).setImageResource(SwitchUtils.getMerchantImgId(merchantReview.merchantName))
                    dialog.findViewById<TextView>(R.id.tv_submit).text = "OK"
                    dialog.findViewById<TextView>(R.id.tv_merchant_name).text = merchantReview.merchantName
                    dialog.findViewById<View>(R.id.tv_cancel).visibility = View.GONE
                }
            }
            "Product Review Chat" -> {
                var productReviewChat: ProductReviewChat = customerNotificationData as ProductReviewChat
                var productReview = ProductReview(productReviewChat.customerEmail, productReviewChat.customerName, productReviewChat.merchantName, productReviewChat.productCode, "", "", 0, "", "", productReviewChat.qrId, false, true, productReviewChat.date)


                ivNotificationImg.setImageResource(R.drawable.ic_product_review)


                holder.llMain.setOnClickListener {
                    getProductReviewChat(productReview)
                }
            }

            "Offer" -> {

                val offer: Offer = customerNotificationData as Offer

                ivNotificationImg.setImageResource(R.drawable.ic_offer)

                holder.llMain.setOnClickListener {
                    val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(context)
                    alertDialogBuilder
                            .setTitle(offer.offerTitle)
                            .setMessage(offer.offerDesc)
                            .setPositiveButton("OK") { _, _ ->

                            }
                            .show()
                }
            }
        }

        tvNotificationTitle.text = customerNotificationHolder.title
        tvNotificationDesc.text = customerNotificationHolder.message
    }

    private fun getProductReviewChat(productReview: ProductReview) {

        done = 0
        toBeDone = 1
        var productReviewToPass: ProductReview = productReview

        showProgressDialog("Loading Review")

        if (productReview.productName.isEmpty()) {
            toBeDone = 2

            DBUtils.getProductReview(productReview.qrId, productReview.productCode, object : IGetProductReview {
                override fun onCallback(successful: Boolean, productReview: ProductReview) {
                    if (successful) {
                        productReviewToPass = productReview
                        checkAndStartFragment(productReviewToPass)
                    } else {
                        Toast.makeText(context, "Couldn't load review", Toast.LENGTH_SHORT).show()
                        progressDialog.dismiss()
                    }
                }
            })

        }

        db.collection("ProductReviewChat")
                .whereEqualTo("Merchant Name", productReview.merchantName)
                .whereEqualTo("Customer Email", productReview.customerEmail)
                .whereEqualTo("QR Id", productReview.qrId)
                .whereEqualTo("Product Code", productReview.productCode)
                .orderBy("Date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!.documents) {
                            val customerName = document["Customer Name"].toString()
                            val productCode = document["Product Code"].toString()
                            val qrId = document["QR Id"].toString()
                            val image = document["Image"].toString()
                            val message = document["Message"].toString()
                            val sender = document["Sender"].toString()
                            val customerProfilePic = document["Customer Profile Picture"].toString()
                            val timestamp = document["Date"] as Timestamp?
                            val dateObj = timestamp!!.toDate()
                            val date = DateUtils.dateToStringWithTime(dateObj)
                            val productReviewChat = ProductReviewChat(productReview.customerEmail, customerName, productReview.merchantName, productCode, qrId, image, message, sender, date, customerProfilePic)
                            productReviewChats.add(productReviewChat)
                        }
                        checkAndStartFragment(productReviewToPass)
                    } else {
                        Toast.makeText(merchantHome, "Couldn't load chat", Toast.LENGTH_SHORT).show()
                        progressDialog.dismiss()
                    }

                }

        if (customerHome != null) {

            DBUtils.getMerchant(productReview.merchantName, object : GetMerchantCallback {
                override fun onCallback(merchant: Merchant?, successful: Boolean) {
                    if (successful) {
                        this@MerchantNotificationsAdapter.merchant = merchant
                        checkAndStartFragment(productReviewToPass)
                    } else {
                        Toast.makeText(merchantHome, "Couldn't load chat", Toast.LENGTH_SHORT).show()
                        progressDialog.dismiss()
                    }
                }
            })

        } else {
            db.collection("Customers")
                    .whereEqualTo("Email", productReview.customerEmail)
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val documentSnapshot = task.result!!.documents[0]
                            val customerName = documentSnapshot["Customer Name"].toString()
                            val phone = documentSnapshot["Phone"].toString()
                            val points = documentSnapshot.getLong("Points").toString()
                            val profilePicture = documentSnapshot["Profile Picture"].toString()
                            customer = Customer(productReview.customerEmail, customerName, phone, points, profilePicture)
                            checkAndStartFragment(productReviewToPass)
                        } else {
                            Toast.makeText(context, "Couldn't load chat", Toast.LENGTH_SHORT).show()
                            progressDialog.dismiss()
                        }

                    }
        }
    }

    private fun checkAndStartFragment(productReview: ProductReview) {
        done++
        if (done > toBeDone) {
            progressDialog.dismiss()
            if (customerHome != null){
                customerHome!!.startProductReviewsThenProductReviewChat(productReviewChats, merchant, productReview, false)
            } else {
                merchantHome!!.startProductReviewChatFragment(productReviewChats, productReview, customer)
            }
        }
    }

    private fun showProgressDialog(title: String) {
        progressDialog = ProgressDialog(context)
        progressDialog.setTitle(title)
        progressDialog.setCancelable(false)
        progressDialog.show()
    }
}