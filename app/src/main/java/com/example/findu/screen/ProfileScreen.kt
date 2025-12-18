package com.example.findu.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.findu.model.FoundItem
import com.example.findu.model.ItemStatus
import com.example.findu.model.LostItem
import com.example.findu.model.User
import com.example.findu.viewmodel.ProfileViewModel
import com.example.findu.viewmodel.ProfileViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    userId: String,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(LocalContext.current, userId))
) {
    val myLostItems by viewModel.myLostItems.collectAsState(initial = emptyList())
    val myFoundItems by viewModel.myFoundItems.collectAsState(initial = emptyList())
    val currentUser by viewModel.currentUser.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    
    // 每次进入页面时自动刷新数据
    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("个人中心") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 用户头像和基本信息区域
            // 临时修改：显示传入的 userId 帮助调试
            UserProfileHeader(user = currentUser, debugUserId = userId)

            Spacer(modifier = Modifier.height(16.dp))

            // 选项卡
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("我丢失的") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("我拾得的") })
            }

            // 列表内容
            Box(modifier = Modifier.fillMaxSize()) {
                when(selectedTab) {
                    0 -> MyLostItemsList(
                        items = myLostItems,
                        onItemClick = { lostItem ->
                            // 如果状态是 MATCHED，跳转到详情页查看匹配到的物品
                            if (lostItem.status == ItemStatus.MATCHED && lostItem.matchedFoundItemId != null) {
                                navController.navigate("matched_result/${lostItem.matchedFoundItemId}")
                            }
                        }
                    )
                    1 -> MyFoundItemsList(
                        items = myFoundItems,
                        onItemClick = { foundItem ->
                            // 查看自己捡到的物品详情
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun UserProfileHeader(user: User?, debugUserId: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        // 如果用户加载中，显示 ID 方便排查
        if (user == null) {
            Text(
                text = "加载中...",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "正在查找ID: $debugUserId",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            Text(
                text = user.username,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = user.phone,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MyLostItemsList(
    items: List<LostItem>,
    onItemClick: (LostItem) -> Unit
) {
    if (items.isEmpty()) {
        EmptyState("还没有发布寻物信息")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                LostItemCard(item = item, onClick = { onItemClick(item) })
            }
        }
    }
}

@Composable
fun MyFoundItemsList(
    items: List<FoundItem>,
    onItemClick: (FoundItem) -> Unit
) {
    if (items.isEmpty()) {
        EmptyState("还没有发布招领信息")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                FoundItemCard(item = item, onClick = { onItemClick(item) })
            }
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun LostItemCard(item: LostItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.category.name, // 使用资源字符串优化
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(status = item.status)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("丢失时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(item.loseTime))}")
            
            if (item.status == ItemStatus.MATCHED) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onClick() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("查看匹配结果")
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            } else if (item.status == ItemStatus.SEARCHING) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "请耐心等待，若有匹配物品，我们将通过消息通知您",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun FoundItemCard(item: FoundItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(status = item.status) // FoundItem 也可以有状态，比如已被认领
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("拾得时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(item.pickUpTime))}")
        }
    }
}

@Composable
fun StatusBadge(status: ItemStatus?) {
    val (text, color) = when (status) {
        ItemStatus.SEARCHING -> "寻找中" to Color.Gray
        ItemStatus.MATCHED -> "已匹配" to Color(0xFF4CAF50) // Green
        ItemStatus.COMPLETED -> "已完成" to Color.Blue
        null -> "寻找中" to Color.Gray // 默认状态
    }
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = color, style = MaterialTheme.typography.labelSmall)
    }
}
