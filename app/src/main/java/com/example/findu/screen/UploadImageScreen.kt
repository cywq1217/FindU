package com.example.findu.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.findu.model.ItemCategory
import com.example.findu.utils.ImageUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadImageScreen(
    category: ItemCategory,
    onBackClick: () -> Unit,
    onSubmitClick: (String, Pair<Double, Double>) -> Unit
) {
    val context = LocalContext.current

    // 修复：明确指定类型
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imagePath by remember { mutableStateOf<String?>(null) }
    var location by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    // 相机启动器
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // 图片拍摄成功
            imagePath?.let { path ->
                // 压缩图片
                val compressedFile = ImageUtils.compressImage(File(path))
                imagePath = compressedFile.absolutePath
                imageUri = Uri.fromFile(compressedFile) // 更新显示的 URI
            }
        }
    }

    // 相册选择器
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            // 这里需要处理从相册选择的图片保存到本地，暂时模拟一个路径
             // 实际项目中应该将 uri 复制到应用私有目录
             // val file = ImageUtils.uriToFile(context, it)
             // imagePath = file.absolutePath
             imagePath = it.path // 仅作示例，这可能不是真实文件路径
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("上传物品图片") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        },
        bottomBar = {
            Box(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = {
                        imagePath?.let { path ->
                            // 模拟定位数据，实际应该调用定位服务
                            val mockLocation = Pair(31.2304, 121.4737) 
                            location = mockLocation
                            onSubmitClick(path, mockLocation)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = imagePath != null // 暂时移除location检查，因为没有实现实际定位
                ) {
                    Text("完成提交")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 图片预览区域
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "物品图片",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "请上传物品图片",
                            color = Color.Gray
                        )
                    }
                }
            }

            // 上传按钮区域
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        val imageFile = ImageUtils.launchCamera(context, cameraLauncher)
                        imagePath = imageFile.absolutePath
                        // 这里不需要设置 imageUri，因为 TakePicture contract 成功后会自动写入
                        // 我们需要在 callback 中设置 imageUri 来刷新 UI，
                        // 但因为 launchCamera 返回了 file，我们可以提前构建 uri 用于预览（可选）
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "拍照",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("拍照上传")
                }

                Spacer(modifier = Modifier.size(16.dp))

                Button(
                    onClick = {
                        galleryLauncher.launch("image/*")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Photo,
                        contentDescription = "相册",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("相册选择")
                }
            }

            // 定位信息显示
            location?.let { (lat, lng) ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "定位信息",
                            style = androidx.compose.material3.MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "纬度: ${"%.5f".format(lat)}")
                        Text(text = "经度: ${"%.5f".format(lng)}")
                    }
                }
            }
        }
    }
}
