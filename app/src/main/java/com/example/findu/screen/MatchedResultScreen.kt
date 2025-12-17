package com.example.findu.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Poi
import com.amap.api.navi.AmapNaviPage
import com.amap.api.navi.AmapNaviParams
import com.amap.api.navi.AmapNaviType
import com.amap.api.navi.INaviInfoCallback
import com.amap.api.navi.model.AMapNaviLocation
import com.example.findu.viewmodel.MatchedResultViewModel
import com.example.findu.viewmodel.MatchedResultViewModelFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchedResultScreen(
    navController: NavController,
    foundItemId: String,
    viewModel: MatchedResultViewModel = viewModel(factory = MatchedResultViewModelFactory(LocalContext.current, foundItemId))
) {
    val foundItem by viewModel.foundItem.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("匹配详情", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            foundItem?.let { item ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // 标题区
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "系统为您匹配到了疑似物品！",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // 图片卡片
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
                    ) {
                        if (item.imagePath.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(File(item.imagePath)),
                                contentDescription = "物品图片",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.ImageNotSupported,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("暂无图片", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    // 信息列表卡片
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "物品信息",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            // 修复：使用 Divider 而不是 HorizontalDivider (旧版本 Compose Material3)
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(12.dp))

                            MatchedInfoItem(
                                icon = Icons.Default.Category,
                                label = "类别",
                                value = item.category.name
                            )
                            
                            MatchedInfoItem(
                                icon = Icons.Default.AccessTime,
                                label = "拾得时间",
                                value = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(item.pickUpTime))
                            )
                            
                            item.features.forEach { (key, value) ->
                                MatchedInfoItem(
                                    icon = Icons.Default.Description, // 可以根据key选择不同图标
                                    label = key,
                                    value = value
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // 操作按钮组
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                startInternalNavigation(context, item.latitude, item.longitude)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(Icons.Default.Navigation, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("App内导航 (推荐)", style = MaterialTheme.typography.titleMedium)
                        }

                        OutlinedButton(
                            onClick = {
                                openMapNavigation(context, item.latitude, item.longitude)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("跳转外部地图 (高德/百度)", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            } ?: run {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("正在加载物品信息...", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun MatchedInfoItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * 启动 App 内部的高德导航组件
 */
fun startInternalNavigation(context: Context, lat: Double, lng: Double) {
    Log.d("FindU_Nav", "startInternalNavigation called")
    val end = Poi("物品位置", LatLng(lat, lng), "")
    val params = AmapNaviParams(null, null, end, AmapNaviType.WALK)
    
    try {
        AmapNaviPage.getInstance().showRouteActivity(
            context, 
            params, 
            object : INaviInfoCallback {
                override fun onInitNaviFailure() {
                    Log.e("FindU_Nav", "onInitNaviFailure")
                    Toast.makeText(context, "App内导航初始化失败，请尝试外部跳转", Toast.LENGTH_SHORT).show()
                }
                override fun onGetNavigationText(p0: String?) {}
                override fun onLocationChange(p0: AMapNaviLocation?) {}
                override fun onArriveDestination(p0: Boolean) {}
                override fun onStartNavi(p0: Int) {}
                override fun onCalculateRouteSuccess(p0: IntArray?) {}
                override fun onCalculateRouteFailure(p0: Int) {
                    Log.e("FindU_Nav", "onCalculateRouteFailure: $p0")
                    Toast.makeText(context, "路线规划失败(Code:$p0)，请检查网络或定位", Toast.LENGTH_SHORT).show()
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
        Log.d("FindU_Nav", "showRouteActivity executed")
    } catch (e: Exception) {
        e.printStackTrace()
        Log.e("FindU_Nav", "Exception starting nav", e)
        Toast.makeText(context, "启动错误: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

// 备用：通用地图跳转
fun openMapNavigation(context: Context, lat: Double, lng: Double) {
    val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(物品位置)")
    val mapIntent = Intent(Intent.ACTION_VIEW, uri)
    mapIntent.setPackage("com.autonavi.minimap") // 尝试高德

    try {
        context.startActivity(mapIntent)
    } catch (e: Exception) {
        val browserIntent = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(browserIntent)
        } catch (e2: Exception) {
             Toast.makeText(context, "未找到地图应用", Toast.LENGTH_SHORT).show()
        }
    }
}
