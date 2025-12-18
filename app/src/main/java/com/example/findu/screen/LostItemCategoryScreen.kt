package com.example.findu.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.findu.model.ItemCategory
import com.example.findu.viewmodel.FoundItemFormViewModel
import com.example.findu.viewmodel.FoundItemFormViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LostItemCategoryScreen(
    onCategorySelected: (ItemCategory) -> Unit
) {
    val context = LocalContext.current
    val viewModel: FoundItemFormViewModel = viewModel(
        factory = FoundItemFormViewModelFactory(context)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("您丢失了什么？", fontWeight = FontWeight.Bold) },
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
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = "请选择遗失物品类别，我们将帮您匹配",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(ItemCategory.values().size) { index ->
                    val category = ItemCategory.values()[index]
                    
                    // 进入动画：交错浮入
                    val animatable = remember { Animatable(0f) }
                    LaunchedEffect(Unit) {
                        animatable.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(
                                durationMillis = 400,
                                delayMillis = index * 50
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                alpha = animatable.value
                                translationY = 50f * (1f - animatable.value)
                            }
                    ) {
                        LostCategoryCard(
                            category = category,
                            onClick = {
                                viewModel.initForm(category)
                                onCategorySelected(category)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LostCategoryCard(
    category: ItemCategory,
    onClick: () -> Unit
) {
    // 点击反馈：缩放效果
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.95f else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null, 
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 0.dp
        ),
        colors = CardDefaults.cardColors(
            // 与拾得端相同结构，使用 surface 背景色
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        // 蓝色系圆形背景，与拾得端橙色区分
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getLostCategoryIcon(category),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.tertiary // 蓝色系图标
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// 复用图标逻辑，也可以根据需要自定义
fun getLostCategoryIcon(category: ItemCategory): ImageVector {
    return when (category) {
        ItemCategory.CAMPUS_CARD -> Icons.Default.Person
        ItemCategory.KEYS -> Icons.Default.Key
        ItemCategory.HEADPHONES -> Icons.Default.Headphones
        ItemCategory.WALLET -> Icons.Default.Wallet
        ItemCategory.CLOTHES -> Icons.Default.Work
        ItemCategory.BACKPACK -> Icons.Default.School
        ItemCategory.ELECTRONICS -> Icons.Default.Devices
        ItemCategory.OTHERS -> Icons.Default.Category
    }
}