package com.pickit.app.presentation.navigation

import android.net.Uri

object Routes {
    const val Home = "home"
    const val Add = "add"
    const val Preview = "preview?imageUri={imageUri}&note={note}"
    const val Detail = "detail/{productId}"
    const val Stats = "stats"
    const val Settings = "settings"

    fun detail(productId: String): String = "detail/$productId"

    fun preview(imageUri: String, note: String): String =
        "preview?imageUri=${Uri.encode(imageUri)}&note=${Uri.encode(note)}"
}
