package com.example.test_04.adapters

import android.app.Dialog
import android.content.Context
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test_04.R
import com.example.test_04.models.CustomerNotification
import com.example.test_04.models.MerchantReview
import com.example.test_04.models.ProductReview
import com.example.test_04.ui.MerchantHome
import com.example.test_04.utils.ReviewUtils
import java.util.*


class MerchantNotificationsAdapter(var customerNotifications: ArrayList<CustomerNotification>, var notifications: ArrayList<ArrayList<ProductReview>>, var merchantReviews: ArrayList<MerchantReview>, var merchantHome: MerchantHome) : RecyclerView.Adapter<MerchantNotificationsAdapter.ViewHolder>() {

    lateinit var context: Context
    private var lastDate = Calendar.getInstance().time

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var ivNotificationImg: ImageView = itemView.findViewById(R.id.iv_notification_img)
        var tvNotificationTitle: TextView = itemView.findViewById(R.id.tv_notification_title)
        var tvNotificationDisc: TextView = itemView.findViewById(R.id.tv_notification_disc)
        var llMain: LinearLayout = itemView.findViewById(R.id.ll_main)
        var tvDateHeading: TextView = itemView.findViewById(R.id.tv_date_heading)
        var tvUnread: TextView = itemView.findViewById(R.id.tv_unread)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_notification_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return customerNotifications.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val customerNotification = customerNotifications[position]

        val tvDateHeading: TextView = holder.tvDateHeading
        val tvNotificationTitle: TextView = holder.tvNotificationTitle
        val tvNotificationDesc: TextView = holder.tvNotificationDisc
        val ivNotificationImg: ImageView = holder.ivNotificationImg
        val tvUnread: TextView = holder.tvUnread

        tvUnread.visibility = View.GONE

//        val currentDate = Calendar.getInstance().time
//        val msgDate = DateUtils.stringToDateWithTime(customerNotifications[position].date)
//
//        if (DateUtils.isSameDay(currentDate, msgDate)) {
//            tvDateHeading.visibility = View.VISIBLE
//            tvDateHeading.text = "Today"
//        } else if (DateUtils.isSameDay(msgDate, lastDate)) {
//            tvDateHeading.visibility = View.GONE
//        } else {
//            lastDate = msgDate
//            tvDateHeading.visibility = View.VISIBLE
//            tvDateHeading.text = DateUtils.dateToString(msgDate)
//        }

        if (position > 0)
            tvDateHeading.visibility = View.GONE

        if (position < notifications.size) {

            var productReviews = notifications[position]

            holder.llMain.setOnClickListener {
                merchantHome.startMerchantReviewsFragment(productReviews)
            }

        } else {

            var merchantReview: MerchantReview = merchantReviews[position - notifications.size]

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

                val params = WindowManager.LayoutParams()
                params.width = WindowManager.LayoutParams.MATCH_PARENT
                params.height = WindowManager.LayoutParams.WRAP_CONTENT
                dialog.window!!.attributes = params
                dialog.findViewById<View>(R.id.tv_submit).setOnClickListener { dialog.dismiss() }
                dialog.findViewById<TextView>(R.id.tv_submit).text = "OK"
                dialog.findViewById<View>(R.id.tv_cancel).visibility = View.GONE

            }
        }

        if (customerNotification.type == "Merchant Review")
            ivNotificationImg.setImageResource(R.drawable.ic_message)
        else if (customerNotification.type == "Product Review")
            ivNotificationImg.setImageResource(R.drawable.ic_stars_back_white)

        tvNotificationTitle.text = customerNotification.title
        tvNotificationDesc.text = customerNotification.message
//        tvUnread.height = tvUnread.width

    }

    fun convertDpToPixel(dp: Float, context: Context): Float {
        return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun convertPixelsToDp(px: Float, context: Context): Float {
        return px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }
}