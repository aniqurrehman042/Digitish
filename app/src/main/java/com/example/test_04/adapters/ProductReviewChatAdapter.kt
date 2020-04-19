package com.example.test_04.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test_04.R
import com.example.test_04.async.DownloadImageTask
import com.example.test_04.models.ProductReviewChat
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.makeramen.roundedimageview.RoundedImageView

class ProductReviewChatAdapter(private var productReviewChats: ArrayList<ProductReviewChat>, private var profileBmp: Bitmap?, private var isMerchantChat: Boolean) : RecyclerView.Adapter<ProductReviewChatAdapter.ViewHolder>() {

    init {
        if (isMerchantChat && !productReviewChats.isEmpty())
            DownloadImageTask(profileBmp, null).execute(productReviewChats.get(0).customerProfilePic)
    }

    private val storage = FirebaseStorage.getInstance()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvDate: TextView = itemView.findViewById(R.id.tv_date)
        var tvMyMsg: TextView = itemView.findViewById(R.id.tv_my_msg)
        var tvOtherMsg: TextView = itemView.findViewById(R.id.tv_other_msg)
        var tvMyName: TextView = itemView.findViewById(R.id.tv_my_name)
        var tvOtherName: TextView = itemView.findViewById(R.id.tv_other_name)
        var rivMyImg: RoundedImageView = itemView.findViewById(R.id.riv_my_img)
        var rivOtherImg: RoundedImageView = itemView.findViewById(R.id.riv_other_img)
        var ivMyImg: ImageView = itemView.findViewById(R.id.iv_my_img)
        var ivOtherImg: ImageView = itemView.findViewById(R.id.iv_other_img)
        var tvNotSent: TextView = itemView.findViewById(R.id.tv_not_sent)
        var llChat: LinearLayout = itemView.findViewById(R.id.ll_chat)
        var llMyMsg: LinearLayout = itemView.findViewById(R.id.ll_my_msg)
        var llOtherMsg: LinearLayout = itemView.findViewById(R.id.ll_other_msg)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_product_review_chat_item, parent, false)
        return ViewHolder(view);
    }

    override fun getItemCount(): Int {
        return productReviewChats.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tvMyMsg = holder.tvMyMsg
        val tvOtherMsg = holder.tvOtherMsg
        val tvMyName = holder.tvMyName
        val tvOtherName = holder.tvOtherName
        val llMyMsg = holder.llMyMsg
        val llOtherMsg = holder.llOtherMsg
        val tvDate = holder.tvDate
        val ivMyImg = holder.ivMyImg
        val ivOtherImg = holder.ivOtherImg
        val rivMyImg = holder.rivMyImg
        val rivOtherImg = holder.rivOtherImg
        val llChat = holder.llChat
        val tvNotSent = holder.tvNotSent

        val productReviewChat: ProductReviewChat = productReviewChats[position]

        val message = productReviewChat.message
        val date = productReviewChat.date
        val image = productReviewChat.image
        val sender = productReviewChat.sender
        val bitmap = productReviewChat.bitmap
        val name = if (productReviewChat.sender.equals("Customer")) productReviewChat.customerName else productReviewChat.merchantName

        if (productReviewChats.get(position).sent)
            tvNotSent.visibility = View.GONE
        else
            tvNotSent.visibility = View.VISIBLE

        tvDate.text = date

        if (isMerchantChat) {

            if (sender == "Merchant") {
                rivMyImg.setImageResource(getMerchantImgId(productReviewChat.merchantName))
                rivOtherImg.visibility = View.GONE
                llOtherMsg.visibility = View.GONE
                ivOtherImg.visibility = View.GONE
                ivMyImg.visibility = View.GONE

                if (image == "") {
                    tvMyMsg.visibility = View.VISIBLE
                    tvMyMsg.text = message
                    tvMyName.visibility = View.VISIBLE
                    tvMyName.text = name
                    llMyMsg.visibility = View.VISIBLE
                } else {
                    llChat.visibility = View.GONE
                    ivMyImg.visibility = View.VISIBLE
                    if (bitmap == null) setImage(image, ivMyImg, tvNotSent, productReviewChat) else ivMyImg.setImageBitmap(bitmap)
                }
            } else {
                if (profileBmp != null)
                    rivOtherImg.setImageBitmap(profileBmp)
                else
                    DownloadImageTask(profileBmp, rivOtherImg).execute(productReviewChat.customerProfilePic)
                rivMyImg.visibility = View.GONE
                llMyMsg.visibility = View.GONE
                ivOtherImg.visibility = View.GONE
                ivMyImg.visibility = View.GONE

                if (image == "") {
                    tvOtherMsg.visibility = View.VISIBLE
                    tvOtherMsg.text = message
                    tvOtherName.visibility = View.VISIBLE
                    tvOtherName.text = name
                    llOtherMsg.visibility = View.VISIBLE
                } else {
                    llChat.visibility = View.GONE
                    ivOtherImg.visibility = View.VISIBLE
                    if (bitmap == null) setImage(image, ivOtherImg, tvNotSent, productReviewChat) else ivOtherImg.setImageBitmap(bitmap)
                }
            }
        } else {
            if (sender == "Customer") {
                if (profileBmp != null)
                    rivMyImg.setImageBitmap(profileBmp)
                else
                    DownloadImageTask(profileBmp, rivMyImg).execute(productReviewChat.customerProfilePic)
                rivOtherImg.visibility = View.GONE
                llOtherMsg.visibility = View.GONE
                ivOtherImg.visibility = View.GONE
                ivMyImg.visibility = View.GONE

                if (image == "") {
                    tvMyMsg.visibility = View.VISIBLE
                    tvMyMsg.text = message
                    tvMyName.visibility = View.VISIBLE
                    tvMyName.text = name
                    llMyMsg.visibility = View.VISIBLE
                } else {
                    llChat.visibility = View.GONE
                    ivMyImg.visibility = View.VISIBLE
                    if (bitmap == null) setImage(image, ivMyImg, tvNotSent, productReviewChat) else ivMyImg.setImageBitmap(bitmap)
                }
            } else {
                rivOtherImg.setImageResource(getMerchantImgId(productReviewChat.merchantName))
                rivMyImg.visibility = View.GONE
                llMyMsg.visibility = View.GONE
                ivOtherImg.visibility = View.GONE
                ivMyImg.visibility = View.GONE

                if (image == "") {
                    tvOtherMsg.visibility = View.VISIBLE
                    tvOtherMsg.text = message
                    tvOtherName.visibility = View.VISIBLE
                    tvOtherName.text = name
                    llOtherMsg.visibility = View.VISIBLE
                } else {
                    llChat.visibility = View.GONE
                    ivOtherImg.visibility = View.VISIBLE
                    if (bitmap == null) setImage(image, ivOtherImg, tvNotSent, productReviewChat) else ivOtherImg.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun getMerchantImgId(merchantName: String): Int {
        when(merchantName) {
            "Abans" -> return R.drawable.abans_logo_round
            "Softlogic Holdings PLC" -> return R.drawable.softlogic_logo_round
            "Singer" -> return R.drawable.singer_logo_round
        }
        return R.drawable.abans_logo_round
    }

    private fun setImage(image: String, imageView: ImageView, notSent: TextView, productReviewChat: ProductReviewChat) {
        val ONE_MEGABYTE = 1024 * 1024.toLong()
        val storageRef: StorageReference = storage.reference
        val imageRef = storageRef.child("chatimages/$image")
        imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener { bytes -> // Data for "images/island.jpg" is returns, use this as needed
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            imageView.setImageBitmap(bitmap)
            productReviewChat.bitmap = bitmap
        }.addOnFailureListener {
            notSent.visibility = View.VISIBLE
            notSent.text = "Couldn't load image"
        }
    }

}