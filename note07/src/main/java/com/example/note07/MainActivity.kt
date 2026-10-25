package com.example.note07

import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.note07.ui.theme.MobileProjectTheme
import com.google.gson.Gson
import kotlinx.parcelize.Parcelize

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileProjectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        //AppNav()
                        //SavedStateNav()
                        //JsonNav()
                     MyApp()
                    }
                }
            }
        }
    }
}

//---------------------------------------------------------------------------
@Composable
fun AppNav() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen { navController.navigate("detail/42") }
        }
        composable(
            route = "detail/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.IntType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId")
            DetailScreen(itemId)
        }
    }
}

@Composable
fun HomeScreen(onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text("Go to Detail (Basic Arg)")
    }
}

@Composable
fun DetailScreen(itemId: Int?) {
    Text("Detail Screen: ItemId = $itemId")
}

//---------------------------------------------------------------------------
@Parcelize
data class User(val name: String, val age: Int) : Parcelable

@Composable
fun SavedStateNav() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "userList") {
        composable("userList") {
            UserListScreen { user ->
                navController.currentBackStackEntry?.savedStateHandle?.set("user", user)
                navController.navigate("userDetail")
            }
        }
        composable("userDetail") {
            val user = navController.previousBackStackEntry?.savedStateHandle?.get<User>("user")
            UserDetailScreen(user)
        }
    }
}

@Composable
fun UserListScreen(onUserSelected: (User) -> Unit) {
    Button(onClick = { onUserSelected(User("Alice", 25)) }) {
        Text("Select Alice")
    }
}

@Composable
fun UserDetailScreen(user: User?) {
    Text("User: ${user?.name}, Age: ${user?.age}")
}

//---------------------------------------------------------------------------
data class Product(val name: String, val price: Double)

@Composable
fun JsonNav() {
    val navController = rememberNavController()
    val gson = Gson()
    NavHost(navController = navController, startDestination = "productList") {
        composable("productList") {
            ProductListScreen { product ->
                val json = Uri.encode(gson.toJson(product), "UTF-8")
                navController.navigate("productDetail/$json")
            }
        }
        composable(
            route = "productDetail/{productJson}",
            arguments = listOf(navArgument("productJson") { type = NavType.StringType })
        ) { backStackEntry ->
            val json = backStackEntry.arguments?.getString("productJson")
            val product = gson.fromJson(json, Product::class.java)
            ProductDetailScreen(product)
        }
    }
}

@Composable
fun ProductListScreen(onClick: (Product) -> Unit) {
    Button(onClick = { onClick(Product("Camera", 599.99)) }) {
        Text("View Camera Details")
    }
}

@Composable
fun ProductDetailScreen(product: Product) {
    Text("Product: ${product.name}, Price: $${product.price}")
}

//---------------------------------------------------------------------------
 data class User2(val name: String, val age: Int) //주석 처리: 이미 위에 Parcelable User가 정의되어 있습니다.

// ViewModel 정의
class SharedViewModel : ViewModel() {
    var selectedUser by mutableStateOf<User2?>(null)
        private set

    fun selectUser(user: User2) {
        selectedUser = user
    }
}

@Composable
fun UserListScreen(navController: NavController, viewModel: SharedViewModel) {
    val user = User2("Alice", 20)

    Button(onClick = {
        viewModel.selectUser(user)
        navController.navigate("user_detail")
    }) {
        Text("View Detail")
    }
}

@Composable
fun UserDetailScreen(
    navController: NavController,
    viewModel: SharedViewModel
) {
    val user = viewModel.selectedUser

    user?.let {
        Text("Name: ${it.name}, Age: ${it.age}")
    } ?: Text("No user selected.")
}

@Composable
fun MyApp() {
    val navController = rememberNavController()
    val viewModel: SharedViewModel = viewModel()
    
    NavHost(navController = navController, startDestination = "user_list") {
        composable("user_list") {
            UserListScreen(navController = navController, viewModel)
        }
        composable("user_detail") {
            UserDetailScreen(navController, viewModel)
        }
    }
}

