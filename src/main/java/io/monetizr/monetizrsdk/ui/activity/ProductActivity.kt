package io.monetizr.monetizrsdk.ui.activity

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.*
import io.monetizr.monetizrsdk.*
import io.monetizr.monetizrsdk.R
import io.monetizr.monetizrsdk.dto.Checkout
import io.monetizr.monetizrsdk.dto.PaymentInfo
import io.monetizr.monetizrsdk.dto.Product
import io.monetizr.monetizrsdk.dto.ShippingRate
import io.monetizr.monetizrsdk.ui.adapter.ImageGalleryAdapter
import io.monetizr.monetizrsdk.ui.adapter.ItemIndicator
import io.monetizr.monetizrsdk.ui.adapter.ItemSnapHelper
import io.monetizr.monetizrsdk.ui.dialog.*
import kotlinx.android.synthetic.main.activity_product.*
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class ProductActivity : AppCompatActivity(), ShippingRateDialogListener {
    private var userMadeInteraction: Boolean = false
    private var activityLaunchedStamp: Long = 0
    private val LOAD_PAYMENT_DATA_REQUEST_CODE = 991
    private lateinit var paymentsClient: PaymentsClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product)
        activityLaunchedStamp = System.currentTimeMillis()
        startService(Intent(baseContext, ClearedService::class.java))
        paymentsClient = PaymentsUtil.createPaymentsClient(this)

        val imageGalleryAdapter = ImageGalleryAdapter(this, ArrayList())
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val recyclerView = findViewById<RecyclerView>(R.id.productImagesView)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager
        ItemSnapHelper().attachToRecyclerView(recyclerView)
        recyclerView.addItemDecoration(ItemIndicator())
        recyclerView.adapter = imageGalleryAdapter

        possiblyShowGooglePayButton()
        closeButtonView.setOnClickListener { this.finish() }
        checkoutButtonView.setOnClickListener { checkout() }
        payButtonView.setOnClickListener { checkoutWithPaymentTokenButtonClick() }

        hideNavigationBar()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        userMadeInteraction = true
    }

    override fun onStop() {
        super.onStop()
        val productTag = intent!!.getStringExtra("productTag")
        val productVisibleTime = System.currentTimeMillis() - activityLaunchedStamp
        Telemetrics.impressionvisible(productVisibleTime, productTag)

        if (!userMadeInteraction) {
            Telemetrics.dismiss(productTag)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //calculateElementSize(newConfig.orientation)
        // TODO
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            LOAD_PAYMENT_DATA_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK ->
                        data?.let { intent ->
                            PaymentData.getFromIntent(intent)?.let(::handlePaymentSuccess)
                        }
                    Activity.RESULT_CANCELED -> {
                    }

                    AutoResolveHelper.RESULT_ERROR -> {
                        AutoResolveHelper.getStatusFromIntent(data)?.let {
                            handleError(it.statusCode)
                        }
                    }
                }
                payButtonView.isClickable = true
            }
        }
    }

    private fun hideNavigationBar() {
        rootView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    private fun possiblyShowGooglePayButton() {
        val isReadyToPayJson = PaymentsUtil.isReadyToPayRequest() ?: return
        val request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString()) ?: return

        paymentsClient.isReadyToPay(request).addOnCompleteListener { completedTask ->
            completedTask.getResult(ApiException::class.java)?.let(::showGooglePayButtonIfAvailable)
        }
    }

    private fun showGooglePayButtonIfAvailable(available: Boolean) {
        if (available) {
            payButtonView.visibility = View.VISIBLE
        } else {
            payButtonView.visibility = View.GONE
        }
    }

    override fun onShippingRateSelect(shippingRate: ShippingRate) {

    }

    private fun showBottomModalWithShipping(checkout: Checkout) {
        val modalBottomSheet = ShippingRateDialog.newInstance(checkout)
        modalBottomSheet.show(supportFragmentManager, ShippingRateDialog.TAG)
    }

    private fun checkout(proceedWithPayment: Boolean = false) {
        MonetizrSdk.showProgressDialog()

        //  var variantForCheckout: JSONObject = searchSelectedVariant()

        val language = Locale.getDefault().displayLanguage
        val jsonBody = JSONObject()
        // val variantId = variantForCheckout.getString("id")

//        jsonBody.put("product_handle", productTag)
//        jsonBody.put("variantId", variantId)
//        jsonBody.put("language", language)
//        jsonBody.put("quantity", 1)

//        if (proceedWithPayment) {
//            jsonBody.put("shippingAddress", shippingAddress)
//        }

        val url = MonetizrSdk.apiAddress + "products/checkout"

        if (MonetizrSdk.firstCheckout) {
            Telemetrics.firstimpressioncheckout()
            MonetizrSdk.firstCheckout = false
        }

        val request = getPostRequest(url, jsonBody, {}, {})
        SingletonRequest.getInstance(this).addToRequestQueue(request)
    }

    private fun checkoutWithPaymentTokenButtonClick() {
        requestPayment()
    }

    private fun showProductView(checkoutInfo: JSONObject) {
        val checkoutErrors = checkoutInfo.getJSONObject("data").getJSONObject("checkoutCreate")
            .getJSONArray("checkoutUserErrors")
        val checkoutRedirect =
            checkoutInfo.getJSONObject("data").getJSONObject("checkoutCreate")
                .getJSONObject("checkout").getString("webUrl")

        if (checkoutErrors.length() <= 0) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(checkoutRedirect)))
        } else {
            Toast.makeText(this, R.string.error_while_checkout, Toast.LENGTH_LONG).show()
        }
    }

    private fun requestPayment() {
        payButtonView.isClickable = false

//        var variantForCheckout: JSONObject = searchSelectedVariant()
//        val totalPrice = variantForCheckout.getJSONObject("priceV2").getString("amount")

//        val paymentDataRequestJson =
//            PaymentsUtil.getPaymentDataRequest(totalPrice)
//
//        val request = PaymentDataRequest.fromJson(paymentDataRequestJson.toString())
//
//        if (request != null) {
//            AutoResolveHelper.resolveTask(
//                paymentsClient.loadPaymentData(request),
//                this,
//                LOAD_PAYMENT_DATA_REQUEST_CODE
//            )
//        }
    }

    private fun handlePaymentSuccess(paymentData: PaymentData) {
        if (paymentData.shippingAddress != null) {
            paymentData.shippingAddress!!.let {
                PaymentInfo(
                    it.name,
                    it.name,
                    it.address1,
                    it.address2,
                    it.locality,
                    it.administrativeArea,
                    it.countryCode,
                    it.postalCode
                )
            }
            checkout(true)
        }
    }

    private fun handleError(statusCode: Int) {
        Log.w("loadPaymentData failed", String.format("Error code: %d", statusCode))
    }

    private fun getPostRequest(
        url: String,
        body: JSONObject,
        onSuccess: (response: JSONObject) -> Any,
        onError: (error: VolleyError) -> Any
    ): JsonObjectRequest {
        return object : JsonObjectRequest(
            Method.POST, url, body,
            Response.Listener { response -> onSuccess(response) },
            Response.ErrorListener { error -> onError(error) }) {
            override fun getHeaders(): MutableMap<String, String> {
                val header = mutableMapOf<String, String>()
                header["Authorization"] = "Bearer " + MonetizrSdk.apikey
                return header
            }
        }
    }

    companion object {
        fun start(product: Product, productTag: String) {

        }
    }
}
