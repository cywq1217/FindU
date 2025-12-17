package com.example.findu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.amap.api.maps.MapsInitializer
import com.example.findu.model.FoundItem
import com.example.findu.screen.AllActivitiesScreen
import com.example.findu.screen.FoundItemCategoryScreen
import com.example.findu.screen.FoundItemFormScreen
import com.example.findu.screen.HomeScreen
import com.example.findu.screen.LoginScreen
import com.example.findu.screen.MatchedResultScreen
import com.example.findu.screen.MessageScreen
import com.example.findu.screen.ProfileScreen
import com.example.findu.screen.RegisterScreen
import com.example.findu.screen.SubmissionSuccessScreen
import com.example.findu.screen.UploadImageScreen
import com.example.findu.screen.LostItemCategoryScreen
import com.example.findu.screen.LostItemFormScreen
import com.example.findu.screen.MatchResultScreen
import com.example.findu.ui.theme.FindUTheme
import com.example.findu.viewmodel.AuthViewModel
import com.example.findu.viewmodel.AuthViewModelFactory
import com.example.findu.viewmodel.FoundItemFormViewModel
import com.example.findu.viewmodel.FoundItemFormViewModelFactory

// 全局单例，用于在页面间传递复杂的匹配结果
object MatchResultHolder {
    var similarity: Double = 0.0
    var matchedItem: FoundItem? = null

    fun clear() {
        similarity = 0.0
        matchedItem = null
    }
}

// 新增：用于暂存拾得端表单数据的单例
object FoundItemDraftHolder {
    var formData: Map<String, String> = emptyMap()
    var location: Pair<Double, Double>? = null
    
    fun clear() {
        formData = emptyMap()
        location = null
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 高德地图合规检查
        try {
            MapsInitializer.updatePrivacyShow(this, true, true)
            MapsInitializer.updatePrivacyAgree(this, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        enableEdgeToEdge()
        setContent {
            FindUTheme {
                FindUApp()
            }
        }
    }
}

@Composable
fun FindUApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(context)
    )

    val currentUser by authViewModel.currentUser.collectAsState()
    var isLoggedIn by remember { mutableStateOf(currentUser != null) }

    LaunchedEffect(currentUser) {
        isLoggedIn = currentUser != null
    }

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) "home" else "login"
    ) {
        // 登录页面
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        // 注册页面
        composable("register") {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // 主页面
        composable("home") {
            HomeScreen(
                navController = navController,
                authViewModel = authViewModel,
                onFoundClick = {
                    navController.navigate("found/category")
                },
                onLostClick = {
                    navController.navigate("lost/category")
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                // 新增：点击查看全部动态
                onViewAllActivities = {
                    navController.navigate("activities")
                }
            )
        }
        
        // 新增：全部动态页面
        composable("activities") {
            AllActivitiesScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // 个人中心
        composable("profile/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: "currentUser"
            ProfileScreen(
                navController = navController,
                userId = userId
            )
        }
        
        // 消息中心
        composable("message") {
            val currentUserId = currentUser?.userId ?: "currentUser"
            MessageScreen(
                navController = navController,
                userId = currentUserId
            )
        }
        
        // 匹配详情页
        composable("matched_result/{foundItemId}") { backStackEntry ->
            val foundItemId = backStackEntry.arguments?.getString("foundItemId")
            if (foundItemId != null) {
                MatchedResultScreen(
                    navController = navController,
                    foundItemId = foundItemId
                )
            }
        }

        // 拾得端类别选择
        composable("found/category") {
            FoundItemCategoryScreen(
                onCategorySelected = { category ->
                    navController.navigate("found/form/${category.name}")
                }
            )
        }

        // 拾得端表单填写
        composable("found/form/{category}") { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("category")
            val category = com.example.findu.model.ItemCategory.values()
                .find { it.name == categoryName }
                ?: com.example.findu.model.ItemCategory.OTHERS

            FoundItemFormScreen(
                category = category,
                onBackClick = {
                    navController.popBackStack()
                },
                onSubmitClick = { formData, location ->
                    FoundItemDraftHolder.formData = formData
                    FoundItemDraftHolder.location = location
                    navController.navigate("found/upload/${category.name}")
                }
            )
        }

        // 拾得端图片上传
        composable("found/upload/{category}") { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("category")
            val category = com.example.findu.model.ItemCategory.values()
                .find { it.name == categoryName }
                ?: com.example.findu.model.ItemCategory.OTHERS

            val viewModel: FoundItemFormViewModel = viewModel(
                factory = FoundItemFormViewModelFactory(context)
            )

            UploadImageScreen(
                category = category,
                onBackClick = {
                    navController.popBackStack()
                },
                onSubmitClick = { imagePath, _ ->
                    val formData = FoundItemDraftHolder.formData
                    val location = FoundItemDraftHolder.location ?: Pair(0.0, 0.0)
                    val currentUserId = currentUser?.userId ?: "currentUser"

                    viewModel.submitFoundItemWithMatch(
                        category = category,
                        features = formData,
                        imagePath = imagePath,
                        location = location,
                        userId = currentUserId
                    )

                    FoundItemDraftHolder.clear()
                    navController.navigate("submission/success")
                }
            )
        }

        // 提交成功页面
        composable("submission/success") {
            SubmissionSuccessScreen(
                onBackHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        // 遗失端类别选择
        composable("lost/category") {
            LostItemCategoryScreen(
                onCategorySelected = { category ->
                    navController.navigate("lost/form/${category.name}")
                }
            )
        }

        // 遗失端表单填写
        composable("lost/form/{category}") { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("category")
            val category = com.example.findu.model.ItemCategory.values()
                .find { it.name == categoryName }
                ?: com.example.findu.model.ItemCategory.OTHERS

            val currentUserId = currentUser?.userId ?: "currentUser"

            LostItemFormScreen(
                category = category,
                userId = currentUserId,
                onBackClick = {
                    navController.popBackStack()
                },
                onSubmitClick = { similarity, matchedItem ->
                    MatchResultHolder.similarity = similarity
                    MatchResultHolder.matchedItem = matchedItem
                    navController.navigate("match/result")
                }
            )
        }

        // 匹配结果页面
        composable("match/result") {
            MatchResultScreen(
                similarity = MatchResultHolder.similarity,
                matchedItem = MatchResultHolder.matchedItem,
                onBackClick = {
                    navController.popBackStack("home", inclusive = false)
                }
            )
        }
    }
}