package io.monetizr.monetizrsdk


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.support.v4.widget.CircularProgressDrawable
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.google.android.flexbox.FlexboxLayout
import kotlinx.android.synthetic.main.activity_product.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


/**
 * Full-screen activity that shows product information
 */
class ProductActivity : AppCompatActivity() {

    // Recycler view - view that draws images
    private lateinit var recyclerView: RecyclerView

    // Adapter that takes care of images inside recycler view
    private lateinit var imageGalleryAdapter: ImageGalleryAdapter

    // Options inside variant selection, as a llist view that allows items to be changed
    private lateinit var optionListView: ListView

    // The first product variant that is being chosen
    private lateinit var initialVariant: JSONObject

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
            val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
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
            Log.i("MonetizrSDK", "Product information was not received")
            e.printStackTrace()
        }

        close_button.setOnClickListener{
            this.finish()
        }

        checkout_button.setOnClickListener{
            checkout()
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

    // Send a checkout request to backend and show shop on return
    private fun checkout() {
        userMadeInteraction = true
        var variantForCheckout: JSONObject?

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
            val url = MonetizrSdk.apiAddress + "products/checkout"

            if (MonetizrSdk.firstCheckout) {
                Telemetrics.firstimpressioncheckout()
                MonetizrSdk.firstCheckout = false
            }

            val jsonObjectRequest = object : JsonObjectRequest(
                Method.POST, url, jsonBody,
                Response.Listener { response ->
                    // Successful response, now show shops window
                    showProductView(response)
                },
                Response.ErrorListener { error ->
                    // Die silently, so it does not provide any bad experience
                    Log.i("MonetizrSDK", "Received API error $error")
                    error.printStackTrace()
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

    /**
     * Show product backet in browser to allow to accomplish purchase action
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
        : RecyclerView.Adapter<ImageGalleryAdapter.MyViewHolder>() {

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

            val circularProgressDrawable = CircularProgressDrawable(context)
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

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

            var photoImageView: ImageView = itemView.findViewById(R.id.iv_photo)

            init {
                itemView.setOnClickListener(this)
            }

            override fun onClick(view: View) {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {}
            }
        }
    }
}
