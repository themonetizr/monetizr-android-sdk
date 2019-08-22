package io.monetizr.monetizrsdk

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.google.android.flexbox.FlexboxLayout
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.*
import kotlinx.android.synthetic.main.activity_product.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


/**
 * Full-screen activity that shows product information
 */
class ProductActivity : AppCompatActivity(), EditDialogListener {

    // Recycler view - view that draws images
    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView

    // Adapter that takes care of images inside recycler view
    private lateinit var imageGalleryAdapter: ImageGalleryAdapter

    // Options inside variant selection, as a llist view that allows items to be changed
    private lateinit var optionListView: ListView

    // The first product variant that is being chosen
    private lateinit var initialVariant: JSONObject

    // Shipping address
    private lateinit var shippingAddress: JSONObject

    // Available variants fro product
    private lateinit var variants: JSONArray

    // Users selection options from vartiant selector
    private lateinit var usersSelectedOptions: ArrayList<String>

    // Product that initialized activity launch
    private var productTag: String = ""

    // Flag that specified when variant selection has been completed
    private var selectionAccomplished: Boolean = false

    // User has made any interaction within product activity
    private var userMadeInteraction: Boolean = false

    // Milliseconds when activity has been started
    private var activityLaunched: Long = 0

    // Checkout id returned from request for payment
    private lateinit var checkoutInfo: JSONObject

    private lateinit var mPaymentsClient: PaymentsClient

    /** A constant integer you define to track a request for payment data activity  */
    private val LOAD_PAYMENT_DATA_REQUEST_CODE = 991

