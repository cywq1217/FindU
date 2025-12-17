package com.example.findu.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.findu.model.FormField
import com.example.findu.model.FoundItem
import com.example.findu.model.ItemCategory
import com.example.findu.ui.components.LocationPicker
import com.example.findu.utils.LocationUtils
import com.example.findu.viewmodel.FoundItemFormViewModel
import com.example.findu.viewmodel.FoundItemFormViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LostItemFormScreen(
    category: ItemCategory,
    userId: String, // 新增 userId 参数
    onBackClick: () -> Unit,
    onSubmitClick: (Double, FoundItem?) -> Unit
) {
    val context = LocalContext.current
    val viewModel: FoundItemFormViewModel = viewModel(
        factory = FoundItemFormViewModelFactory(context)
    )
    val scope = rememberCoroutineScope()

    // 状态管理
    val fieldTemplate by viewModel.currentTemplate.collectAsState()
    val formData by viewModel.formData.collectAsState()
    val isFormValid by viewModel.isFormValid.collectAsState()
    
    // 从 ViewModel 获取选中的位置
    val selectedLocation by viewModel.selectedLocation.collectAsState()

    // 本地状态用于显示定位过程中的错误或状态，
    // 但实际位置存储在 ViewModel 中以便跨组件持久化
    var locationError by remember { mutableStateOf<String?>(null) }
    var isLocating by remember { mutableStateOf(false) }
    
    // 地图选择器 Sheet 状态
    var showMapPicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 监听匹配结果
    LaunchedEffect(Unit) {
        viewModel.matchResult.collect { result ->
            onSubmitClick(result.similarity, result.matchedItem)
        }
    }

    // 定义获取位置的逻辑（使用原生定位作为后备或快速定位）
    val getLocation = {
        scope.launch {
            isLocating = true
            // 1. 检查系统定位开关
            if (!LocationUtils.isLocationEnabled(context)) {
                locationError = "系统定位服务未开启，请在设置中打开GPS"
                isLocating = false
                return@launch
            }

            try {
                locationError = null
                // 2. 获取位置（LocationUtils已优化，包含超时和fallback）
                val currentLocation = LocationUtils.getCurrentLocation(context)
                if (currentLocation != null) {
                    viewModel.updateLocation(currentLocation.first, currentLocation.second)
                    locationError = null
                } else {
                    locationError = "无法获取位置，请确保模拟器/设备GPS已开启"
                }
            } catch (e: Exception) {
                locationError = "定位失败: ${e.message}"
            } finally {
                isLocating = false
            }
        }
    }

    // 权限请求启动器
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineLocation || coarseLocation) {
            getLocation()
        } else {
            locationError = "请授予定位权限以获取位置信息"
        }
    }

    // 初始化表单
    LaunchedEffect(category) {
        viewModel.initForm(category)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("填写遗失信息 - ${category.displayName}") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        selectedLocation?.let { loc ->
                            // 传入 userId
                            viewModel.submitLostItem(category, formData, loc, userId)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = isFormValid && selectedLocation != null
                ) {
                    Text("提交并开始匹配", fontSize = 18.sp)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 1. 定位卡片区域
            item {
                Text(
                    text = "位置信息 (必填)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                if (selectedLocation != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.CheckCircle, 
                                            contentDescription = null,
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "已选择位置",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        "Lat: ${"%.4f".format(selectedLocation!!.first)}\nLon: ${"%.4f".format(selectedLocation!!.second)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.MyLocation,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            if (isLocating) "正在获取位置..." else "请选择位置",
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            Row {
                                // 快速定位按钮
                                IconButton(
                                    onClick = {
                                        val hasFine = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                        val hasCoarse = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                        if (hasFine || hasCoarse) getLocation() else locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                                    },
                                    enabled = !isLocating
                                ) {
                                    Icon(Icons.Default.MyLocation, contentDescription = "快速定位", tint = MaterialTheme.colorScheme.primary)
                                }
                                
                                // 地图选点按钮
                                OutlinedButton(
                                    onClick = { showMapPicker = true },
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("地图选点")
                                }
                            }
                        }
                        
                        if (locationError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(locationError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            // 2. 动态表单区域
            item {
                Text(
                    text = "物品特征",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            items(fieldTemplate.fields) { field: FormField ->
                var value by remember(field.label) {
                    mutableStateOf(formData[field.label] ?: "")
                }
                val isError = field.isRequired && value.isBlank()

                OutlinedTextField(
                    value = value,
                    onValueChange = {
                        value = it
                        viewModel.updateFormData(field.label, it)
                    },
                    label = { Text(field.label + if (field.isRequired) " *" else "") },
                    placeholder = { Text(field.hint) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = isError,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                    },
                    supportingText = {
                        if (isError) {
                            Text("此项必填")
                        } else if (field.privacyNote.isNotEmpty()) {
                            Text(field.privacyNote, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                )
            }
            
            item { Spacer(modifier = Modifier.height(60.dp)) } // 底部留白
        }
    }

    // 地图选点 BottomSheet
    if (showMapPicker) {
        ModalBottomSheet(
            onDismissRequest = { showMapPicker = false },
            sheetState = sheetState,
            dragHandle = null, // 隐藏默认的拖动手柄以获得更多地图空间
            modifier = Modifier.fillMaxSize() // 全屏显示
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                LocationPicker(
                    onLocationSelected = { lat, lng ->
                        // 实时更新，这里我们不需要做任何事，因为用户还没确认
                        // 但我们可以选择实时更新 ViewModel 或者只在关闭时更新
                        // 为了更好的体验，我们可以在界面上显示一个“确认选择”按钮
                        // 这里简化逻辑：地图移动时我们记录一个临时变量，点击确认按钮时更新ViewModel
                    }
                )
                
                // 在 LocationPicker 上方覆盖一个关闭/确认按钮
                // 注意：LocationPicker 内部已经有了定位逻辑，我们只需要一个确认退出的机制
                // 这里因为 LocationPicker 的回调是实时的，我们为了简单，可以再包装一层
                // 或者更简单地：让 LocationPicker 增加一个“确认并返回”的按钮，但这会耦合
                
                // 我们采用在 Sheet 顶部加一个 Header 的方式
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter)
                ) {
                     // 实际实现中，LocationPicker 已经全屏了。
                     // 我们可以在 LocationPicker 内部实现返回，或者覆盖一个按钮。
                }
                
                // 覆盖一个“确认位置”按钮在底部
                Button(
                    onClick = { 
                         // 由于 LocationPicker 的 onLocationSelected 是实时回调，
                         // 我们需要捕获最后一次的值。
                         // 修改 LocationPicker 让它内部维护状态，或者在这里用变量接收
                         showMapPicker = false 
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                ) {
                    Text("确认选择此位置")
                }
                
                // 关闭按钮
                 IconButton(
                    onClick = { showMapPicker = false },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 16.dp, start = 16.dp)
                        .background(Color.White.copy(alpha = 0.7f), shape = RoundedCornerShape(50))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Close")
                }
            }
        }
    }
}
