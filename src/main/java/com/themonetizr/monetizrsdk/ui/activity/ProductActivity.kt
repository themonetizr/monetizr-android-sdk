package com.themonetizr.monetizrsdk.ui.activity

//import com.google.android.gms.common.api.ApiException
//import com.google.android.gms.wallet.*
//import com.stripe.android.ApiResultCallback
//import com.stripe.android.Stripe
//import com.stripe.android.model.ConfirmPaymentIntentParams
//import com.stripe.android.model.PaymentMethod
//import com.stripe.android.model.PaymentMethodCreateParams
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.themonetizr.monetizrsdk.ClearedService
import com.themonetizr.monetizrsdk.MonetizrSdk.Companion.logError
import com.themonetizr.monetizrsdk.R
import com.themonetizr.monetizrsdk.api.Telemetrics
import com.themonetizr.monetizrsdk.api.WebApi
import com.themonetizr.monetizrsdk.dto.*
import com.themonetizr.monetizrsdk.misc.ConfigHelper
import com.themonetizr.monetizrsdk.misc.Parameters
import com.themonetizr.monetizrsdk.ui.adapter.ImageGalleryAdapter
import com.themonetizr.monetizrsdk.ui.adapter.ItemIndicator
import com.themonetizr.monetizrsdk.ui.adapter.ItemSnapHelper
import com.themonetizr.monetizrsdk.ui.dialog.*
import com.themonetizr.monetizrsdk.ui.helpers.ProgressDialogBuilder
import kotlinx.android.synthetic.main.activity_product.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable




class ProductActivity : AppCompatActivity(), ShippingRateDialogListener, ShippingAddressDialogListener, OptionsDialogListener {
    private var userMadeInteraction: Boolean = false
    private var activityLaunchedStamp: Long = 0
//    private lateinit var paymentsClient: PaymentsClient
    private val selectedOptions: ArrayList<String> = ArrayList()
    private var progressDialog: AlertDialog? = null
    private var chosenVariant: Variant? = null
    private var shippingAddress: ShippingAddress? = null
    private var tag: String = ""
    private var productJson: JSONObject = JSONObject()
    private var product: Product = Product()
    private var apiKey: String = ""
    private var apiAddress: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product)
        activityLaunchedStamp = System.currentTimeMillis()
