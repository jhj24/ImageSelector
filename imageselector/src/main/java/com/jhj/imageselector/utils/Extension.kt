package com.jhj.imageselector.utils

import android.app.Activity
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat


/**
 * Created by jhj on 19-1-21.
 */

fun <T> List<T>.toArrayList(): ArrayList<T> {
    return ArrayList(this)
}

fun Activity.getTColor(@ColorRes textColor: Int): Int {
    return ContextCompat.getColor(this, textColor)
}

fun Activity.getImgDrawable(@DrawableRes drawableRes: Int): Drawable? {
    return ContextCompat.getDrawable(this, drawableRes)
}

fun Activity.selected(press: Int, normal: Int): StateListDrawable {
    return selected(ContextCompat.getDrawable(this, press), ContextCompat.getDrawable(this, normal))
}

fun Activity.selected(press: Drawable?, normal: Drawable?): StateListDrawable {
    val drawable = StateListDrawable()
    drawable.addState(intArrayOf(android.R.attr.state_selected, android.R.attr.state_enabled), press)
    drawable.addState(intArrayOf(), normal)
    return drawable
}