    private lateinit var button_google_pay: View

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_product)

        var variantsAvailable = true

        // Start service that is trying to watch when activity is being destroyed
        startService(Intent(baseContext, ClearedService::class.java))

        // Store milliseconds on launch
        activityLaunched = System.currentTimeMillis()

        // Parse product information, die silently if some errors do happen
        try {
            // Check for Google Pay
            button_google_pay = findViewById<View>(R.id.button_google_pay)

            // Initialize a Google Pay API client for an environment suitable for testing.
            // It's recommended to create the PaymentsClient object inside of the onCreate method.
            mPaymentsClient = PaymentsUtil.createPaymentsClient(this)

            possiblyShowGooglePayButton()

            productTag = intent.getStringExtra("product_tag")
            val data = JSONObject(intent.getStringExtra("product"))
            val product = data.getJSONObject("data").getJSONObject("productByHandle")
            val buttonTitle = product.getString("button_title")

            if (buttonTitle.isNotEmpty()) {
                checkout_button.text = buttonTitle
            }

            product_title.text = product.getString("title")

            product_description.text = product.getString("description_ios")
            val images = product.getJSONObject("images").getJSONArray("edges")

            variants = product.getJSONObject("variants").getJSONArray("edges")
            val productOptions = product.getJSONArray("options")

            // Set initial selected variant and its values
            initialVariant = variants.getJSONObject(0).getJSONObject("node")
            val initPriceObj = initialVariant.getJSONObject("priceV2")
            val initPrice = initPriceObj.getString("currencyCode") + " " + initPriceObj.getString("amount")
            product_price.text = initPrice

            // If there are only limited number of variants included, for example one variant
            if (variants.length() > 1 ) {
                variant_chevron.text = ">"
                val selectedOptions = initialVariant.getJSONArray("selectedOptions")
                var selectedText = ""
                for (i in 0 until selectedOptions.length()) {
                    val option = selectedOptions.getJSONObject(i).getString("value")
                    selectedText = "$selectedText / $option"
                }
                variant_title.text = selectedText
            } else {
                variantsAvailable = false
                variant_title.text = ""
                variant_chevron.text = ""
            }

            // Create recycler view for images and add additional dot indicator and swipe lock
            val layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                this,
                androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL,
                false
            )
            recyclerView = findViewById(R.id.product_images)
            recyclerView.setHasFixedSize(true)
            recyclerView.layoutManager = layoutManager
            ItemSnapHelper().attachToRecyclerView(recyclerView)
            recyclerView.addItemDecoration(ItemIndicator())
            imageGalleryAdapter = ImageGalleryAdapter(this, ProductPhoto.getProductPhotos(images))

            // Create variant chooser if product does have variants available
            if (variantsAvailable) {
                variant_chooser.setOnClickListener{
                    userMadeInteraction = true

                    // This is the first time user clicks on variant selector
                    if (MonetizrSdk.firstImpressionClick) {
                        Telemetrics.firstimpressionclick()
                        MonetizrSdk.firstImpressionClick = false
                    }

                    // Instantiate dialog
                    val builder: AlertDialog.Builder = this.let {
                        AlertDialog.Builder(it)
                    }

                    // Add title and items to choose from
                    val numberOfIterations = productOptions.length()
                    var iteration = 0
                    val firstOption = productOptions.getJSONObject(iteration)
                    val optionValues = firstOption.getJSONArray("values")
                    val items = ArrayList<String>()
                    usersSelectedOptions = ArrayList()
                    selectionAccomplished = false

                    for (i in 0 until optionValues.length()) {
                        items.add(optionValues.getString(i))
                    }

                    // Inflate list view into dialog window
                    val inflater = layoutInflater
                    val dialogLayout = inflater.inflate(R.layout.list_view, null)
                    val titleView = inflater.inflate(R.layout.option_title, null)
                    val dataAdapter = ArrayAdapter<String>(this, R.layout.option, items)

                    optionListView = dialogLayout.findViewById(R.id.listview_1)
                    optionListView.adapter = dataAdapter
                    builder.setView(dialogLayout)

                    // Set custom dialog title and add item click listener
                    val dialogTitle: TextView = titleView.findViewById(R.id.dialog_title)
                    val dialogClose = titleView.findViewById<ImageView>(R.id.dialog_close)
                    dialogTitle.text = firstOption.getString("name")
                    builder.setCustomTitle(titleView)
                    val dialog: AlertDialog = builder.create()
                    dialogClose.setOnClickListener{
                        dialog.dismiss()
                    }
                    optionListView.setOnItemClickListener {
                            _,
                            _,
                            position,
                            _
                        ->
                            iteration++
                            usersSelectedOptions.add(items[position])

                            // User has selected every possible variant
                            if (numberOfIterations == iteration) {
                                dialog.dismiss()
                                var selectedText = ""
                                for (i in 0 until usersSelectedOptions.size) {
                                    val option = usersSelectedOptions[i]
                                    selectedText = "$selectedText / $option"
                                }
                                variant_title.text = selectedText
                                selectionAccomplished = true
                            } else {
                                // Change dialog title and inflate new list items for next variant selection
                                val title = productOptions.getJSONObject(iteration).getString("name")
                                dialogTitle.text = title
                                items.clear()
                                val innerOptions = productOptions.getJSONObject(iteration).getJSONArray("values")
                                for (i in 0 until innerOptions.length()) {
                                    items.add(innerOptions.getString(i))
                                }

                                dataAdapter.notifyDataSetChanged()
                            }
                    }


                    // Dialog view does not play well with full-screen activities, has to re-set view
                    val dialogWindow = dialog.window

                    if (dialogWindow != null) {
                        // Set the dialog to not focusable (makes navigation ignore us adding the window)
                        dialogWindow.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)

                        dialog.show()

                        // Set the dialog to immersive
                        dialogWindow.decorView.systemUiVisibility = this.window.decorView.systemUiVisibility

                        // Clear the not focusable flag from the window
                        dialogWindow.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
                        dialog.setOnDismissListener {
                            hideNavs()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            if (MonetizrSdk.debuggable) {
                Log.i("MonetizrSDK", "Product information was not received")
                e.printStackTrace()
            }
        }

        close_button.setOnClickListener{
            this.finish()
        }

        /**
         * Checkout button
         */
        checkout_button.setOnClickListener{
            checkout()
        }

        /**
         * Google Pay checkout button, if payment available
         */
        button_google_pay.setOnClickListener{
            checkoutWithPaymentTokenButtonClick()
        }
    }

    /**
     * Recalculate element sizes on orientation change
     */
    override fun onResume() {
        super.onResume()

        // Trigger the initial navigation hiding
        hideNavs()

        // Recalculate view sizes on resume
        val orientation = resources.configuration.orientation
        calculateElementSize(orientation)
    }

    override fun onStart() {
        super.onStart()
        recyclerView.adapter = imageGalleryAdapter
    }

    override fun onStop() {
        super.onStop()

        // Log milliseconds while product has been visible
        val productVisibleTime = System.currentTimeMillis() - activityLaunched
        Telemetrics.impressionvisible(productVisibleTime, productTag)

        // Use did not make any interaction
        if (!userMadeInteraction) {
            Telemetrics.dismiss(productTag)
        }
    }

    // Method invoked on screen orientation change, recalculate positions of elements
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        calculateElementSize(newConfig.orientation)
    }

    /**
     * Data returned from shipping address input to use for checkout process
     */
    override fun updateResult(address: JSONObject) {
        shippingAddress = address
    }

    // Send a checkout request to backend and show shop on return
    private fun checkout(continueWithPayment: Boolean = false) {
        val isReadyToPayJson = PaymentsUtil.isReadyToPayRequest() ?: return
        val request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString()) ?: return


        // The call to isReadyToPay is asynchronous and returns a Task. Need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        val task = mPaymentsClient.isReadyToPay(request)
        task.addOnCompleteListener { completedTask ->
            try {
                completedTask.getResult(ApiException::class.java)?.let(::setGooglePayAvailable)
            } catch (exception: ApiException) {
                // Process error
                Log.i("MonetizrSDK", "Error on payment processing" + exception.toString())
            }
        }


        // Show modal bottom sheet for shipping address input
        val modalBottomSheet = BottomModal()
        modalBottomSheet.isCancelable = false
        modalBottomSheet.show(supportFragmentManager, BottomModal.TAG)
        supportFragmentManager.executePendingTransactions()

        // Shipping address modal dismissed, receive data and submit checkout
        modalBottomSheet.dialog.setOnDismissListener {
            userMadeInteraction = true
            var variantForCheckout: JSONObject?

            // Really dismiss this modal as it shows up on back pressed
            supportFragmentManager.findFragmentByTag(BottomModal.TAG)?.let {
                (it as BottomModal).dismiss()
            }

            // User just pushes checkout button, it means that initial
            // variant has to be chosen to finish buying
            variantForCheckout = initialVariant

            // If this value is true, then user has completed selection and
            // selected variant can be found by selected options from variants array
            if (selectionAccomplished) {
                variantForCheckout = searchSelectedVariant()
            }

            // Make a checkout request
            if (variantForCheckout != null) {
                val language = Locale.getDefault().displayLanguage
                val jsonBody = JSONObject()
                val variantId = variantForCheckout.getString("id")

                jsonBody.put("product_handle", productTag)
                jsonBody.put("variantId", variantId)
                jsonBody.put("language", language)
                jsonBody.put("quantity", 1)
                jsonBody.put("shippingAddress", shippingAddress.toString())

                val url = MonetizrSdk.apiAddress + "products/checkout"

                if (MonetizrSdk.firstCheckout) {
                    Telemetrics.firstimpressioncheckout()
                    MonetizrSdk.firstCheckout = false
                }

                val jsonObjectRequest = object : JsonObjectRequest(
                    Method.POST, url, jsonBody,
                    Response.Listener { response ->
                        if (continueWithPayment == true) {
                            checkoutInfo = response
                            requestPayment()
                       } else {
                            // Successful response, now show shops window
                            showProductView(response)
                        }
                    },
                    Response.ErrorListener { error ->
                        if (MonetizrSdk.debuggable) {
                            // Die silently, so it does not provide any bad experience
                            Log.i("MonetizrSDK", "Received API error " + error.networkResponse.data.toString())
                            error.printStackTrace()
                        }
                    }) {

                    // Override headers to pass authorization
                    override fun getHeaders(): MutableMap<String, String> {

                        val header = mutableMapOf<String, String>()
                        header["Authorization"] = "Bearer " + MonetizrSdk.apikey
                        return header
                    }
                }

                // Access the RequestQueue through singleton class.
                SingletonRequest.getInstance(this).addToRequestQueue(jsonObjectRequest)
            } else {
                // Show a solid information that chosen variant is not available
                Toast.makeText(this, R.string.variant_not_found, Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Make a checkout with payment token available
     */
    private fun checkoutWithPaymentTokenButtonClick() {

        // Request for checkout token
        checkout(true)
    }

    /**
     * Show product in browser to allow to accomplish purchase action
     */
    private fun showProductView(checkoutInfo: JSONObject) {
        val checkoutErrors = checkoutInfo.getJSONObject("data").getJSONObject("checkoutCreate").getJSONArray("checkoutUserErrors")
        val checkoutRedirect = checkoutInfo.getJSONObject("data").getJSONObject("checkoutCreate").getJSONObject("checkout").getString("webUrl")

        if (checkoutErrors.length() <= 0) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(checkoutRedirect)))
        } else {
            Toast.makeText(this, R.string.error_while_checkout, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Searching for selected variant in request results
     */
    private fun searchSelectedVariant(): JSONObject? {
        var selectedVariant: JSONObject? = null

        for (i in 0 until variants.length()) {
            val selectedOptions = variants.getJSONObject(i).getJSONObject("node").getJSONArray("selectedOptions")
            var numberOfMatchingOptions = 0

            // Iteration in options to search for the selected ones
            for (j in 0 until selectedOptions.length()) {
                val option = selectedOptions.getJSONObject(j).getString("value")

                // Search for option inside users choice
                if (usersSelectedOptions.indexOf(option) != -1) {
                    numberOfMatchingOptions++
                }
            }

            if (numberOfMatchingOptions == usersSelectedOptions.size) {
                // This is the variant user has chosen, stop and exit from loop
                selectedVariant = variants.getJSONObject(i).getJSONObject("node")
                break
            }
        }
        return selectedVariant
    }

    // Change sizes of views when orientation changes
    private fun calculateElementSize(orientation: Int) {
        val display = this.windowManager.defaultDisplay
        val realMetrics = DisplayMetrics()
        display.getRealMetrics(realMetrics)
        val width = realMetrics.widthPixels
        val height = realMetrics.heightPixels
        val topView = viewTop.layoutParams
        val bottomView = viewBottom.layoutParams

        if (topView is FlexboxLayout.LayoutParams && bottomView is FlexboxLayout.LayoutParams) {

            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                topView.height = height
                topView.maxHeight = height
                topView.width = width /2
                topView.maxWidth = width /2
                bottomView.height = height
                bottomView.maxHeight = height
                bottomView.width = width / 2
                bottomView.maxWidth = width /2
            }

            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                topView.height = height /2
                topView.maxHeight = height /2
                topView.width = width
                topView.maxWidth = width
                bottomView.height = height / 2
                bottomView.maxHeight = height /2
                bottomView.width = width
                bottomView.maxWidth = width
            }
        }
    }

    /**
     * Hide navbar and button bars
     */
    private fun hideNavs() {
        product_content.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    /**
     * Custom image adapter for recycler view
     */
    private inner class ImageGalleryAdapter(val context: Context, val productPhotos: ArrayList<ProductPhoto>)
        : androidx.recyclerview.widget.RecyclerView.Adapter<ImageGalleryAdapter.MyViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val context = parent.context
            val inflater = LayoutInflater.from(context)
            val photoView = inflater.inflate(R.layout.image, parent, false)
            return MyViewHolder(photoView)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val productPhoto = productPhotos[position]
            val imageView = holder.photoImageView

            val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()

            val circularProgressDrawable = androidx.swiperefreshlayout.widget.CircularProgressDrawable(context)
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.start()

            Glide.with(context)
                .load(productPhoto.url)
                .transition(withCrossFade(factory))
                .placeholder(circularProgressDrawable)
                .error(R.drawable.error)
                .fitCenter()
                .into(imageView)
        }

        override fun getItemCount(): Int {
            return productPhotos.size
        }

        inner class MyViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView), View.OnClickListener {

            var photoImageView: ImageView = itemView.findViewById(R.id.iv_photo)

            init {
                itemView.setOnClickListener(this)
            }

            override fun onClick(view: View) {
                val position = adapterPosition
                if (position != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {}
            }
        }
    }

    /**
     * Show google pay button if service available
     */
    fun possiblyShowGooglePayButton() {
        val isReadyToPayJson = PaymentsUtil.isReadyToPayRequest() ?: return
        val request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString()) ?: return