//        paymentsClient = PaymentsUtil.createPaymentsClient(this)
        progressDialog = ProgressDialogBuilder.makeProgressDialog(this)
        startService(Intent(baseContext, ClearedService::class.java))

        tag = intent!!.getStringExtra(Parameters.PRODUCT_TAG)!!
        val json = intent!!.getStringExtra(Parameters.PRODUCT_JSON)!!
        productJson = JSONObject(json)
        product = Product(productJson)
        apiKey = ConfigHelper.getConfigValue(this, Parameters.RAW_API_KEY)
        apiAddress = ConfigHelper.getConfigValue(this, Parameters.RAW_API_ENDPOINT)

        initImageAdapter(product.images)

        // Show Google Pay if it is available in specified country
        // Google pay won`t be as option before testing accordingly
        // @TODO Google Pay
        // initGooglePayButton()

        // Init checkout
        initCheckoutTitle(product)

        if (product.variants.isEmpty() == false) {
            variantContainerView.isEnabled = true
            var first = product.getFirstVariant()!!

            // On orientation change chosen variant is being saved so it can be restored
            if (savedInstanceState?.containsKey(CHOSEN_VARIANT_KEY) != null ) {
                first = savedInstanceState.getSerializable(CHOSEN_VARIANT_KEY) as Variant
            }
            this.chosenVariant = first
            initProductPriceTitle(first)
            initProductVariantsTitle(first)
            initDefaultSelected(first)
        } else {
            variantContainerView.isEnabled = false
        }

        productTitleView.text = product.title
        productDescriptionView.text = product.descriptionIos

        closeButtonView.setOnClickListener { finish() }
        payButtonView.setOnClickListener { payGooglePlayTap(productJson) }

        variantContainerView.setOnClickListener { showOptionDialog(json) }

        if (lockedProduct) {
            checkoutButtonView.isEnabled = false
        }
        if (product.claimable == true) {
            checkoutButtonView.setOnClickListener { showShippingAddressDialog() }
        } else {
            checkoutButtonView.setOnClickListener { checkout(null) }
        }

        hideStatusBar()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        userMadeInteraction = true
    }


    override fun onStop() {
        super.onStop()
        val productTag = intent!!.getStringExtra(Parameters.PRODUCT_TAG)
        val productVisibleTime = System.currentTimeMillis() - activityLaunchedStamp
        Telemetrics.impressionvisible(productVisibleTime, productTag)

        if (userMadeInteraction == false) {
            Telemetrics.dismiss(productTag)
        }
    }

    override fun onResume() {
        super.onResume()
        hideStatusBar()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideStatusBar()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        hideStatusBar()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState?.putStringArrayList(SELECTED_OPTIONS_KEY, selectedOptions)
        outState?.putSerializable(CHOSEN_VARIANT_KEY, this.chosenVariant as Serializable)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState?.let {
            if (it.containsKey(SELECTED_OPTIONS_KEY)) {
                val restored = it.getStringArrayList(SELECTED_OPTIONS_KEY)
                if (restored != null) {
                    this.selectedOptions.clear()
                    this.selectedOptions.addAll(restored)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

//        if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE && resultCode == RESULT_OK) {
//            if (data != null) {
//                val paymentData = PaymentData.getFromIntent(data)
//
//                if (paymentData != null) {
//                    val tag = intent.getStringExtra(Parameters.PRODUCT_TAG)!!
//                    val json = intent.getStringExtra(Parameters.PRODUCT_JSON)!!
//                    val productJson = JSONObject(json)
//
//                    checkout(paymentData)
//                }
//            } else {
//                val tag = intent.getStringExtra(Parameters.PRODUCT_TAG)!!
//                val json = intent.getStringExtra(Parameters.PRODUCT_JSON)!!
//                val productJson = JSONObject(json)
//                checkout(null)
//            }
//        }
//
//        if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE && resultCode == AutoResolveHelper.RESULT_ERROR) {
//            AutoResolveHelper.getStatusFromIntent(data)?.let { handleError(it.statusCode) }
//        }
//        payButtonView.isEnabled = true
    }

    override fun onOptionsSelect(options: ArrayList<HierarchyVariant>) {
        hideStatusBar()
        selectedOptions.clear()
        for (option in options) {
            selectedOptions.add(option.id)
        }
        initProductVariantsValues(selectedOptions)
        updateProductPriceValues()
    }

    override fun onShippingRateSelect(paymentData: String, checkout: JSONObject, shippingRate: ShippingRate) {

        // Create shipping address if it was not yet present
//        val data = PaymentData.fromJson(paymentData)
//        shippingAddress = if (data.shippingAddress != null) {
//            val address = data.shippingAddress!!
//            ShippingAddressInto(address.name, address.name, address.address1, address.address2, address.locality, address.administrativeArea, address.countryCode, address.postalCode).getJsonObject()
//            JSONObject()
//        } else {
//            JSONObject()
//        }

        val body = CheckoutWithPaymentBody.createBody(paymentData, checkout, tag, shippingAddress, shippingRate)

        updateCheckout(body, paymentData)
    }

    private fun updateCheckout(requestBody: JSONObject, paymentData: String? = null) {
        showProgressDialog()
        val url = apiAddress + "products/updatecheckout"

        WebApi.getInstance(this).makeRequest(url, Request.Method.POST, requestBody, apiKey, { response ->

            if (!response.has("data")) {
                showUnexpectedExceptions(response)
            } else {
                val updateShipping = response.getJSONObject("data").getJSONObject("updateShippingLine")
                val checkoutErrors = updateShipping.getJSONArray("checkoutUserErrors")

                if (checkoutErrors.length() > 0) {
                    showDialogMessage(parseCheckoutUserErrors(checkoutErrors), "error")
                } else {
                    val checkoutResponse = updateShipping.getJSONObject("checkout")

                    if (product.claimable == true) {
                        completeCheckoutClaim(checkoutResponse)
                    } else {
                        continueWithPayment(checkoutResponse, paymentData)
                    }
                }
            }
        }, {
            hideProgressDialog()
            logError(it, supportFragmentManager)
        })
    }

    private fun continueWithPayment(checkout: JSONObject, paymentData: String?) {
//        val paymentBody = CheckoutWithPaymentBody.paymentBody(paymentData, checkout, tag)
//        WebApi.getInstance(this).makeRequest(apiAddress + "products/payment", Request.Method.POST, paymentBody, apiKey, {
//            // This is a charge token request from server
//            val paymentMethodCreateParams = PaymentMethodCreateParams.createFromGooglePay(JSONObject(paymentData))
//
//            // Now use the `paymentMethodCreateParams` object to create a PaymentMethod
//            val stripe = Stripe(this, "pk_test_OS6QyI1IBsFtonsnFk6rh2wb00mSXyblvu");
//            stripe.createPaymentMethod(
//                paymentMethodCreateParams,
//                paymentBody.getString("payment_token"),
//                object: ApiResultCallback<PaymentMethod> {
//                    override fun onSuccess(result: PaymentMethod) {
//                        // Confirm payment with method id and client secret from server
//                        if (result.id != null) {
//                            val methodId = result.id
//                            stripe.confirmPayment(this@ProductActivity,
//                                ConfirmPaymentIntentParams.createWithPaymentMethodId(
//                                    methodId!!,
//                                    it.getString("intent"),
//                                    "https://themonetizr.com"
//                                )
//                            )
//                        }
//                    }
//
//                    override fun onError(e: Exception) {
//                        logError("Stripe exception " + e.toString(), supportFragmentManager)
//                    }
//                }
//            )
//                hideProgressDialog()
//                finish()
//        }, {
//            hideProgressDialog()
//            logError(it, supportFragmentManager)
//        })
    }

    // Region checkout

// proceedWithPayment: PaymentData? = null

    private fun checkout(proceedWithPayment: String? = null) {
        val variant: JSONObject? = searchSelectedVariant(productJson)
        val url = apiAddress + "products/checkout"
        val withPayment = proceedWithPayment != null
        if (variant != null) {
            showProgressDialog()
            val jsonBody = CheckoutBody.createBody(variant, tag, shippingAddress)

            WebApi.getInstance(this).makeRequest(
                url, Request.Method.POST, jsonBody, apiKey,
                { response ->
                    hideProgressDialog()

                    // Process has realy unexpected errors, outside of errors that can create in valid flow
                    if (!response.has("data")) {
                        showUnexpectedExceptions(response)
                    } else {
                        val checkoutCreate = response.getJSONObject("data").getJSONObject("checkoutCreate")
                        val checkoutErrors = checkoutCreate.getJSONArray("checkoutUserErrors")

                        if (checkoutErrors.length() > 0) {
                            showDialogMessage(parseCheckoutUserErrors(checkoutErrors), "error")
                        } else {
                            val checkout = checkoutCreate.getJSONObject("checkout")
                            if (withPayment) {
                                showShippingRateDialog(proceedWithPayment!!, checkout)
                            } else {
                                if (product.claimable == true) {
                                    // Complete free claiming process
                                    checkoutClaimProcess(checkout)
                                } else {
                                    showProductView(checkout.getString("webUrl"))
                                }
                            }
                        }
                    }
                },
                {
                    hideProgressDialog()
                    logError(it, supportFragmentManager)
                }
            )

        }
    }

    private fun showProductView(url: String?) {
        try {
            val starter = Intent(this@ProductActivity, CheckoutViewActivity::class.java)
            starter.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            starter.putExtra(CheckoutViewActivity.WEBSITE_ADDRESS, url)
            startActivity(starter)

        } catch (error: Throwable) {
        }
    }

    //endregion

    // region claimable product

    override fun onShippingAddressEntered(address: ShippingAddress) {
        shippingAddress = address
        checkout(null)
    }

    private fun checkoutClaimProcess(checkoutJson: JSONObject) {
        // Prepare request body, create shipping Address
        val checkout = Checkout(checkoutJson)

        // Auto select shipping rate with price 0.00, if not available, show error
        var zeroPriceShippingRate: ShippingRate ?= null
        for (rate in checkout.shippingRates) {
            if (rate.price.amount <= 0) {
                zeroPriceShippingRate = rate
            }
        }

        if (zeroPriceShippingRate?.equals(null) == true) {
            // In this case, there really is not a price with value zero, impossible to continue
            var message = getText(R.string.free_shipping_impossible).toString()
            checkoutJson.getString("webUrl")
            message += "\n" + checkoutJson.getString("webUrl")
            showDialogMessage(message, "error")
        } else {
            val requestBody = CheckoutBody.createUpdateBody(checkoutJson, tag, shippingAddress, zeroPriceShippingRate)
            updateCheckout(requestBody)
        }
    }

    private fun completeCheckoutClaim(checkout: JSONObject) {
        // Make final request to claim product
        val claimBody = CheckoutBody.createClaimBody(checkout, playerId)

        WebApi.getInstance(this).makeRequest(apiAddress + "products/claimorder", Request.Method.POST, claimBody, apiKey, {response ->
            hideProgressDialog()

            if (response.getString("status") == "error") {
                logError(response.toString())
                showDialogMessage(response.getString("message"), "error")
            } else {
                showDialogMessage(getText(R.string.product_claimed).toString(), "success")
            }
        }, {
            hideProgressDialog()
            logError(it, supportFragmentManager)
        })
    }
    // endregion

    //region ui

    private fun hideStatusBar() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    private fun initGooglePayButton() {
//        val isReadyToPayJson = PaymentsUtil.isReadyToPayRequest() ?: return
//        val request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString()) ?: return
//
//        paymentsClient.isReadyToPay(request).addOnCompleteListener { completedTask ->
//            completedTask.getResult(ApiException::class.java)?.let(::showGooglePayButtonIfAvailable)
//        }
    }

    private fun showGooglePayButtonIfAvailable(available: Boolean) {
        if (available) {
            payButtonView.visibility = View.VISIBLE
        } else {
            payButtonView.visibility = View.GONE
        }
    }

    private fun initImageAdapter(photos: ArrayList<String>) {
        val imageGalleryAdapter = ImageGalleryAdapter(this, photos)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        if (lockedProduct) {
            topView.foreground = ColorDrawable(ContextCompat.getColor(this, R.color.imageOverlay))
            lockedIcon.visibility = View.VISIBLE
        } else {
            topView.foreground = null
            lockedIcon.visibility = View.GONE
        }

        productImagesView.setHasFixedSize(true)
        productImagesView.layoutManager = layoutManager
        ItemSnapHelper().attachToRecyclerView(productImagesView)
        productImagesView.addItemDecoration(ItemIndicator())
        productImagesView.adapter = imageGalleryAdapter
    }

    private fun initProductPriceTitle(variant: Variant) {
        productPriceView.text = variant.priceV2.formatString()
        if (product.claimable != true && variant.compareAtPriceV2 != null) {
            productDiscountView.text = variant.compareAtPriceV2?.formatString()
            productDiscountView.paintFlags = productDiscountView.paintFlags or STRIKE_THRU_TEXT_FLAG
        } else {
            productDiscountView.text = ""
        }
    }

    private fun initProductVariantsTitle(variant: Variant) {
        if (variant.selectedOptions.size > 0) {
            option1NameView.text = variant.selectedOptions[0].name
            option1ValueView.text = variant.selectedOptions[0].value

            option1NameView.visibility = View.VISIBLE
            option1ValueView.visibility = View.VISIBLE
            option1IconView.visibility = View.VISIBLE
        } else {
            option1NameView.visibility = View.GONE
            option1ValueView.visibility = View.GONE
            option1IconView.visibility = View.GONE
        }

        if (variant.selectedOptions.size > 1) {
            option2NameView.text = variant.selectedOptions[1].name
            option2ValueView.text = variant.selectedOptions[1].value

            option2NameView.visibility = View.VISIBLE
            option2ValueView.visibility = View.VISIBLE
            option2IconView.visibility = View.VISIBLE
        } else {
            option2NameView.visibility = View.GONE
            option2ValueView.visibility = View.GONE
            option2IconView.visibility = View.GONE
        }

        if (variant.selectedOptions.size > 2) {
            option3NameView.text = variant.selectedOptions[2].name
            option3ValueView.text = variant.selectedOptions[2].value

            option3NameView.visibility = View.VISIBLE
            option3ValueView.visibility = View.VISIBLE
            option3IconView.visibility = View.VISIBLE
        } else {
            option3NameView.visibility = View.GONE
            option3ValueView.visibility = View.GONE
            option3IconView.visibility = View.GONE
        }
    }

    private fun initProductVariantsValues(options: ArrayList<String>) {
        if (options.isEmpty() == false) {

            if (options.size > 0) {
                option1ValueView.text = options[0]
            }

            if (options.size > 1) {
                option2ValueView.text = options[1]
            }

            if (options.size > 2) {
                option3ValueView.text = options[2]
            }
        }
    }

    private fun updateProductPriceValues() {
        val json = intent.getStringExtra(Parameters.PRODUCT_JSON)!!
        val selectedVariant = searchSelectedVariant(JSONObject(json))
        if (selectedVariant != null) {
            val variant = Variant(selectedVariant)
            this.chosenVariant = variant
            initProductPriceTitle(variant)
        }
    }

    private fun initCheckoutTitle(product: Product) {
        if (product.buttonTitle != null && product.buttonTitle.isNotEmpty()) {
            checkoutButtonView.text = product.buttonTitle
        }
    }

    private fun showOptionDialog(product: String) {
        val fragment = OptionsDialog.newInstance(product, selectedOptions)
        fragment.show(supportFragmentManager, "Option")
    }

    private fun payGooglePlayTap(productJson: JSONObject) {
//        payButtonView.isEnabled = false
//        val variantForCheckout: JSONObject? = searchSelectedVariant(productJson)
//        if (variantForCheckout != null) {
//            val totalPrice = variantForCheckout.getJSONObject("priceV2").getString("amount")
//
//            val paymentDataRequestJson = PaymentsUtil.getPaymentDataRequest(totalPrice)
//            if (paymentDataRequestJson == null) {
//                logError("Can't fetch payment data request", supportFragmentManager)
//                return
//            }
//            val request = PaymentDataRequest.fromJson(paymentDataRequestJson.toString())
//
//            if (request != null) {
//                AutoResolveHelper.resolveTask(paymentsClient.loadPaymentData(request), this, LOAD_PAYMENT_DATA_REQUEST_CODE)
//            }
//        } else {
//            logError("Can't fetch payment data request", supportFragmentManager)
//        }
//        payButtonView.isEnabled = true
    }

    private fun showProgressDialog() {
        progressDialog?.show()
    }

    private fun hideProgressDialog() {
        progressDialog?.dismiss()
    }

    private fun showShippingRateDialog(paymentInfo: String, checkoutInfo: JSONObject) {
        val shippingDialog = ShippingRateDialog.newInstance(paymentInfo, checkoutInfo)
        shippingDialog.show(supportFragmentManager, ShippingRateDialog.TAG)
    }


    private fun showShippingAddressDialog() {
        if (playerId == null || playerId == "" ) {
            showDialogMessage(getString(R.string.required_values_not_present), "error")
        } else {
            val shippingDialog = ShippingAddressDialog.newInstance()
            shippingDialog.show(supportFragmentManager, ShippingAddressDialog.TAG)
        }
    }
    //endregion

    //region variants
    private fun searchSelectedVariant(product: JSONObject): JSONObject? {
        if (selectedOptions.isEmpty() == false) {
            if (product.has("variants")) {
                val variantsArray = product.getJSONObject("variants").getJSONArray("edges")
                for (variantIndex in 0 until variantsArray.length()) {
                    val variant = variantsArray.getJSONObject(variantIndex).getJSONObject("node")
                    val variantSelectedOptions = variant.getJSONArray("selectedOptions")
                    var numberOfMatchingOptions = 0

                    for (optionIndex in 0 until variantSelectedOptions.length()) {
                        val optionValue = variantSelectedOptions.getJSONObject(optionIndex).getString("value")

                        for (userSelected in selectedOptions) {
                            if (userSelected == optionValue) {
                                numberOfMatchingOptions++
                                break
                            }
                        }
                    }

                    if (numberOfMatchingOptions == variantSelectedOptions.length()) {
                        return variant
                    }
                }
            }
        }

        return null
    }

    private fun initDefaultSelected(variant: Variant) {
        for (option in variant.selectedOptions) {
            selectedOptions.add(option.value)
        }
    }
    //endregion

    private fun handleError(statusCode: Int) {
        Log.w("loadPaymentData failed", String.format("Error code: %d", statusCode))
    }

    private fun showDialogMessage(message: String, type: String) {
        val dialog = MessageDialog.newInstance(message, type)
        dialog.show(supportFragmentManager, MessageDialog.TAG)
    }

    private fun parseCheckoutUserErrors(errors: JSONArray): String {
        var messages = ""
        for (error in 0 until errors.length()) {
            val item = errors.getJSONObject(error)
            messages += item.getString("message") + "\n"
        }
        return messages
    }

    private fun showUnexpectedExceptions(response: JSONObject) {
        logError(response.toString())
        if (response.has("errors")) {
            val errorObject = response.getJSONArray("errors").getJSONObject(0)
            showDialogMessage(errorObject.getString("message"), "error")
        } else {
            showDialogMessage(getText(R.string.unexpected_exception).toString(), "error")
        }
    }

    companion object {
        var firstCheckout: Boolean = true
        const val LOAD_PAYMENT_DATA_REQUEST_CODE = 991
        const val SELECTED_OPTIONS_KEY = "SELECTED_OPTIONS_KEY"
        const val CHOSEN_VARIANT_KEY = "CHOSEN_VARIANT_KEY"
        var playerId: String? = null
        var lockedProduct: Boolean = false

        fun start(context: Context, productJson: String, productTag: String) {
            val starter = Intent(context, ProductActivity::class.java)
            starter.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            starter.putExtra(Parameters.PRODUCT_JSON, productJson)
            starter.putExtra(Parameters.PRODUCT_TAG, productTag)
            context.startActivity(starter)
        }
    }
}
