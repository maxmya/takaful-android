package com.takaful.user.utils

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.vacxe.phonemask.PhoneMaskManager
import com.takaful.user.R

object StringUtils {

        fun makeUrlColoredSpan(
            sentence: String,
            word: String,
            textView: TextView,
            clickableSpan: ClickableSpan) {

            val builder = SpannableStringBuilder()
            val startIndex = sentence.indexOf(word)
            val endIndex = startIndex + word.length
            val spannableString = SpannableString(sentence)
            val boldSpan = StyleSpan(Typeface.BOLD_ITALIC)

            spannableString.setSpan(
                boldSpan,
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            spannableString.setSpan(
                clickableSpan,
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            spannableString.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(textView.context, R.color.colorPrimary)),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)


            builder.append(spannableString)
            textView.setText(builder, TextView.BufferType.SPANNABLE)
            textView.movementMethod = LinkMovementMethod.getInstance()
        }


        fun maskPhoneField(field: EditText) {
            PhoneMaskManager()
                .withMask("### ###-##-###")
                .bindTo(field)
        }

        fun getUnmaskedPhone(fieldPhone: EditText): String {
            return fieldPhone
                .text
                .toString()
                .replace(" ", "")
                .replace("-", "")
                .trim()
        }


}