//         The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
//         OnCompleteListener to be triggered when the result of the call is known.
        val task = mPaymentsClient.isReadyToPay(request)
        task.addOnCompleteListener { completedTask ->
            try {
                completedTask.getResult(ApiException::class.java)?.let(::setGooglePayAvailable)
            } catch (exception: ApiException) {
                // Process error
                Log.i("MonetizrSDK", "this is the stuff" + exception.toString())
            }
        }
    }

    /**
     * If isReadyToPay returned `true`, show the button and hide the "checking" text. Otherwise,
     * notify the user that Google Pay is not available. Please adjust to fit in with your current
     * user flow. You are not required to explicitly let the user know if isReadyToPay returns `false`.
     *
     * @param available isReadyToPay API response.
     */
    private fun setGooglePayAvailable(available: Boolean) {
        if (available) {
            button_google_pay.visibility = View.VISIBLE
        } else {

            Toast.makeText(this, "Pay not available", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Create payment request
     */
    private fun requestPayment() {

        // Disables the button to prevent multiple clicks.
        button_google_pay.isClickable = false

        // The price provided to the API include taxes and shipping costs.
        val totalPrice = checkoutInfo.getJSONObject("data").getJSONObject("checkoutCreate").getJSONObject("checkout").getJSONObject("totalPriceV2").getString("amount")

        val paymentDataRequestJson = PaymentsUtil.getPaymentDataRequest(totalPrice)
        if (paymentDataRequestJson == null) {
            Log.e("RequestPayment", "Can't fetch payment data request")
            return
        }
        val request = PaymentDataRequest.fromJson(paymentDataRequestJson.toString())

        // Since loadPaymentData may show the UI asking the user to select a payment method, we use
        // AutoResolveHelper to wait for the user interacting with it. Once completed,
        // onActivityResult will be called with the result.
        if (request != null) {
            AutoResolveHelper.resolveTask(
                mPaymentsClient.loadPaymentData(request), this, LOAD_PAYMENT_DATA_REQUEST_CODE)
        }
    }

    /**
     * Handle a resolved activity from the Google Pay payment sheet.
     *
     * @param requestCode Request code originally supplied to AutoResolveHelper in requestPayment().
     * @param resultCode Result code returned by the Google Pay API.
     * @param data Intent from the Google Pay API containing payment or error data.
     * @see [Getting a result
     * from an Activity](https://developer.android.com/training/basics/intents/result)
     */
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            // value passed in AutoResolveHelper
            LOAD_PAYMENT_DATA_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK ->
                        data?.let { intent ->
                            PaymentData.getFromIntent(intent)?.let(::handlePaymentSuccess)
                        }
                    Activity.RESULT_CANCELED -> {
                        // Nothing to do here normally - the user simply cancelled without selecting a
                        // payment method.
                    }

                    AutoResolveHelper.RESULT_ERROR -> {
                        AutoResolveHelper.getStatusFromIntent(data)?.let {
                            handleError(it.statusCode)
                        }
                    }
                }
                // Re-enables the Google Pay payment button.
                button_google_pay.isClickable = true
            }
        }
    }

    /**
     * PaymentData response object contains the payment information, as well as any additional
     * requested information, such as billing and shipping address.
     *
     * @param paymentData A response object returned by Google after a payer approves payment.
     * @see [Payment
     * Data](https://developers.google.com/pay/api/android/reference/object.PaymentData)
     */
    private fun handlePaymentSuccess(paymentData: PaymentData) {
        val paymentInformation = paymentData.toJson() ?: return

        try {
            // Token will be null if PaymentDataRequest was not constructed using fromJson(String).
            val paymentMethodData = JSONObject(paymentInformation).getJSONObject("paymentMethodData")
            Log.i("MonetizrSDK", paymentMethodData.toString())

            Log.i("MonetizrSDK", "this is what comes out: " + paymentMethodData
                .getJSONObject("tokenizationData")
                .getString("token"))

            // If the gateway is set to "example", no payment information is returned - instead, the
            // token will only consist of "examplePaymentMethodToken".
//            if (paymentMethodData
//                    .getJSONObject("tokenizationData")
//                    .getString("type") == "PAYMENT_GATEWAY" && paymentMethodData
//                    .getJSONObject("tokenizationData")
//                    .getString("token") == "examplePaymentMethodToken") {

//                AlertDialog.Builder(this)
//                    .setTitle("Warning")
//                    .setMessage("Gateway name set to \"example\" - please modify " +
//                            "Constants.java and replace it with your own gateway.")
//                    .setPositiveButton("OK", null)
//                    .create()
//                    .show()
//            }

                val billingAddress = paymentMethodData.getJSONObject("info")
                    .getJSONObject("billingAddress")
               // Log.i("MonetizrSDK", billingName)

//                val jsonBody = JSONObject()
//
//                jsonBody.put("checkoutId", productTag)
//                jsonBody.put("product_handle", productTag)
//                jsonBody.put("billingAddress", billingAddress.toString())
//                jsonBody.put("idempotencyKey", "")
//                jsonBody.put("paymentAmount", 1)
//                jsonBody.put("paymentData", shippingAddress.toString())
//                jsonBody.put("test", true)
//                jsonBody.put("type", "google_pay")
//
//                val url = MonetizrSdk.apiAddress + "products/checkoutwithpayment"
//
//                if (MonetizrSdk.firstCheckout) {
//                    Telemetrics.firstimpressioncheckout()
//                    MonetizrSdk.firstCheckout = false
//                }
//
//                val jsonObjectRequest = object : JsonObjectRequest(
//                    Method.POST, url, jsonBody,
//                    Response.Listener { response ->
//                        // Nothing here, close view
//                        Toast.makeText(this, "payment success", Toast.LENGTH_LONG).show()
////                        this.finish()
//                    },
//                    Response.ErrorListener { error ->
//                        if (MonetizrSdk.debuggable) {
//                            // Die silently, so it does not provide any bad experience
//                            Log.i("MonetizrSDK", "Received API error " + error.networkResponse.data.toString())
//                            error.printStackTrace()
//                        }
//                    }) {
//
//                    // Override headers to pass authorization
//                    override fun getHeaders(): MutableMap<String, String> {
//
//                        val header = mutableMapOf<String, String>()
//                        header["Authorization"] = "Bearer " + MonetizrSdk.apikey
//                        return header
//                    }
//                }
//
//                // Access the RequestQueue through singleton class.
//                SingletonRequest.getInstance(this).addToRequestQueue(jsonObjectRequest)

//            Toast.makeText(this, "This is some kind of text here", Toast.LENGTH_LONG).show()
            // Logging token string.

            // Complete payment with request to shopify complete with checkout

        } catch (e: JSONException) {
            Log.e("handlePaymentSuccess", "Error: " + e.toString())
        }

    }

    /**
     * At this stage, the user has already seen a popup informing them an error occurred. Normally,
     * only logging is required.
     *
     * @param statusCode will hold the value of any constant from CommonStatusCode or one of the
     * WalletConstants.ERROR_CODE_* constants.
     * @see [
     * Wallet Constants Library](https://developers.google.com/android/reference/com/google/android/gms/wallet/WalletConstants.constant-summary)
     */
    private fun handleError(statusCode: Int) {
        Log.w("loadPaymentData failed", String.format("Error code: %d", statusCode))
    }
}
