package com.example.playlistmaker.search.domain

import android.content.res.Resources
import android.util.TypedValue

class DpToPxUseCase {
    fun execute(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            Resources.getSystem().displayMetrics
        ).toInt()
    }
}
