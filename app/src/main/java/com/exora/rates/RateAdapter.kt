package com.exora.rates

import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

/** One "1 CODE = value BASE" line. */
data class RateRow(
    val code: String,
    val name: String,
    val value: Double,
    val base: String
)

class RateAdapter : ListAdapter<RateRow, RateAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rate, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    class VH(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val code: TextView = itemView.findViewById(R.id.code)
        private val name: TextView = itemView.findViewById(R.id.name)
        private val value: TextView = itemView.findViewById(R.id.value)
        private val unit: TextView = itemView.findViewById(R.id.unit)

        fun bind(row: RateRow) {
            val ctx = itemView.context

            val codeText = SpannableString("1 ${row.code} =")
            codeText.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(ctx, R.color.faint)),
                0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            codeText.setSpan(
                StyleSpan(Typeface.BOLD), 2, 2 + row.code.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            codeText.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(ctx, R.color.faint)),
                codeText.length - 1, codeText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            code.text = codeText

            name.text = row.name
            value.text = format(row.value)
            unit.text = row.base
        }

        private fun format(v: Double): String = when {
            v >= 1000 -> String.format(Locale.US, "%,.0f", v)
            v >= 1 -> String.format(Locale.US, "%,.2f", v)
            v >= 0.01 -> String.format(Locale.US, "%.3f", v)
            else -> String.format(Locale.US, "%.5f", v)
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<RateRow>() {
            override fun areItemsTheSame(a: RateRow, b: RateRow) = a.code == b.code
            override fun areContentsTheSame(a: RateRow, b: RateRow) = a == b
        }
    }
}
