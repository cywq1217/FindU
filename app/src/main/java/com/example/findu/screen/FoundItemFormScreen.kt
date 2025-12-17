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
import com.example.findu.model.ItemCategory
import com.example.findu.ui.components.LocationPicker
import com.example.findu.utils.LocationUtils
import com.example.findu.viewmodel.FoundItemFormViewModel
import com.example.findu.viewmodel.FoundItemFormViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoundItemFormScreen(
    category: ItemCategory,
    onBackClick: () -> Unit,
    onSubmitClick: (Map<String, String>, Pair<Double, Double>) -> Unit
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
    val selectedLocation by viewModel.selectedLocation.collectAsState()

    // 本地UI状态
    var locationError by remember { mutableStateOf<String?>(null) }
    var isLocating by remember { mutableStateOf(false) }
    var showMapPicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 获取位置的逻辑
    val getLocation = {
        scope.launch {
            isLocating = true
            if (!LocationUtils.isLocationEnabled(context)) {
                locationError = "系统定位服务未开启，请在设置中打开GPS"
                isLocating = false
                return@launch
            }
            try {
                locationError = null
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

    // 权限请求
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.any { it.value }) {
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
                title = { Text("填写拾得信息 - ${category.displayName}") },
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
                            onSubmitClick(formData, loc)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = isFormValid && selectedLocation != null
                ) {
                    Text("下一步（上传图片）", fontSize = 18.sp)
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
                                        Icon(Icons.Default.CheckCircle, "", tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("已选择位置", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    Text(
                                        "Lat: ${"%.4f".format(selectedLocation!!.first)}\nLon: ${"%.4f".format(selectedLocation!!.second)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.MyLocation, "", tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(if (isLocating) "正在获取位置..." else "请选择位置", color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }

                            Row {
                                IconButton(
                                    onClick = {
                                        val hasPerms = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                        if (hasPerms) getLocation() else locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                                    },
                                    enabled = !isLocating
                                ) {
                                    Icon(Icons.Default.MyLocation, "快速定位", tint = MaterialTheme.colorScheme.primary)
                                }
                                OutlinedButton(
                                    onClick = { showMapPicker = true },
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Icon(Icons.Default.Map, null, Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("地图选点")
                                }
                            }
                        }
                        
                        if (locationError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(locationError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            // 2. 动态表单区域
            item {
                Text("物品特征", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 4.dp))
            }

            items(fieldTemplate.fields) { field: FormField ->
                var value by remember(field.label) { mutableStateOf(formData[field.label] ?: "") }
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
                    leadingIcon = { Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.outline) },
                    supportingText = {
                        if (isError) Text("此项必填") else if (field.privacyNote.isNotEmpty()) Text(field.privacyNote, style = MaterialTheme.typography.bodySmall)
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
            dragHandle = null,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                LocationPicker(
                    onLocationSelected = { lat, lng ->
                        viewModel.updateLocation(lat, lng)
                    }
                )
                Button(
                    onClick = { showMapPicker = false },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                ) {
                    Text("确认选择此位置")
                }
                IconButton(
                    onClick = { showMapPicker = false },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 16.dp, start = 16.dp)
                        .background(Color.White.copy(alpha = 0.7f), shape = RoundedCornerShape(50))
                ) {
                    Icon(Icons.Default.ArrowBack, "Close")
                }
            }
        }
    }
}