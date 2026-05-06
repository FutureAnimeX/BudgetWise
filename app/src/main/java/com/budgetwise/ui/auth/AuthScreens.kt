package com.budgetwise.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.budgetwise.viewmodels.AuthState
import com.budgetwise.viewmodels.AuthViewModel

@Composable
fun LoginScreen(vm: AuthViewModel, onNavigateToRegister: () -> Unit) {
    val state by vm.state.collectAsState()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val errorMsg = (state as? AuthState.Error)?.message

    Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("BudgetWise", style = MaterialTheme.typography.displaySmall)
        Text("Your finances, simplified.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(36.dp))
        OutlinedTextField(value = username, onValueChange = { username = it; vm.clearError() }, label = { Text("Username") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = password, onValueChange = { password = it; vm.clearError() }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
        if (errorMsg != null) { Spacer(Modifier.height(8.dp)); Text(errorMsg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        Spacer(Modifier.height(24.dp))
        Button(onClick = { vm.login(username, password) }, enabled = state !is AuthState.Loading, modifier = Modifier.fillMaxWidth()) {
            if (state is AuthState.Loading) CircularProgressIndicator(Modifier.size(18.dp)) else Text("Log In")
        }
        TextButton(onClick = onNavigateToRegister) { Text("Don't have an account? Register") }
    }
}

@Composable
fun RegisterScreen(vm: AuthViewModel, onNavigateToLogin: () -> Unit) {
    val state by vm.state.collectAsState()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm  by remember { mutableStateOf("") }
    var matchError by remember { mutableStateOf<String?>(null) }
    val errorMsg = (state as? AuthState.Error)?.message

    Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Create Account", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(28.dp))
        OutlinedTextField(value = username, onValueChange = { username = it; vm.clearError() }, label = { Text("Username") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = password, onValueChange = { password = it; vm.clearError() }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = confirm, onValueChange = { confirm = it; matchError = null }, label = { Text("Confirm Password") }, visualTransformation = PasswordVisualTransformation(), isError = matchError != null, supportingText = matchError?.let { { Text(it) } }, singleLine = true, modifier = Modifier.fillMaxWidth())
        if (errorMsg != null) { Spacer(Modifier.height(8.dp)); Text(errorMsg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        Spacer(Modifier.height(24.dp))
        Button(onClick = { if (password != confirm) { matchError = "Passwords do not match"; return@Button }; vm.register(username, password) }, enabled = state !is AuthState.Loading, modifier = Modifier.fillMaxWidth()) {
            if (state is AuthState.Loading) CircularProgressIndicator(Modifier.size(18.dp)) else Text("Create Account")
        }
        TextButton(onClick = onNavigateToLogin) { Text("Already have an account? Log In") }
    }
}
