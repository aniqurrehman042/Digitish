package com.example.test_04.ui.fragments

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test_04.R
import com.example.test_04.adapters.SearchReviewsAdapter
import com.example.test_04.models.ProductReview
import com.example.test_04.ui.MerchantHome
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class MerchantReviewsFragment : Fragment() {

    private lateinit var rvReviews: RecyclerView

    private var productReviews = ArrayList<ProductReview>()
    private var searchReviewsAdapter: SearchReviewsAdapter? = null
    private var merchantHome: MerchantHome? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_merchant_reviews, container, false)
        merchantHome = activity as MerchantHome

        findView(view)
        init()

        return view
    }

    private fun findView(view: View?) {
        rvReviews = view!!.findViewById(R.id.rv_reviews)
    }

    private fun init() {
        getReviews()
        setUpReviewsRecycler()
    }

    private fun getReviews() {
        productReviews = arguments!!.getSerializable("Product Reviews") as ArrayList<ProductReview>
    }

    private fun setUpReviewsRecycler() {
        searchReviewsAdapter = SearchReviewsAdapter(productReviews, null, merchantHome)
        var reviewsLayoutManager = LinearLayoutManager(merchantHome)
        rvReviews.layoutManager = reviewsLayoutManager
        rvReviews.adapter = searchReviewsAdapter
    }

}
