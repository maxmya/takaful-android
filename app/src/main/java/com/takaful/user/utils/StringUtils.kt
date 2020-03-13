package com.takaful.user.utils

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.takaful.user.R

class StringUtils {

    companion object {

        fun makeTextColoredAndBold(
            sentence: String,
            word: String,
            textView: TextView
        ) {

            val builder = SpannableStringBuilder()
            val startIndex = sentence.indexOf(word)
            val endIndex = startIndex + word.length
            val spannableString = SpannableString(sentence)
            val boldSpan = StyleSpan(Typeface.BOLD_ITALIC)

            spannableString.setSpan(
                boldSpan,
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            spannableString.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(textView.context, R.color.colorPrimary)),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            builder.append(spannableString)
            textView.setText(builder, TextView.BufferType.SPANNABLE)
        }

    }
}