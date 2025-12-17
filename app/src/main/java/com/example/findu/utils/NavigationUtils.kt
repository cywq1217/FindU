package com.example.findu.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object NavigationUtils {

    /**
     * 启动高德地图导航
     */
    fun startNavigation(
        context: Context,
        latitude: Double,
        longitude: Double,
        destination: String = "目的地"
    ) {
        try {
            // 高德地图URI方案
            val uri = Uri.parse("amapuri://route/plan/?dlat=$latitude&dlon=$longitude&dname=$destination&dev=0&t=0")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.autonavi.minimap")

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // 如果没有安装高德地图，使用网页版
                val webUri = Uri.parse("https://uri.amap.com/navigation?to=$longitude,$latitude,$destination")
                val webIntent = Intent(Intent.ACTION_VIEW, webUri)
                context.startActivity(webIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "启动导航失败，请检查是否安装了地图应用", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}