package com.dieghosty10.ghostymusicy.ui.utils

fun String?.resize(w: Int, h: Int): String? {
    if (this == null) return null
    if (this.contains("googleusercontent.com") || this.contains("ggpht.com")) {
        return this.replace(Regex("=w\\d+-h\\d+"), "=w$w-h$h")
            .replace(Regex("=s\\d+"), "=s$w")
    }
    return this
}
