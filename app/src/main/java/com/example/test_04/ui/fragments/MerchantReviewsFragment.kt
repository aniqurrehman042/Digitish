package com.example.test_04.ui.fragments

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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

    var merchantReviewsLoadedOnce: Boolean = false

    private var madeMerchantAsynchCalls: Boolean = false
    private lateinit var rvReviews: RecyclerView
    private lateinit var rvFilters: RecyclerView
    private lateinit var tvNoRating: TextView

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
    private var productCategory: String? = null
    private var merchantName: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_merchant_reviews, container, false)
        merchantHome = activity as MerchantHome
        showProgressDialog("")
        progressDialog.dismiss()

        findViews(view)
        init()

        return view
    }

    private fun findViews(view: View?) {
        rvReviews = view!!.findViewById(R.id.rv_reviews)
        rvFilters = view.findViewById(R.id.rv_select)
        tvNoRating = view.findViewById(R.id.tv_no_rating)
    }

    private fun init() {
        if (arguments != null) {
            firstRunCheck()
            checkProductCategory()
        }
        arguments = null
        if (filters.isEmpty())
            setUpFilters()
        setUpFilterRecycler()
        if (!merchantReviewsLoadedOnce && filters[0].filterName == "Merchant Rating")
            getMerchantReviews()
        else if ((productReviews.isNotEmpty() && filterAdapter!!.selectedFilter != 0) || (productReviews.isNotEmpty() && filters[0].filterName == "Reviews")) {
            setUpReviewsRecycler()
        } else if (filters[0].filterName == "Reviews") {
            getReviews("", "")
        } else {
            setMerchantReviewsRecycler()
        }
    }

    private fun checkProductCategory() {
        productCategory = arguments!!.getString("Product Category")
        merchantName = arguments!!.getString("Merchant Name")
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
        tvNoRating.visibility = View.GONE
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
        val filterNames: Array<String>
        if (productCategory == null)
            filterNames = arrayOf("Merchant Rating", "Reviews", "5 star reviews", "4 star reviews", "3 star reviews", "2 star reviews", "1 star reviews")
        else
            filterNames = arrayOf("Reviews", "5 star reviews", "4 star reviews", "3 star reviews", "2 star reviews", "1 star reviews")
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

        if (madeMerchantAsynchCalls) return

        madeMerchantAsynchCalls = true

        if (progressDialog != null)
            progressDialog.dismiss()

        showProgressDialog("Loading Reviews")

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
                    } else {
                        Toast.makeText(merchantHome, "No reviews found", Toast.LENGTH_SHORT).show()
                        progressDialog.dismiss()
                    }

                } else {
                    Toast.makeText(merchantHome, "Couldn't load reviews", Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }

                setMerchantReviewsRecycler()
            }
        })
    }

    fun setMerchantReviewsRecycler() {

        tvNoRating.visibility = View.GONE

        Collections.sort(merchantReviews, Collections.reverseOrder<DateComparator>())
        Collections.sort(merchantReviewsHolder, Collections.reverseOrder<DateComparator>())

        if (merchantReviews.isEmpty()) {
            tvNoRating.visibility = View.VISIBLE
            tvNoRating.text = "No one has rated ${CurrentMerchant.name} yet"
        }

        merchantNotificationsAdapter = MerchantNotificationsAdapter(merchantReviewsHolder, merchantReviews as ArrayList<DateComparator>, merchantHome!!, null, null)
        reviewsLayoutManager = LinearLayoutManager(merchantHome)
        rvReviews.layoutManager = reviewsLayoutManager
        rvReviews.adapter = merchantNotificationsAdapter

        madeMerchantAsynchCalls = false
        merchantReviewsLoadedOnce = true
        progressDialog.dismiss()
    }

    fun getReviews(key: String, value: String?) {
        if (!reviewsLoadingCompleted) return
        reviewsLoadingCompleted = false
        reviewsLoaded = 0
        productReviews.clear()

        if (progressDialog != null)
            progressDialog.dismiss()

        showProgressDialog("Loading Reviews")

        var query: Query = db.collection("Product Reviews")
        query = query.whereEqualTo("Reviewed", true)
        query = query.whereEqualTo("Merchant Name", CurrentMerchant.name)
        if (key.isNotEmpty()) {
            query = if (key == "Product Rating")
                query.whereEqualTo(key, java.lang.Long.valueOf(value!!))
            else
                query.whereEqualTo(key, value)
        }

        if (productCategory != null) {
            query = query.whereEqualTo("Product Category", productCategory)
            query = query.whereEqualTo("Merchant Name", merchantName)
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
        if (productCategory == null)
            merchantHome!!.onMerchantReviewsFragmentResume()
        else
            merchantHome!!.onProductCategoryMerchantReviewsFragmentResume(productCategory)
    }

    override fun onPause() {
        super.onPause()
        merchantHome!!.onMerchantReviewsFragmentPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (progressDialog != null) progressDialog.dismiss()
    }

}
