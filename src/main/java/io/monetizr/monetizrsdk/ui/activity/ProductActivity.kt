package io.monetizr.monetizrsdk.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentsClient
import io.monetizr.monetizrsdk.ClearedService
import io.monetizr.monetizrsdk.R
import io.monetizr.monetizrsdk.api.Telemetrics
import io.monetizr.monetizrsdk.dto.HierarchyVariant
import io.monetizr.monetizrsdk.dto.PaymentInfo
import io.monetizr.monetizrsdk.dto.Product
import io.monetizr.monetizrsdk.dto.ShippingRate
import io.monetizr.monetizrsdk.misc.Parameters
import io.monetizr.monetizrsdk.payment.PaymentsUtil
import io.monetizr.monetizrsdk.ui.adapter.ImageGalleryAdapter
import io.monetizr.monetizrsdk.ui.adapter.ItemIndicator
import io.monetizr.monetizrsdk.ui.adapter.ItemSnapHelper
import io.monetizr.monetizrsdk.ui.dialog.OptionsDialog
import io.monetizr.monetizrsdk.ui.dialog.OptionsDialogListener
import io.monetizr.monetizrsdk.ui.dialog.ShippingRateDialogListener
import kotlinx.android.synthetic.main.activity_product.*
import kotlinx.android.synthetic.main.item_shipping_rate.*
import org.json.JSONObject

class ProductActivity : AppCompatActivity(), ShippingRateDialogListener, OptionsDialogListener {
    private var userMadeInteraction: Boolean = false
    private var activityLaunchedStamp: Long = 0
    private lateinit var paymentsClient: PaymentsClient
    private val selectedOptions: ArrayList<HierarchyVariant> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product)
        activityLaunchedStamp = System.currentTimeMillis()
        paymentsClient = PaymentsUtil.createPaymentsClient(this)
        startService(Intent(baseContext, ClearedService::class.java))

        val json = intent!!.getStringExtra(Parameters.PRODUCT_JSON)!!
        val jsonObject = JSONObject(json)
        val product = Product(jsonObject)

        initImageAdapter(product.images)
        initGooglePayButton()

        initCheckoutTitle(product)
        initProductPriceTitle(product)
        initProductVariantsTitle(product)

        productTitleView.text = product.title
        productDescriptionView.text = product.descriptionIos

        closeButtonView.setOnClickListener { finish() }
        payButtonView.setOnClickListener { checkoutWithPaymentTokenButtonClick() }
        variantContainerView.setOnClickListener { showOptionDialog(json) }

        hideNavigationBar()
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

    override fun onOptionsSelect(options: ArrayList<HierarchyVariant>) {
        hideNavigationBar()
        selectedOptions.clear()
        selectedOptions.addAll(options)
        initProductVariantsValues(options)
    }

    override fun onShippingRateSelect(shippingRate: ShippingRate) {

    }

    private fun checkout(proceedWithPayment: Boolean = false) {

    }

    private fun checkoutWithPaymentTokenButtonClick() {

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

    //region ui

    private fun hideNavigationBar() {
        rootView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    private fun initGooglePayButton() {
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


    private fun initImageAdapter(photos: ArrayList<String>) {
        val imageGalleryAdapter = ImageGalleryAdapter(this, photos)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        productImagesView.setHasFixedSize(true)
        productImagesView.layoutManager = layoutManager
        ItemSnapHelper().attachToRecyclerView(productImagesView)
        productImagesView.addItemDecoration(ItemIndicator())
        productImagesView.adapter = imageGalleryAdapter
    }

    private fun initProductPriceTitle(product: Product) {
        val variant = product.getFirstVariant()
        if (variant != null) {
            productPriceView.text = variant.priceV2.formatString()
        }
    }

    private fun initProductVariantsTitle(product: Product) {
        variantContainerView.isEnabled = product.variants.size >= 1

        if (product.variants.isEmpty() == false) {
            val variant = product.getFirstVariant()!!

            if (variant.selectedOptions.size > 0) {
                option1NameView.text = variant.selectedOptions[0].name
                option1ValueView.text = variant.selectedOptions[0].value

                option1NameView.visibility = View.VISIBLE
                option1ValueView.visibility = View.VISIBLE
            } else {
                option1NameView.visibility = View.GONE
                option1ValueView.visibility = View.GONE
            }

            if (variant.selectedOptions.size > 1) {
                option2NameView.text = variant.selectedOptions[1].name
                option2ValueView.text = variant.selectedOptions[1].value

                option2NameView.visibility = View.VISIBLE
                option2ValueView.visibility = View.VISIBLE
            } else {
                option2NameView.visibility = View.GONE
                option2ValueView.visibility = View.GONE
            }

            if (variant.selectedOptions.size > 2) {
                option3NameView.text = variant.selectedOptions[2].name
                option3ValueView.text = variant.selectedOptions[2].value

                option3NameView.visibility = View.VISIBLE
                option3ValueView.visibility = View.VISIBLE
            } else {
                option3NameView.visibility = View.GONE
                option3ValueView.visibility = View.GONE
            }
        }
    }

    private fun initProductVariantsValues(options: ArrayList<HierarchyVariant>) {
        if (options.isEmpty() == false) {

            if (options.size > 0) {
                option1ValueView.text = options[0].id
            }

            if (options.size > 1) {
                option2ValueView.text = options[1].id
            }

            if (options.size > 2) {
                option3ValueView.text = options[2].id
            }
        }
    }

    private fun initCheckoutTitle(product: Product) {
        if (product.buttonTitle != null && product.buttonTitle.isNotEmpty()) {
            checkoutButtonView.text = product.buttonTitle
        }
    }

    private fun showOptionDialog(product: String) {
        val fragment = OptionsDialog.newInstance(product)
        fragment.show(supportFragmentManager, "Option")
    }

    //endregion

    companion object {
        var firstCheckout: Boolean = true
        const val LOAD_PAYMENT_DATA_REQUEST_CODE = 991

        fun start(context: Context, productJson: String, productTag: String) {
            val starter = Intent(context, ProductActivity::class.java)
            starter.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            starter.putExtra(Parameters.PRODUCT_JSON, productJson)
            starter.putExtra(Parameters.PRODUCT_TAG, productTag)
            context.startActivity(starter)
        }
    }
}
