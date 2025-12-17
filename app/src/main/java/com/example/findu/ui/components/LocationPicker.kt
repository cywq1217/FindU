package com.example.findu.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.MyLocationStyle

/**
 * 地图选点组件
 * @param onLocationSelected 当地图停止移动时回调选中的中心点经纬度 (Lat, Lng)
 */
@Composable
fun LocationPicker(
    onLocationSelected: (Double, Double) -> Unit
) {
    var aMapInstance by remember { mutableStateOf<AMap?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. 高德地图视图
        AmapView { mapView ->
            val map = mapView.map
            aMapInstance = map

            // 配置定位蓝点样式
            val myLocationStyle = MyLocationStyle()
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE)
            map.myLocationStyle = myLocationStyle
            map.isMyLocationEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = false // 我们自己实现按钮

            // 监听地图移动
            map.setOnCameraChangeListener(object : AMap.OnCameraChangeListener {
                override fun onCameraChange(position: CameraPosition?) {}

                override fun onCameraChangeFinish(position: CameraPosition?) {
                    position?.target?.let { target ->
                        onLocationSelected(target.latitude, target.longitude)
                    }
                }
            })
        }

        // 2. 中心固定标记 (Pin)
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Selected Location",
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.Center)
                .padding(bottom = 24.dp), // 让图标底部尖端对准中心
            tint = Color.Red
        )

        // 3. "回到当前位置"按钮
        FloatingActionButton(
            onClick = {
                // 触发定位，移动到当前位置
                aMapInstance?.let { map ->
                    // 设置为定位模式并触发一次定位
                    val style = MyLocationStyle()
                    style.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE)
                    map.myLocationStyle = style
                    map.isMyLocationEnabled = true
                    
                    // 简单地将缩放级别调整到合适的大小
                    map.moveCamera(CameraUpdateFactory.zoomTo(17f))
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(imageVector = Icons.Default.MyLocation, contentDescription = "My Location")
        }
    }
}
