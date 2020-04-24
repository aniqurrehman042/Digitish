package com.example.test_04.ui.fragments

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test_04.R
import com.example.test_04.adapters.FilterAdapter
import com.example.test_04.adapters.MerchantNotificationsAdapter
import com.example.test_04.adapters.SearchReviewsAdapter
import com.example.test_04.comparators.DateComparator
import com.example.test_04.db_callbacks.IGetMerchantReviews
import com.example.test_04.models.*
import com.example.test_04.ui.MerchantHome
import com.example.test_04.utils.DBUtils
import com.example.test_04.utils.DateUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 */
class MerchantReviewsFragment : Fragment() {

    private lateinit var rvReviews: RecyclerView
    private lateinit var rvFilters: RecyclerView

    private var productReviews = ArrayList<ProductReview>()
    private var merchantReviews = ArrayList<MerchantReview>()
    private var merchantReviewsHolder = ArrayList<CustomerNotification>()
    private var filters = ArrayList<Filter>()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var progressDialog: ProgressDialog
    private var searchReviewsAdapter: SearchReviewsAdapter? = null
    private var filterAdapter: FilterAdapter? = null
    private var merchantNotificationsAdapter: MerchantNotificationsAdapter? = null
    private lateinit var selectLayoutManager: LinearLayoutManager
    private var reviewsLayoutManager: LinearLayoutManager? = null
    private var reviewsLoaded = 0
    private var reviewsLoadingCompleted = true
    private var merchantHome: MerchantHome? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_merchant_reviews, container, false)
        merchantHome = activity as MerchantHome

        findViews(view)
        init()

        return view
    }

    private fun findViews(view: View?) {
        rvReviews = view!!.findViewById(R.id.rv_reviews)
        rvFilters = view.findViewById(R.id.rv_select)
    }

    private fun init() {
        if (arguments != null) {
            firstRunCheck()
        }
        arguments = null
        if (filters.isEmpty())
            setUpFilters()
        setUpFilterRecycler()
        if (merchantReviews.isEmpty())
            getMerchantReviews()
        else if (productReviews.isNotEmpty() && filterAdapter!!.selectedFilter != 0) {
            setUpReviewsRecycler()
        } else {
            setMerchantReviewsRecycler()
        }
    }

    private fun firstRunCheck() {
        val firstRun = arguments!!.getBoolean("First Run", false)
        if (firstRun) {
            resetRecyclers()
        }
    }

    private fun resetRecyclers() {
        filters = ArrayList()
        productReviews = ArrayList()
        filterAdapter = null
        searchReviewsAdapter = null
    }

    fun onReviewsClickAgain() {
        resetRecyclers()
        init()
    }

    private fun setUpReviewsRecycler() {
        if (searchReviewsAdapter == null)
            searchReviewsAdapter = SearchReviewsAdapter(productReviews, null, merchantHome)
        else
            searchReviewsAdapter!!.notifyDataSetChanged()
        reviewsLayoutManager = LinearLayoutManager(merchantHome)
        rvReviews.layoutManager = reviewsLayoutManager
        rvReviews.adapter = searchReviewsAdapter
    }

    private fun showProgressDialog(title: String) {
        progressDialog = ProgressDialog(merchantHome)
        progressDialog.setTitle(title)
        progressDialog.setCancelable(false)
        progressDialog.show()
    }

    private fun setUpFilters() {
        val filterNames = arrayOf("Merchant rating", "Reviews", "5 star reviews", "4 star reviews", "3 star reviews", "2 star reviews", "1 star reviews")
        filters = ArrayList()
        for (filterName in filterNames) {
            filters.add(Filter(filterName, false))
        }
        filters[0].selected = true
    }

    private fun setUpFilterRecycler() {
        if (filterAdapter == null)
            filterAdapter = FilterAdapter(filters, null, this)
        selectLayoutManager = LinearLayoutManager(context)
        selectLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        rvFilters.layoutManager = selectLayoutManager
        rvFilters.adapter = filterAdapter
    }

    fun getMerchantReviews() {

        showProgressDialog("Loading reviews")

        merchantReviews.clear()
        merchantReviewsHolder.clear()

        DBUtils.getMerchantReviews(CurrentMerchant.name, object : IGetMerchantReviews {
            override fun onCallback(successful: Boolean, merchantReviews: ArrayList<MerchantReview>) {
                if (successful) {

                    if (merchantReviews.isNotEmpty()) {
                        this@MerchantReviewsFragment.merchantReviews = merchantReviews
                        for (merchantReview: MerchantReview in merchantReviews) {
                            merchantReviewsHolder.add(CustomerNotification(merchantReview.customerName, merchantReview.customerName, "Tap to view", "Merchant Review", merchantReview.date))
                        }

                        setMerchantReviewsRecycler()
                    } else {
                        Toast.makeText(merchantHome, "No reviews found", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Toast.makeText(merchantHome, "Couldn't load reviews", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    fun setMerchantReviewsRecycler() {

        Collections.sort(merchantReviews, Collections.reverseOrder<DateComparator>())
        Collections.sort(merchantReviewsHolder, Collections.reverseOrder<DateComparator>())
        
        merchantNotificationsAdapter = MerchantNotificationsAdapter(merchantReviewsHolder, merchantReviews as ArrayList<DateComparator>, merchantHome!!, null)
        reviewsLayoutManager = LinearLayoutManager(merchantHome)
        rvReviews.layoutManager = reviewsLayoutManager
        rvReviews.adapter = merchantNotificationsAdapter

        progressDialog.dismiss()
    }

    fun getReviews(key: String, value: String?) {
        if (!reviewsLoadingCompleted) return
        reviewsLoadingCompleted = false
        reviewsLoaded = 0
        productReviews.clear()
        showProgressDialog("Loading reviews")
        var query: Query = db.collection("Product Reviews")
        query = query.whereEqualTo("Reviewed", true)
        query = query.whereEqualTo("Merchant Name", CurrentMerchant.name)
        if (key.isNotEmpty()) {
            query = if (key == "Product Rating")
                query.whereEqualTo(key, java.lang.Long.valueOf(value!!))
            else
                query.whereEqualTo(key, value)
        }
        query.get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (documentSnapshot in task.result!!.documents) {
                            val merchantName = documentSnapshot["Merchant Name"].toString()
                            val customerName = documentSnapshot["Customer Name"].toString()
                            val customerEmail = documentSnapshot["Customer Email"].toString()
                            val productCode = documentSnapshot["Product Code"].toString()
                            val productName = documentSnapshot["Product Name"].toString()
                            val productRating = Integer.valueOf(documentSnapshot["Product Rating"].toString())
                            val reviewDescription = documentSnapshot["Review Description"].toString()
                            val reviewTitle = documentSnapshot["Review Title"].toString()
                            val qrId = documentSnapshot["QR Id"].toString()
                            val productCategory = documentSnapshot["Product Category"].toString()
                            val date = documentSnapshot.getTimestamp("Date")
                            val dateString = DateUtils.dateToString(date!!.toDate())
                            val completed = documentSnapshot["Completed"] as Boolean
                            val reviewed = documentSnapshot["Reviewed"] as Boolean
                            val productReview = ProductReview(customerEmail, customerName, merchantName, productCode, productName, productCategory, productRating, reviewDescription, reviewTitle, qrId, completed, reviewed, dateString)
                            productReviews.add(productReview)
                        }
                        if (productReviews.isNotEmpty()) {
                            checkAndSetRecycler()
                        } else {
                            Toast.makeText(merchantHome, "No reviews found", Toast.LENGTH_SHORT).show()
                            checkAndSetRecycler()
                        }
                    } else {
                        Toast.makeText(merchantHome, "Couldn't load reviews", Toast.LENGTH_SHORT).show()
                    }
                    progressDialog.dismiss()
                    reviewsLoadingCompleted = true
                }
    }

    private fun checkAndSetRecycler() {
        reviewsLoaded++
        if (reviewsLoaded > 0) {
            setUpReviewsRecycler()
        }
    }

    override fun onResume() {
        super.onResume()
        merchantHome!!.setPageTitle("Reviews")
    }

}
