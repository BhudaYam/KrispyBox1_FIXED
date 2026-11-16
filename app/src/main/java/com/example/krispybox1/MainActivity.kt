package com.example.krispybox1

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var contentContainer: FrameLayout
    private lateinit var bottomNavigation: BottomNavigationView

    private var isLoggedIn = false

    private val cartItems = mutableListOf<String>()

    private var currentScreen = SCREEN_HOME
    private var customiseReturnScreen = SCREEN_MENU

    private var navigationReady = false
    private var suppressNavigationCallback = false

    companion object {

        const val SCREEN_HOME = "home"
        const val SCREEN_MENU = "menu"
        const val SCREEN_CART = "cart"
        const val SCREEN_ORDERS = "orders"
        const val SCREEN_PROFILE = "profile"
        const val SCREEN_SETTINGS = "settings"
        const val SCREEN_CUSTOMISE = "customise"

        private const val PREFS_NAME = "krispybox_prefs"

        private const val KEY_LOGGED_IN = "logged_in"
        private const val KEY_NAME = "user_name"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_CURRENT_SCREEN = "current_screen"

        private const val KEY_CART = "cart_items"
        private const val KEY_ORDERS = "orders"

        private const val FREE_DELIVERY_FROM = 50.0
        private const val DELIVERY_FEE = 15.0
    }

    // =========================================================
    // ON CREATE
    // =========================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        contentContainer = findViewById(R.id.content_container)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        val prefs = getSharedPreferences(
            PREFS_NAME,
            MODE_PRIVATE
        )

        isLoggedIn = prefs.getBoolean(
            KEY_LOGGED_IN,
            false
        )

        loadCart()

        setupNavigation()
        setupBackButton()

        if (!isLoggedIn) {

            showLogin()

        } else {

            val savedScreen = prefs.getString(
                KEY_CURRENT_SCREEN,
                SCREEN_HOME
            ) ?: SCREEN_HOME

            when (savedScreen) {

                SCREEN_MENU -> showScreen(SCREEN_MENU)

                SCREEN_CART -> showScreen(SCREEN_CART)

                SCREEN_ORDERS -> showScreen(SCREEN_ORDERS)

                SCREEN_PROFILE -> showScreen(SCREEN_PROFILE)

                SCREEN_SETTINGS -> showScreen(SCREEN_SETTINGS)

                else -> showScreen(SCREEN_HOME)
            }
        }
    }

    // =========================================================
    // PREFERENCES
    // =========================================================

    private fun getPrefs() =
        getSharedPreferences(
            PREFS_NAME,
            MODE_PRIVATE
        )

    private fun saveCurrentScreen(screen: String) {

        getPrefs()
            .edit()
            .putString(
                KEY_CURRENT_SCREEN,
                screen
            )
            .apply()
    }

    // =========================================================
    // NAVIGATION
    // =========================================================

    private fun setupNavigation() {

        navigationReady = false

        bottomNavigation.setOnItemSelectedListener { item ->

            if (suppressNavigationCallback) {
                return@setOnItemSelectedListener true
            }

            if (!isLoggedIn) {

                showLogin()

                return@setOnItemSelectedListener false
            }

            when (item.itemId) {

                R.id.nav_home -> {

                    showScreen(SCREEN_HOME)

                    true
                }

                R.id.nav_menu -> {

                    showScreen(SCREEN_MENU)

                    true
                }

                R.id.nav_orders -> {

                    showScreen(SCREEN_ORDERS)

                    true
                }

                R.id.nav_profile -> {

                    showScreen(SCREEN_PROFILE)

                    true
                }

                else -> false
            }
        }

        navigationReady = true
    }

    private fun selectBottomItem(id: Int) {

        if (!navigationReady) {
            return
        }

        if (bottomNavigation.selectedItemId == id) {
            return
        }

        suppressNavigationCallback = true

        try {

            bottomNavigation.selectedItemId = id

        } finally {

            suppressNavigationCallback = false
        }
    }

    private fun showScreen(screen: String) {

        if (!isLoggedIn) {

            showLogin()

            return
        }

        currentScreen = screen

        saveCurrentScreen(screen)

        when (screen) {

            SCREEN_HOME -> {

                selectBottomItem(R.id.nav_home)

                showHome()
            }

            SCREEN_MENU -> {

                selectBottomItem(R.id.nav_menu)

                showMenu()
            }

            SCREEN_CART -> {

                showCart()
            }

            SCREEN_ORDERS -> {

                selectBottomItem(R.id.nav_orders)

                showOrders()
            }

            SCREEN_PROFILE -> {

                selectBottomItem(R.id.nav_profile)

                showProfile()
            }

            SCREEN_SETTINGS -> {

                showSettings()
            }

            SCREEN_CUSTOMISE -> {

                showCustomise()
            }
        }
    }

    // =========================================================
    // LOGIN
    // =========================================================

    private fun showLogin() {

        clearContent()

        bottomNavigation.visibility = View.GONE

        val view = layoutInflater.inflate(
            R.layout.layout_login,
            contentContainer,
            false
        )

        contentContainer.addView(view)

        val loginButton =
            view.findViewById<View>(
                R.id.btn_login_submit
            )

        val emailInput =
            view.findViewById<EditText>(
                R.id.et_login_email
            )

        loginButton.setOnClickListener {

            val enteredEmail =
                emailInput.text
                    .toString()
                    .trim()

            val email =
                if (enteredEmail.isEmpty()) {
                    "customer@krispybox.com"
                } else {
                    enteredEmail
                }

            val prefs = getPrefs()

            prefs.edit()
                .putBoolean(
                    KEY_LOGGED_IN,
                    true
                )
                .putString(
                    KEY_NAME,
                    "KrispyBox Customer"
                )
                .putString(
                    KEY_EMAIL,
                    email
                )
                .putString(
                    KEY_CURRENT_SCREEN,
                    SCREEN_HOME
                )
                .apply()

            isLoggedIn = true

            bottomNavigation.visibility =
                View.VISIBLE

            showScreen(SCREEN_HOME)
        }
    }

    // =========================================================
    // HOME
    // =========================================================

    private fun showHome() {

        clearContent()

        val view = layoutInflater.inflate(
            R.layout.layout_home,
            contentContainer,
            false
        )

        contentContainer.addView(view)

        view.findViewById<View>(
            R.id.btn_order_now
        )?.setOnClickListener {

            showScreen(SCREEN_MENU)
        }
    }

    // =========================================================
    // MENU
    // =========================================================

    private fun showMenu() {

        clearContent()

        val view = layoutInflater.inflate(
            R.layout.layout_menu,
            contentContainer,
            false
        )

        contentContainer.addView(view)

        // BURGER
        view.findViewById<View>(
            R.id.btn_add_burger
        ).setOnClickListener {

            openCustomise(
                "Burger Combo",
                65.0
            )
        }

        // WINGS
        view.findViewById<View>(
            R.id.btn_add_wings
        ).setOnClickListener {

            openCustomise(
                "Wings",
                50.0
            )
        }

        // CHICKEN BOX
        view.findViewById<View>(
            R.id.btn_add_box
        ).setOnClickListener {

            openCustomise(
                "Chicken Box",
                85.0
            )
        }

        // LOADED FRIES
        view.findViewById<View>(
            R.id.btn_add_fries
        ).setOnClickListener {

            openCustomise(
                "Loaded Fries",
                45.0
            )
        }

        // VIEW CART
        view.findViewById<View>(
            R.id.btn_go_to_cart
        ).setOnClickListener {

            showScreen(SCREEN_CART)
        }
    }

    // =========================================================
    // CUSTOMISATION
    // =========================================================

    private fun openCustomise(
        itemName: String,
        price: Double
    ) {

        customiseReturnScreen = SCREEN_MENU

        showCustomiseScreen(
            itemName,
            price
        )
    }

    private fun showCustomise() {

        showScreen(SCREEN_MENU)
    }

    private fun showCustomiseScreen(
        itemName: String,
        price: Double,
        existingCustomisation: String = ""
    ) {

        clearContent()

        val view = layoutInflater.inflate(
            R.layout.layout_customise,
            contentContainer,
            false
        )

        contentContainer.addView(view)

        val pickles =
            view.findViewById<CheckBox>(
                R.id.cb_no_pickles
            )

        val tomatoes =
            view.findViewById<CheckBox>(
                R.id.cb_no_tomatoes
            )

        val onions =
            view.findViewById<CheckBox>(
                R.id.cb_no_onions
            )

        val lettuce =
            view.findViewById<CheckBox>(
                R.id.cb_no_lettuce
            )

        val sauce =
            view.findViewById<CheckBox>(
                R.id.cb_no_sauce
            )

        val instructions =
            view.findViewById<EditText>(
                R.id.et_special_instructions
            )

        val rememberPreferences =
            view.findViewById<CheckBox>(
                R.id.cb_remember_preferences
            )

        // Load saved preferences
        val prefs = getPrefs()

        if (existingCustomisation.isEmpty()) {

            pickles.isChecked =
                prefs.getBoolean(
                    "no_pickles",
                    false
                )

            tomatoes.isChecked =
                prefs.getBoolean(
                    "no_tomatoes",
                    false
                )

            onions.isChecked =
                prefs.getBoolean(
                    "no_onions",
                    false
                )

            lettuce.isChecked =
                prefs.getBoolean(
                    "no_lettuce",
                    false
                )

            sauce.isChecked =
                prefs.getBoolean(
                    "no_sauce",
                    false
                )

            rememberPreferences.isChecked =
                prefs.getBoolean(
                    "remember_preferences",
                    false
                )
        }

        view.findViewById<View>(
            R.id.btn_add_customised
        ).setOnClickListener {

            val customisation =
                mutableListOf<String>()

            if (pickles.isChecked) {
                customisation.add("No pickles")
            }

            if (tomatoes.isChecked) {
                customisation.add("No tomatoes")
            }

            if (onions.isChecked) {
                customisation.add("No onions")
            }

            if (lettuce.isChecked) {
                customisation.add("No lettuce")
            }

            if (sauce.isChecked) {
                customisation.add("No sauce")
            }

            val specialText =
                instructions.text
                    .toString()
                    .trim()

            if (specialText.isNotEmpty()) {

                customisation.add(
                    "Note: $specialText"
                )
            }

            val customText =
                customisation.joinToString(
                    ", "
                )

            val cartItem =
                if (customText.isEmpty()) {

                    "$itemName|$price"

                } else {

                    "$itemName|$price|$customText"
                }

            cartItems.add(cartItem)

            saveCart()

            if (rememberPreferences.isChecked) {

                prefs.edit()
                    .putBoolean(
                        "remember_preferences",
                        true
                    )
                    .putBoolean(
                        "no_pickles",
                        pickles.isChecked
                    )
                    .putBoolean(
                        "no_tomatoes",
                        tomatoes.isChecked
                    )
                    .putBoolean(
                        "no_onions",
                        onions.isChecked
                    )
                    .putBoolean(
                        "no_lettuce",
                        lettuce.isChecked
                    )
                    .putBoolean(
                        "no_sauce",
                        sauce.isChecked
                    )
                    .apply()

            } else {

                prefs.edit()
                    .putBoolean(
                        "remember_preferences",
                        false
                    )
                    .apply()
            }

            Toast.makeText(
                this,
                "$itemName added to cart!",
                Toast.LENGTH_SHORT
            ).show()

            showScreen(SCREEN_CART)
        }
    }

    // =========================================================
    // CART
    // =========================================================

    private fun showCart() {

        clearContent()

        val view = layoutInflater.inflate(
            R.layout.layout_cart,
            contentContainer,
            false
        )

        contentContainer.addView(view)

        val itemsContainer =
            view.findViewById<LinearLayout>(
                R.id.cart_items_container
            )

        val subtotalText =
            view.findViewById<TextView>(
                R.id.tv_cart_subtotal
            )

        val totalText =
            view.findViewById<TextView>(
                R.id.tv_cart_total
            )

        val checkoutButton =
            view.findViewById<View>(
                R.id.btn_checkout
            )

        itemsContainer.removeAllViews()

        var subtotal = 0.0

        for (index in cartItems.indices) {

            val item =
                cartItems[index]

            val parts =
                item.split("|")

            val itemName =
                parts.getOrNull(0)
                    ?: "KrispyBox Item"

            val price =
                parts.getOrNull(1)
                    ?.toDoubleOrNull()
                    ?: 0.0

            val customisation =
                if (parts.size >= 3) {
                    parts.drop(2)
                        .joinToString("|")
                } else {
                    ""
                }

            subtotal += price

            val itemView =
                layoutInflater.inflate(
                    R.layout.item_cart,
                    itemsContainer,
                    false
                )

            val nameText =
                itemView.findViewById<TextView>(
                    R.id.tv_cart_item_name
                )

            val priceText =
                itemView.findViewById<TextView>(
                    R.id.tv_cart_item_price
                )

            val quantityText =
                itemView.findViewById<TextView>(
                    R.id.tv_cart_item_quantity
                )

            val customisationText =
                itemView.findViewById<TextView>(
                    R.id.tv_cart_customisation
                )

            val minusButton =
                itemView.findViewById<View>(
                    R.id.btn_cart_minus
                )

            val plusButton =
                itemView.findViewById<View>(
                    R.id.btn_cart_plus
                )

            val editButton =
                itemView.findViewById<View>(
                    R.id.btn_cart_edit
                )

            val removeButton =
                itemView.findViewById<View>(
                    R.id.btn_cart_remove
                )

            nameText.text = itemName

            priceText.text =
                String.format(
                    Locale.getDefault(),
                    "R%.2f",
                    price
                )

            quantityText.text = "1"

            if (customisation.isEmpty()) {

                customisationText.visibility =
                    View.GONE

            } else {

                customisationText.visibility =
                    View.VISIBLE

                customisationText.text =
                    customisation
            }

            // MINUS
            minusButton.setOnClickListener {

                if (index < cartItems.size) {

                    cartItems.removeAt(index)

                    saveCart()

                    showCart()
                }
            }

            // PLUS
            plusButton.setOnClickListener {

                if (index < cartItems.size) {

                    cartItems.add(
                        cartItems[index]
                    )

                    saveCart()

                    showCart()
                }
            }

            // EDIT
            editButton.setOnClickListener {

                showEditCustomisationDialog(
                    index
                )
            }

            // REMOVE
            removeButton.setOnClickListener {

                if (index < cartItems.size) {

                    cartItems.removeAt(index)

                    saveCart()

                    showCart()
                }
            }

            itemsContainer.addView(
                itemView
            )
        }

        val delivery =
            if (
                subtotal == 0.0 ||
                subtotal >= FREE_DELIVERY_FROM
            ) {
                0.0
            } else {
                DELIVERY_FEE
            }

        val total =
            subtotal + delivery

        subtotalText.text =
            String.format(
                Locale.getDefault(),
                "R%.2f",
                subtotal
            )

        totalText.text =
            String.format(
                Locale.getDefault(),
                "R%.2f",
                total
            )

        checkoutButton.setOnClickListener {

            if (cartItems.isEmpty()) {

                Toast.makeText(
                    this,
                    "Your cart is empty.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            showCheckoutDialog(
                subtotal,
                delivery,
                total
            )
        }
    }

    // =========================================================
    // EDIT CART ITEM
    // =========================================================

    private fun showEditCustomisationDialog(
        index: Int
    ) {

        if (index !in cartItems.indices) {
            return
        }

        val parts =
            cartItems[index].split("|")

        val itemName =
            parts.getOrNull(0)
                ?: "Item"

        val price =
            parts.getOrNull(1)
                ?.toDoubleOrNull()
                ?: 0.0

        val currentCustomisation =
            if (parts.size >= 3) {
                parts.drop(2)
                    .joinToString("|")
            } else {
                ""
            }

        val dialogView =
            layoutInflater.inflate(
                R.layout.layout_customise,
                null
            )

        val pickles =
            dialogView.findViewById<CheckBox>(
                R.id.cb_no_pickles
            )

        val tomatoes =
            dialogView.findViewById<CheckBox>(
                R.id.cb_no_tomatoes
            )

        val onions =
            dialogView.findViewById<CheckBox>(
                R.id.cb_no_onions
            )

        val lettuce =
            dialogView.findViewById<CheckBox>(
                R.id.cb_no_lettuce
            )

        val sauce =
            dialogView.findViewById<CheckBox>(
                R.id.cb_no_sauce
            )

        val instructions =
            dialogView.findViewById<EditText>(
                R.id.et_special_instructions
            )

        val remember =
            dialogView.findViewById<CheckBox>(
                R.id.cb_remember_preferences
            )

        val customisationLower =
            currentCustomisation.lowercase(
                Locale.getDefault()
            )

        pickles.isChecked =
            customisationLower.contains(
                "no pickles"
            )

        tomatoes.isChecked =
            customisationLower.contains(
                "no tomatoes"
            )

        onions.isChecked =
            customisationLower.contains(
                "no onions"
            )

        lettuce.isChecked =
            customisationLower.contains(
                "no lettuce"
            )

        sauce.isChecked =
            customisationLower.contains(
                "no sauce"
            )

        if (
            currentCustomisation.contains(
                "Note:"
            )
        ) {

            val noteStart =
                currentCustomisation.indexOf(
                    "Note:"
                )

            instructions.setText(
                currentCustomisation
                    .substring(
                        noteStart + 5
                    )
                    .trim()
            )
        }

        val dialog =
            AlertDialog.Builder(this)
                .setTitle("Edit $itemName")
                .setView(dialogView)
                .setNegativeButton(
                    "CANCEL",
                    null
                )
                .setPositiveButton(
                    "SAVE"
                ) { _, _ ->

                    val changes =
                        mutableListOf<String>()

                    if (pickles.isChecked) {
                        changes.add(
                            "No pickles"
                        )
                    }

                    if (tomatoes.isChecked) {
                        changes.add(
                            "No tomatoes"
                        )
                    }

                    if (onions.isChecked) {
                        changes.add(
                            "No onions"
                        )
                    }

                    if (lettuce.isChecked) {
                        changes.add(
                            "No lettuce"
                        )
                    }

                    if (sauce.isChecked) {
                        changes.add(
                            "No sauce"
                        )
                    }

                    val note =
                        instructions.text
                            .toString()
                            .trim()

                    if (note.isNotEmpty()) {

                        changes.add(
                            "Note: $note"
                        )
                    }

                    val newItem =
                        if (changes.isEmpty()) {

                            "$itemName|$price"

                        } else {

                            "$itemName|$price|${
                                changes.joinToString(", ")
                            }"
                        }

                    cartItems[index] =
                        newItem

                    saveCart()

                    showCart()
                }
                .create()

        dialog.show()
    }

    // =========================================================
    // CHECKOUT
    // =========================================================

    private fun showCheckoutDialog(
        subtotal: Double,
        delivery: Double,
        total: Double
    ) {

        val message =
            String.format(
                Locale.getDefault(),
                "Subtotal: R%.2f\nDelivery: R%.2f\nTotal: R%.2f",
                subtotal,
                delivery,
                total
            )

        AlertDialog.Builder(this)
            .setTitle("Confirm Order")
            .setMessage(message)
            .setNegativeButton(
                "CANCEL",
                null
            )
            .setPositiveButton(
                "PLACE ORDER"
            ) { _, _ ->

                placeOrder(
                    subtotal,
                    delivery,
                    total
                )
            }
            .show()
    }

    private fun placeOrder(
        subtotal: Double,
        delivery: Double,
        total: Double
    ) {

        val order =
            JSONObject()

        order.put(
            "id",
            "KB-${System.currentTimeMillis()}"
        )

        order.put(
            "date",
            SimpleDateFormat(
                "dd MMM yyyy, HH:mm",
                Locale.getDefault()
            ).format(Date())
        )

        order.put(
            "subtotal",
            subtotal
        )

        order.put(
            "delivery",
            delivery
        )

        order.put(
            "total",
            total
        )

        val items =
            JSONArray()

        for (item in cartItems) {

            items.put(item)
        }

        order.put(
            "items",
            items
        )

        val savedOrders =
            getPrefs().getString(
                KEY_ORDERS,
                "[]"
            ) ?: "[]"

        val orders =
            JSONArray(savedOrders)

        orders.put(order)

        getPrefs()
            .edit()
            .putString(
                KEY_ORDERS,
                orders.toString()
            )
            .apply()

        cartItems.clear()

        saveCart()

        Toast.makeText(
            this,
            "Order placed successfully!",
            Toast.LENGTH_LONG
        ).show()

        showScreen(SCREEN_ORDERS)
    }

    // =========================================================
    // ORDERS
    // =========================================================

    private fun showOrders() {

        clearContent()

        val view = layoutInflater.inflate(
            R.layout.layout_orders,
            contentContainer,
            false
        )

        contentContainer.addView(view)

        val ordersContainer =
            view.findViewById<LinearLayout>(
                R.id.orders_container
            )

        ordersContainer.removeAllViews()

        val savedOrders =
            getPrefs().getString(
                KEY_ORDERS,
                "[]"
            ) ?: "[]"

        val orders =
            JSONArray(savedOrders)

        if (orders.length() == 0) {

            val emptyText =
                TextView(this)

            emptyText.text =
                "You have no orders yet."

            emptyText.textSize = 18f

            emptyText.setTextColor(
                getColor(
                    R.color.text_secondary
                )
            )

            emptyText.gravity =
                Gravity.CENTER

            emptyText.setPadding(
                20,
                60,
                20,
                60
            )

            ordersContainer.addView(
                emptyText
            )

            return
        }

        for (i in orders.length() - 1 downTo 0) {

            val order =
                orders.getJSONObject(i)

            val orderView =
                layoutInflater.inflate(
                    R.layout.item_order,
                    ordersContainer,
                    false
                )

            val orderIdText =
                orderView.findViewById<TextView>(
                    R.id.tv_order_id
                )

            val dateText =
                orderView.findViewById<TextView>(
                    R.id.tv_order_date
                )

            val detailsText =
                orderView.findViewById<TextView>(
                    R.id.tv_order_details
                )

            val totalText =
                orderView.findViewById<TextView>(
                    R.id.tv_order_total
                )

            orderIdText.text =
                order.optString(
                    "id",
                    "KRISPYBOX ORDER"
                )

            dateText.text =
                order.optString(
                    "date",
                    "Recent order"
                )

            totalText.text =
                String.format(
                    Locale.getDefault(),
                    "Total: R%.2f",
                    order.optDouble(
                        "total",
                        0.0
                    )
                )

            val items =
                order.optJSONArray(
                    "items"
                )

            val details =
                StringBuilder()

            if (items != null) {

                for (j in 0 until items.length()) {

                    val item =
                        items.optString(j)

                    val parts =
                        item.split("|")

                    val name =
                        parts.getOrNull(0)
                            ?: "Item"

                    val customisation =
                        if (parts.size >= 3) {
                            parts.drop(2)
                                .joinToString("|")
                        } else {
                            ""
                        }

                    details.append(
                        "• $name"
                    )

                    if (
                        customisation.isNotEmpty()
                    ) {

                        details.append(
                            "\n  $customisation"
                        )
                    }

                    details.append("\n\n")
                }
            }

            detailsText.text =
                details.toString().trim()

            ordersContainer.addView(
                orderView
            )
        }
    }

    // =========================================================
    // PROFILE
    // =========================================================

    private fun showProfile() {

        clearContent()

        val view = layoutInflater.inflate(
            R.layout.layout_profile,
            contentContainer,
            false
        )

        contentContainer.addView(view)

        val nameText =
            view.findViewById<TextView>(
                R.id.tv_profile_name
            )

        val emailText =
            view.findViewById<TextView>(
                R.id.tv_profile_email
            )

        val prefs = getPrefs()

        nameText.text =
            prefs.getString(
                KEY_NAME,
                "KrispyBox Customer"
            )

        emailText.text =
            prefs.getString(
                KEY_EMAIL,
                "customer@krispybox.com"
            )

        // MY CART
        view.findViewById<View>(
            R.id.btn_profile_cart
        ).setOnClickListener {

            showScreen(SCREEN_CART)
        }

        // MY ORDERS
        view.findViewById<View>(
            R.id.btn_profile_orders
        ).setOnClickListener {

            showScreen(SCREEN_ORDERS)
        }

        // SETTINGS
        view.findViewById<View>(
            R.id.btn_open_settings
        ).setOnClickListener {

            showScreen(SCREEN_SETTINGS)
        }
    }

    // =========================================================
    // SETTINGS
    // =========================================================

    private fun showSettings() {

        clearContent()

        val view = layoutInflater.inflate(
            R.layout.layout_settings,
            contentContainer,
            false
        )

        contentContainer.addView(view)

        val darkModeSwitch =
            view.findViewById<Switch>(
                R.id.switch_dark_mode
            )

        val prefs = getPrefs()

        val darkMode =
            prefs.getBoolean(
                KEY_DARK_MODE,
                true
            )

        darkModeSwitch.isChecked =
            darkMode

        darkModeSwitch.setOnCheckedChangeListener {
                _,
                checked ->

            prefs.edit()
                .putBoolean(
                    KEY_DARK_MODE,
                    checked
                )
                .putString(
                    KEY_CURRENT_SCREEN,
                    SCREEN_SETTINGS
                )
                .apply()

            AppCompatDelegate
                .setDefaultNightMode(
                    if (checked) {

                        AppCompatDelegate
                            .MODE_NIGHT_YES

                    } else {

                        AppCompatDelegate
                            .MODE_NIGHT_NO
                    }
                )
        }

        val logoutButton =
            view.findViewById<View>(
                R.id.btn_logout_action
            )

        logoutButton.setOnClickListener {

            AlertDialog.Builder(this)
                .setTitle("Log out?")
                .setMessage(
                    "Are you sure you want to log out?"
                )
                .setNegativeButton(
                    "CANCEL",
                    null
                )
                .setPositiveButton(
                    "LOG OUT"
                ) { _, _ ->

                    logout()
                }
                .show()
        }
    }

    private fun logout() {

        isLoggedIn = false

        getPrefs()
            .edit()
            .putBoolean(
                KEY_LOGGED_IN,
                false
            )
            .putString(
                KEY_CURRENT_SCREEN,
                SCREEN_HOME
            )
            .apply()

        bottomNavigation.visibility =
            View.GONE

        showLogin()
    }

    // =========================================================
    // BACK BUTTON
    // =========================================================

    private fun setupBackButton() {

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {

                    if (!isLoggedIn) {

                        finish()

                        return
                    }

                    when (currentScreen) {

                        SCREEN_MENU -> {

                            showScreen(
                                SCREEN_HOME
                            )
                        }

                        SCREEN_CART -> {

                            showScreen(
                                SCREEN_PROFILE
                            )
                        }

                        SCREEN_ORDERS -> {

                            showScreen(
                                SCREEN_PROFILE
                            )
                        }

                        SCREEN_SETTINGS -> {

                            showScreen(
                                SCREEN_PROFILE
                            )
                        }

                        SCREEN_PROFILE -> {

                            showScreen(
                                SCREEN_HOME
                            )
                        }

                        SCREEN_CUSTOMISE -> {

                            showScreen(
                                customiseReturnScreen
                            )
                        }

                        SCREEN_HOME -> {

                            finish()
                        }

                        else -> {

                            showScreen(
                                SCREEN_HOME
                            )
                        }
                    }
                }
            }
        )
    }

    // =========================================================
    // CART STORAGE
    // =========================================================

    private fun saveCart() {

        val array =
            JSONArray()

        for (item in cartItems) {

            array.put(item)
        }

        getPrefs()
            .edit()
            .putString(
                KEY_CART,
                array.toString()
            )
            .apply()
    }

    private fun loadCart() {

        cartItems.clear()

        val saved =
            getPrefs().getString(
                KEY_CART,
                "[]"
            ) ?: "[]"

        try {

            val array =
                JSONArray(saved)

            for (i in 0 until array.length()) {

                cartItems.add(
                    array.optString(i)
                )
            }

        } catch (_: Exception) {

            cartItems.clear()
        }
    }

    // =========================================================
    // CLEAR CONTENT
    // =========================================================

    private fun clearContent() {

        contentContainer.removeAllViews()
    }
}