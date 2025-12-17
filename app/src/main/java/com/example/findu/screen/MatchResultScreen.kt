package com.example.findu.screen

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Poi
import com.amap.api.navi.AmapNaviPage
import com.amap.api.navi.AmapNaviParams
import com.amap.api.navi.AmapNaviType
import com.amap.api.navi.INaviInfoCallback
import com.amap.api.navi.model.AMapNaviLocation
import com.example.findu.model.FoundItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchResultScreen(
    similarity: Double,
    matchedItem: FoundItem?,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val isMatchSuccess = matchedItem != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("匹配结果", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // 状态图标区
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isMatchSuccess) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isMatchSuccess) Icons.Default.CheckCircle else Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp),
                                tint = if (isMatchSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (isMatchSuccess) "匹配成功！" else "暂无匹配",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isMatchSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isMatchSuccess) {
                    Text(
                        text = "相似度 ${(similarity * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LinearProgressIndicator(
                        progress = similarity.toFloat(),
                        modifier = Modifier
                            .width(200.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                } else {
                    Text(
                        text = "暂时没有找到符合您描述的物品。\n您可以稍后再试，系统将持续为您匹配。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (isMatchSuccess && matchedItem != null) {
                    // 物品详情卡片
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "物品详情",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Divider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                            
                            MatchInfoRow(Icons.Default.Category, "类别", matchedItem.category.name)
                            
                            val date = Date(matchedItem.pickUpTime)
                            val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            MatchInfoRow(Icons.Default.AccessTime, "拾得时间", format.format(date))
                            
                            // 动态特征
                            matchedItem.features.forEach { (key, value) ->
                                MatchInfoRow(Icons.Default.Description, key, value)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 位置导航卡片
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn, 
                                        contentDescription = null, 
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "位置信息",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // 实际地址或经纬度
                            Text(
                                text = "经度: ${String.format("%.4f", matchedItem.longitude)}\n纬度: ${String.format("%.4f", matchedItem.latitude)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    startInternalNavigationForMatch(context, matchedItem.latitude, matchedItem.longitude)
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("前往导航 (App内)", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isMatchSuccess) {
                    Text(
                        text = "温馨提示：请在公共场所完成物品交接，注意保护个人隐私。",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun MatchInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = label, 
                color = MaterialTheme.colorScheme.secondary, 
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = value, 
                fontWeight = FontWeight.Medium, 
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// 复用 MatchedResultScreen 中的导航逻辑
fun startInternalNavigationForMatch(context: Context, lat: Double, lng: Double) {
    val end = Poi("物品位置", LatLng(lat, lng), "")
    val params = AmapNaviParams(null, null, end, AmapNaviType.WALK)
    
    try {
        AmapNaviPage.getInstance().showRouteActivity(
            context, 
            params, 
            object : INaviInfoCallback {
                override fun onInitNaviFailure() {
                    Toast.makeText(context, "App内导航初始化失败", Toast.LENGTH_SHORT).show()
                }
                override fun onGetNavigationText(p0: String?) {}
                override fun onLocationChange(p0: AMapNaviLocation?) {}
                override fun onArriveDestination(p0: Boolean) {}
                override fun onStartNavi(p0: Int) {}
                override fun onCalculateRouteSuccess(p0: IntArray?) {}
                override fun onCalculateRouteFailure(p0: Int) {
                    Toast.makeText(context, "路线规划失败", Toast.LENGTH_SHORT).show()
                }
                override fun onStopSpeaking() {}
                override fun onReCalculateRoute(p0: Int) {}
                override fun onExitPage(p0: Int) {}
                override fun onStrategyChanged(p0: Int) {}
                override fun onArrivedWayPoint(p0: Int) {}
                override fun onMapTypeChanged(p0: Int) {}
                override fun onNaviDirectionChanged(p0: Int) {}
                override fun onDayAndNightModeChanged(p0: Int) {}
                override fun onBroadcastModeChanged(p0: Int) {}
                override fun onScaleAutoChanged(p0: Boolean) {}
                override fun getCustomMiddleView() = null
                override fun getCustomNaviView() = null
                override fun getCustomNaviBottomView() = null
            }
        )
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "启动错误: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}