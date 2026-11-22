package me.weishu.kernelsu.ui.util

/** Quote a path/argument for the root shell. */
fun String.shellQuote(): String = "'" + replace("'", "'\\''") + "'"
