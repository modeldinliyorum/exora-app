package com.exora.rates

import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

/**
 * Single-screen currency board. Shows TRY-based rates, USD and EUR first,
 * refreshes every 10 minutes and keeps the last successful rates when offline.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var primaryList: LinearLayout
    private lateinit var secondaryList: LinearLayout
    private lateinit var statusView: TextView

    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())

    // Most important first: USD and EUR at the very top.
    private val primary = listOf("usd" to "USD", "eur" to "EUR")
    private val secondary = listOf(
        "gbp" to "GBP",
        "chf" to "CHF",
        "jpy" to "JPY",
        "aed" to "AED",
        "cad" to "CAD"
    )

    private val prefs by lazy { getSharedPreferences("rates", MODE_PRIVATE) }

    private val refresh = object : Runnable {
        override fun run() {
            fetchRates()
            handler.postDelayed(this, REFRESH_MS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        goImmersive()

        primaryList = findViewById(R.id.primaryList)
        secondaryList = findViewById(R.id.secondaryList)
        statusView = findViewById(R.id.status)

        renderFromCache()
        handler.post(refresh)
    }

    override fun onDestroy() {
        handler.removeCallbacks(refresh)
        executor.shutdownNow()
        super.onDestroy()
    }

    private fun goImmersive() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    /** Show the last successful rates immediately, before any network call. */
    private fun renderFromCache() {
        val cached = prefs.getString(KEY_JSON, null) ?: return
        val rates = parse(cached) ?: return
        render(rates)
        setStatus("Last saved " + clock(prefs.getLong(KEY_TIME, 0L)))
    }

    private fun fetchRates() {
        setStatus("Updating\u2026")
        executor.execute {
            val json = fetch(PRIMARY_URL) ?: fetch(FALLBACK_URL)
            val rates = json?.let { parse(it) }
            handler.post {
                if (json != null && rates != null) {
                    prefs.edit()
                        .putString(KEY_JSON, json)
                        .putLong(KEY_TIME, System.currentTimeMillis())
                        .apply()
                    render(rates)
                    setStatus("Updated " + clock(System.currentTimeMillis()))
                } else {
                    setStatus("Offline \u2014 showing last saved rates")
                }
            }
        }
    }

    /** Parse the API payload into "1 <code> = value TRY" values. */
    private fun parse(json: String): Map<String, Double>? {
        return try {
            val base = JSONObject(json).getJSONObject("try")
            val out = HashMap<String, Double>()
            (primary + secondary).forEach { (code, _) ->
                val perTry = base.optDouble(code, Double.NaN)
                if (!perTry.isNaN() && perTry > 0) out[code] = 1.0 / perTry
            }
            if (out.isEmpty()) null else out
        } catch (e: Exception) {
            null
        }
    }

    private fun fetch(url: String): String? {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 10_000
                readTimeout = 10_000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
            }
            if (conn.responseCode in 200..299) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    private fun render(rates: Map<String, Double>) {
        fill(primaryList, primary, rates, primary = true)
        fill(secondaryList, secondary, rates, primary = false)
    }

    private fun fill(
        container: LinearLayout,
        defs: List<Pair<String, String>>,
        rates: Map<String, Double>,
        primary: Boolean
    ) {
        container.removeAllViews()
        for ((code, label) in defs) {
            val value = rates[code] ?: continue
            container.addView(row(label, value, primary))
        }
    }

    /** One line: "1 USD = 41.20 TRY" with the number emphasised. */
    private fun row(label: String, value: Double, primary: Boolean): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(if (primary) 18 else 12) }
        }
        row.addView(text("1 $label =", if (primary) 18f else 15f, R.color.text_muted))
        row.addView(
            text(
                String.format(Locale.US, "%,.2f", value),
                if (primary) 44f else 26f,
                R.color.white
            ).apply {
                setPadding(dp(10), 0, dp(10), 0)
                setTypeface(typeface, Typeface.BOLD)
            }
        )
        row.addView(text("TRY", if (primary) 18f else 15f, R.color.text_muted))
        return row
    }

    private fun text(value: String, sizeSp: Float, colorRes: Int) = TextView(this).apply {
        text = value
        textSize = sizeSp
        setTextColor(getColor(colorRes))
    }

    private fun setStatus(value: String) {
        statusView.text = value
    }

    private fun clock(ts: Long): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ts))

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    companion object {
        private const val PRIMARY_URL =
            "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/try.json"
        private const val FALLBACK_URL =
            "https://latest.currency-api.pages.dev/v1/currencies/try.json"
        private const val REFRESH_MS = 10 * 60 * 1000L
        private const val KEY_JSON = "json"
        private const val KEY_TIME = "time"
    }
}
