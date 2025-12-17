package com.example.findu.screen

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.findu.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllActivitiesScreen(
    onBackClick: () -> Unit
) {
    // 使用 HomeViewModel 从 Supabase 获取数据
    val homeViewModel: HomeViewModel = viewModel()
    val foundItems by homeViewModel.foundItems.collectAsState()
    val lostItems by homeViewModel.lostItems.collectAsState()
    
    // 加载数据
    LaunchedEffect(Unit) {
        homeViewModel.loadData()
    }

    // Tab 状态: 0 -> 全部, 1 -> 拾得, 2 -> 遗失
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("全部", "拾得", "遗失")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("全部动态", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                if (selectedTab == 0 || selectedTab == 1) {
                    if (selectedTab == 1 && foundItems.isEmpty()) {
                        item { ActivityListEmptyState("暂无拾得信息") }
                    } else {
                        // 拾得信息
                        if (selectedTab == 0 && foundItems.isNotEmpty()) item { SectionHeader("最新拾得") }
                        items(foundItems) { item ->
                            ActivityItem(
                                title = "有人捡到了 ${item.category.displayName}",
                                time = formatActivityTime(item.submitTime),
                                type = ActivityType.FOUND,
                                features = item.features // 传入特征数据
                            )
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        }
                    }
                }

                if (selectedTab == 0 || selectedTab == 2) {
                    if (selectedTab == 2 && lostItems.isEmpty()) {
                        item { ActivityListEmptyState("暂无遗失信息") }
                    } else {
                        // 遗失信息
                        if (selectedTab == 0 && lostItems.isNotEmpty()) item { SectionHeader("最新遗失") }
                        items(lostItems) { item ->
                            ActivityItem(
                                title = "有人丢失了 ${item.category.displayName}",
                                time = formatActivityTime(item.submitTime),
                                type = ActivityType.LOST,
                                features = item.features // 传入特征数据
                            )
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        }
                    }
                }
                
                // 如果是"全部"标签且两者都为空
                if (selectedTab == 0 && foundItems.isEmpty() && lostItems.isEmpty()) {
                    item { ActivityListEmptyState("暂无任何动态") }
                }
            }
        }
    }
}

enum class ActivityType { FOUND, LOST }

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun ActivityListEmptyState(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
fun ActivityItem(
    title: String, 
    time: String,
    type: ActivityType,
    features: Map<String, String> // 新增参数
) {
    // 处理特征信息：过滤掉敏感字段，只展示非敏感信息
    val featureDescription = remember(features) {
        features.entries
            .filter { (key, _) -> !isSensitiveKey(key) } // 直接过滤敏感字段
            .joinToString(" · ") { (key, value) ->
                "$key: $value"
            }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (type == ActivityType.FOUND) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (type == ActivityType.FOUND) "拾" else "丢",
                fontWeight = FontWeight.Bold,
                color = if (type == ActivityType.FOUND) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            
            // 展示特征描述 (如果有)
            if (featureDescription.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = featureDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, // 使用辅助色，不那么显眼
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

// 判断字段名是否敏感
fun isSensitiveKey(key: String): Boolean {
    return key.contains("姓名") || key.contains("名") ||
            key.contains("学号") || key.contains("号") ||
            key.contains("电话") || key.contains("手机") ||
            key.contains("证件")
}

fun formatActivityTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}