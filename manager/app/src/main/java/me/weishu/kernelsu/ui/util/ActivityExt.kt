package me.weishu.kernelsu.ui.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/** Unwrap the activity that hosts [this] context, if any. */
fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
