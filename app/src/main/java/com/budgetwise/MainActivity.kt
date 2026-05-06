package com.budgetwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.budgetwise.data.db.AppDatabase
import com.budgetwise.data.models.Expense
import com.budgetwise.data.models.User
import com.budgetwise.data.repository.*
import com.budgetwise.ui.Screen
import com.budgetwise.ui.auth.*
import com.budgetwise.ui.badges.BadgesScreen
import com.budgetwise.ui.categories.CategoryManagementScreen
import com.budgetwise.ui.dashboard.*
import com.budgetwise.ui.expenses.*
import com.budgetwise.ui.graphs.GraphScreen
import com.budgetwise.ui.theme.BudgetWiseTheme
import com.budgetwise.viewmodels.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db           = AppDatabase.getInstance(this)
        val userRepo     = UserRepository(db.userDao())
        val categoryRepo = CategoryRepository(db.categoryDao())
        val expenseRepo  = ExpenseRepository(db.expenseDao())
        val badgeRepo    = BadgeRepository(db.badgeDao())

        setContent {
            BudgetWiseTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    BudgetWiseApp(
                        userRepo     = userRepo,
                        categoryRepo = categoryRepo,
                        expenseRepo  = expenseRepo,
                        badgeRepo    = badgeRepo,
                        onSaveBudget = { user, amount ->
                            lifecycleScope.launch { userRepo.updateBudget(user, amount) }
                        }
                    )
                }
            }
        }
    }
}

private data class BottomNavItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)

private val bottomNavItems = listOf(
    BottomNavItem("Dashboard",  Icons.Default.Home,        Screen.Dashboard.route),
    BottomNavItem("Expenses",   Icons.Default.List,        Screen.ExpenseList.route),
    BottomNavItem("Categories", Icons.Default.Category,    Screen.Categories.route),
    BottomNavItem("Graphs",     Icons.Default.BarChart,    Screen.Graphs.route),
    BottomNavItem("Badges",     Icons.Default.EmojiEvents, Screen.Badges.route),
)

private val bottomBarRoutes = bottomNavItems.map { it.route }.toSet()

@Composable
fun BudgetWiseApp(
    userRepo: UserRepository,
    categoryRepo: CategoryRepository,
    expenseRepo: ExpenseRepository,
    badgeRepo: BadgeRepository,
    onSaveBudget: (User, Double) -> Unit
) {
    val authVm: AuthViewModel = viewModel(factory = AuthViewModelFactory(userRepo, categoryRepo))
    val currentUser by authVm.currentUser.collectAsState()
    val rootNav = rememberNavController()

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            rootNav.navigate("main") { popUpTo(Screen.Login.route) { inclusive = true } }
        } else {
            rootNav.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
        }
    }

    NavHost(navController = rootNav, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(vm = authVm, onNavigateToRegister = { rootNav.navigate(Screen.Register.route) })
        }
        composable(Screen.Register.route) {
            RegisterScreen(vm = authVm, onNavigateToLogin = { rootNav.popBackStack() })
        }
        composable("main") {
            val user = currentUser ?: return@composable
            MainScreen(
                user         = user,
                categoryRepo = categoryRepo,
                expenseRepo  = expenseRepo,
                badgeRepo    = badgeRepo,
                onSaveBudget = onSaveBudget,
                onLogout     = { authVm.logout() }
            )
        }
    }
}

@Composable
fun MainScreen(
    user: User,
    categoryRepo: CategoryRepository,
    expenseRepo: ExpenseRepository,
    badgeRepo: BadgeRepository,
    onSaveBudget: (User, Double) -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick  = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            icon  = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Dashboard.route,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                val expenseVm: ExpenseViewModel = viewModel(factory = ExpenseViewModelFactory(expenseRepo, user.id))
                DashboardScreen(
                    expenseVm     = expenseVm,
                    monthlyBudget = user.monthlyBudget,
                    onAddExpense  = { navController.navigate(Screen.AddExpense.route) },
                    onViewAll     = { navController.navigate(Screen.ExpenseList.route) },
                    onSetBudget   = { navController.navigate(Screen.BudgetSetting.route) },
                    onLogout      = onLogout
                )
            }
            composable(Screen.ExpenseList.route) {
                val expenseVm: ExpenseViewModel = viewModel(factory = ExpenseViewModelFactory(expenseRepo, user.id))
                val catVm: CategoryViewModel    = viewModel(factory = CategoryViewModelFactory(categoryRepo, user.id))
                val categories by catVm.categories.collectAsState()
                ExpenseListScreen(
                    vm         = expenseVm,
                    categories = categories,
                    onEdit     = { expense ->
                        navController.currentBackStackEntry?.savedStateHandle?.set("editExpense", expense)
                        navController.navigate(Screen.EditExpense.createRoute(expense.id))
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AddExpense.route) {
                val expenseVm: ExpenseViewModel = viewModel(factory = ExpenseViewModelFactory(expenseRepo, user.id))
                val catVm: CategoryViewModel    = viewModel(factory = CategoryViewModelFactory(categoryRepo, user.id))
                val categories by catVm.categories.collectAsState()
                AddEditExpenseScreen(categories = categories, expenseVm = expenseVm, onBack = { navController.popBackStack() })
            }
            composable(Screen.EditExpense.route) {
                val expense = navController.previousBackStackEntry?.savedStateHandle?.get<Expense>("editExpense")
                val expenseVm: ExpenseViewModel = viewModel(factory = ExpenseViewModelFactory(expenseRepo, user.id))
                val catVm: CategoryViewModel    = viewModel(factory = CategoryViewModelFactory(categoryRepo, user.id))
                val categories by catVm.categories.collectAsState()
                AddEditExpenseScreen(existingExpense = expense, categories = categories, expenseVm = expenseVm, onBack = { navController.popBackStack() })
            }
            composable(Screen.Categories.route) {
                val catVm: CategoryViewModel = viewModel(factory = CategoryViewModelFactory(categoryRepo, user.id))
                CategoryManagementScreen(vm = catVm, onBack = { navController.popBackStack() })
            }
            composable(Screen.Graphs.route) {
                val expenseVm: ExpenseViewModel = viewModel(factory = ExpenseViewModelFactory(expenseRepo, user.id))
                GraphScreen(vm = expenseVm, onBack = { navController.popBackStack() })
            }
            composable(Screen.Badges.route) {
                val badgeVm: BadgeViewModel = viewModel(factory = BadgeViewModelFactory(badgeRepo, user.id))
                BadgesScreen(vm = badgeVm, onBack = { navController.popBackStack() })
            }
            composable(Screen.BudgetSetting.route) {
                BudgetSettingScreen(
                    currentBudget = user.monthlyBudget,
                    onSave        = { amount -> onSaveBudget(user, amount) },
                    onBack        = { navController.popBackStack() }
                )
            }
        }
    }
}
