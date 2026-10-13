package com.exora.rates

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

/**
 * Exora — a modern TRY-based currency board.
 *
 * One fetch of the TRY-based rates serves every base: cross rates are derived as
 * (perTry[base] / perTry[code]). The base is chosen with the chip bar at the top,
 * the list animates on change, pull-to-refresh reloads, and rates refresh every
 * 10 minutes. The last good rates are cached and shown when offline.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var swipe: SwipeRefreshLayout
    private lateinit var list: RecyclerView
    private lateinit var chips: ChipGroup
    private lateinit var statusView: TextView
    private lateinit var statusDot: View

    private val adapter = RateAdapter()
    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())

    private var perTry: Map<String, Double> = emptyMap()
    private var base: String = "TRY"
    private var lastUpdate: Long = 0L
    private var online: Boolean = true

    private val prefs by lazy { getSharedPreferences("rates", MODE_PRIVATE) }

    private val refresh = object : Runnable {
        override fun run() {
            fetch()
            handler.postDelayed(this, REFRESH_MS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        swipe = findViewById(R.id.swipe)
        list = findViewById(R.id.list)
        chips = findViewById(R.id.baseChips)
        statusView = findViewById(R.id.status)
        statusDot = findViewById(R.id.statusDot)

        setupWindow()
        setupList()
        setupSwipe()
        buildChips()

        renderFromCache()
        handler.post(refresh)
    }

    override fun onDestroy() {
        handler.removeCallbacks(refresh)
        executor.shutdownNow()
        super.onDestroy()
    }

    private fun setupWindow() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        @Suppress("DEPRECATION")
        window.navigationBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        val header = findViewById<View>(R.id.header)
        val baseHeaderTop = header.paddingTop
        val baseListBottom = list.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            header.updatePadding(top = baseHeaderTop + bars.top)
            list.updatePadding(bottom = baseListBottom + bars.bottom)
            insets
        }
    }

    private fun setupList() {
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter
        list.itemAnimator = DefaultItemAnimator().apply {
            addDuration = 220
            changeDuration = 200
            moveDuration = 200
            removeDuration = 160
        }
    }

    private fun setupSwipe() {
        swipe.setColorSchemeColors(ContextCompat.getColor(this, R.color.white))
        swipe.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(this, R.color.surface_high)
        )
        swipe.setOnRefreshListener { fetch() }
    }

    private fun buildChips() {
        chips.removeAllViews()
        for (code in Currencies.SELECTABLE) {
            val chip = Chip(this).apply {
                text = code
                isCheckable = true
                isChecked = code == base
                setChipIconVisible(false)
                setCheckedIconVisible(false)
                chipMinHeight = dp(38).toFloat()
                shapeAppearanceModel = ShapeAppearanceModel.builder()
                    .setAllCorners(CornerFamily.ROUNDED, dp(19).toFloat())
                    .build()
                chipStrokeWidth = dp(1).toFloat()
                chipStartPadding = dp(16).toFloat()
                chipEndPadding = dp(16).toFloat()
                chipBackgroundColor =
                    ContextCompat.getColorStateList(this@MainActivity, R.color.chip_bg)!!
                chipStrokeColor =
                    ContextCompat.getColorStateList(this@MainActivity, R.color.chip_stroke)!!
                setTextColor(
                    ContextCompat.getColorStateList(this@MainActivity, R.color.chip_text)!!
                )
                textSize = 14f
            }
            chips.addView(chip)
        }
        chips.setOnCheckedStateChangeListener { group, checkedIds ->
            val id = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val code = group.findViewById<Chip>(id)?.text?.toString()
                ?: return@setOnCheckedStateChangeListener
            if (code != base) {
                base = code
                render()
            }
        }
    }

    private fun renderFromCache() {
        val cached = prefs.getString(KEY_JSON, null) ?: return
        val parsed = parse(cached) ?: return
        perTry = parsed
        lastUpdate = prefs.getLong(KEY_TIME, 0L)
        render()
        updateStatus()
    }

    private fun fetch() {
        setStatus("Updating\u2026", live = false)
        executor.execute {
            val json = fetchUrl(PRIMARY_URL) ?: fetchUrl(FALLBACK_URL)
            val parsed = json?.let { parse(it) }
            handler.post {
                swipe.isRefreshing = false
                if (json != null && parsed != null) {
                    perTry = parsed
                    lastUpdate = System.currentTimeMillis()
                    online = true
                    prefs.edit()
                        .putString(KEY_JSON, json)
                        .putLong(KEY_TIME, lastUpdate)
                        .apply()
                    render()
                } else {
                    online = false
                }
                updateStatus()
            }
        }
    }

    /** Rebuild the visible list for the current base, animating the change. */
    private fun render() {
        if (perTry.isEmpty()) return
        val baseRate = perTry[base] ?: return
        val rows = Currencies.ORDER
            .filter { it != base && perTry.containsKey(it) }
            .map { code ->
                RateRow(code, Currencies.name(code), baseRate / perTry.getValue(code), base)
            }
        adapter.submitList(rows)
    }

    private fun updateStatus() {
        if (lastUpdate == 0L) {
            setStatus(if (online) "Loading\u2026" else "Offline", live = false)
            return
        }
        val prefix = if (online) "Updated " else "Offline \u00b7 "
        setStatus(prefix + clock(lastUpdate), live = online)
    }

    private fun setStatus(text: String, live: Boolean) {
        statusView.text = text
        statusDot.alpha = if (live) 1f else 0.3f
    }

    /** Parse try.json into "amount of <code> per 1 TRY". TRY itself is 1.0. */
    private fun parse(json: String): Map<String, Double>? {
        return try {
            val obj = JSONObject(json).getJSONObject("try")
            val out = HashMap<String, Double>()
            out["TRY"] = 1.0
            for (code in Currencies.ORDER) {
                if (code == "TRY") continue
                val v = obj.optDouble(code.lowercase(Locale.US), Double.NaN)
                if (!v.isNaN() && v > 0) out[code] = v
            }
            if (out.size <= 1) null else out
        } catch (e: Exception) {
            null
        }
    }

    private fun fetchUrl(url: String): String? {
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

    private fun clock(ts: Long): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ts))

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

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
