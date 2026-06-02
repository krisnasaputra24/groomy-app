package com.krisna.groomy.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.krisna.groomy.pages.AddPetPage
import com.krisna.groomy.pages.ChatPage
import com.krisna.groomy.pages.EditPetPage
import com.krisna.groomy.pages.EditProfilePage
import com.krisna.groomy.pages.GroomerDashboard
import com.krisna.groomy.pages.GroomerDetailPage
import com.krisna.groomy.pages.GroomerJobHistoryPage
import com.krisna.groomy.pages.GroomerLocationPage
import com.krisna.groomy.pages.GroomerProfileManagementPage
import com.krisna.groomy.pages.GroomerPromoManagementPage
import com.krisna.groomy.pages.GroomerSchedulePage
import com.krisna.groomy.pages.GroomerTransactionPage
import com.krisna.groomy.pages.LoginPage
import com.krisna.groomy.pages.PaymentPage
import com.krisna.groomy.pages.RegisterGroomerPage
import com.krisna.groomy.pages.SignupPage
import com.krisna.groomy.screens.MainScreen
import com.krisna.groomy.screens.SplashScreen

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = modifier
    ) {
        composable("splash") {
            SplashScreen(navController = navController)
        }

        composable("login") {
            LoginPage(navController = navController )
        }

        composable("signup") {
            SignupPage(navController = navController)
        }

        composable("home") {
            MainScreen(navController = navController)
        }

        composable("register_groomer") {
            RegisterGroomerPage(navController = navController)
        }

        composable("groomer_dashboard") {
            GroomerDashboard(navController = navController)
        }

        composable("groomer_profile_management") {
            GroomerProfileManagementPage(navController = navController)
        }

        composable("groomer_promo_management") {
            GroomerPromoManagementPage(navController = navController)
        }

        composable("groomer_transactions") {
            GroomerTransactionPage(navController = navController)
        }

        composable("groomer_schedule") {
            GroomerSchedulePage(navController = navController)
        }

        composable("groomer_job_history") {
            GroomerJobHistoryPage(navController = navController)
        }

        composable("groomer_location") {
            GroomerLocationPage(navController = navController)
        }

        composable("groomer_detail/{groomerId}") { backStackEntry ->
            val groomerIdString = backStackEntry.arguments?.getString("groomerId") ?: "0"
            val groomerId = groomerIdString.toIntOrNull() ?: 0
            GroomerDetailPage(navController, groomerId)
        }

        composable("payment/{groomerName}/{serviceName}/{price}") { backStackEntry ->
            val groomerName = backStackEntry.arguments?.getString("groomerName") ?: ""
            val serviceName = backStackEntry.arguments?.getString("serviceName") ?: ""
            val price = backStackEntry.arguments?.getString("price") ?: ""
            PaymentPage(navController, groomerName, serviceName, price)
        }

        composable("edit_pet/{id}/{name}/{type}/{breed}/{age}/{weight}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            val name = backStackEntry.arguments?.getString("name") ?: ""
            val type = backStackEntry.arguments?.getString("type") ?: ""
            val breed = backStackEntry.arguments?.getString("breed") ?: ""
            val age = backStackEntry.arguments?.getString("age") ?: ""
            val weight = backStackEntry.arguments?.getString("weight") ?: ""
            EditPetPage(navController, id, name, type, breed, age, weight)
        }

        composable("edit_profile") {
            EditProfilePage(navController = navController)
        }

        composable("add_pet") {
            AddPetPage(navController = navController)
        }

        composable("chat/{orderId}/{groomerId}/{userName}") { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId")?.toIntOrNull() ?: 0
            val groomerId = backStackEntry.arguments?.getString("groomerId")?.toIntOrNull() ?: 0
            val userName = backStackEntry.arguments?.getString("userName") ?: "User"
            ChatPage(navController, orderId, groomerId, userName)
        }
    }
}
