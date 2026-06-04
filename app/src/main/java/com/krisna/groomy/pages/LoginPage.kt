package com.krisna.groomy.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import android.widget.Toast
import com.krisna.groomy.R
import com.krisna.groomy.api.RetrofitClient
import com.krisna.groomy.components.SocialLoginButton
import com.krisna.groomy.model.LoginRequest
import com.krisna.groomy.utils.PrefManager

@Composable
fun LoginPage(modifier: Modifier = Modifier, navController: NavController) {
    val currentContext = LocalContext.current
    val prefManager = remember { PrefManager(currentContext) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
    ) {
        // Luxury Decorative background elements
        Box(
            modifier = Modifier
                .size(450.dp)
                .offset(x = 180.dp, y = (-180).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF7DD3FC).copy(alpha = 0.25f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(225.dp)
                )
        )
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-150).dp, y = 500.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF257DEF).copy(alpha = 0.1f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(150.dp)
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Premium Logo Branding
            Surface(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF7DD3FC), Color(0xFF257DEF))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logogroomy),
                        contentDescription = "Groomy Logo",
                        modifier = Modifier.size(65.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Welcome Back",
                color = Color(0xFF0F172A),
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "Sign in to ",
                    color = Color(0xFF64748B),
                    fontSize = 16.sp
                )
                Text(
                    text = "Groomy",
                    color = Color(0xFF257DEF),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = null,
                    tint = Color(0xFF257DEF),
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(44.dp))

            // Form
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                placeholder = { Text("example@email.com") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Default.Email, 
                        contentDescription = null, 
                        tint = if (email.isNotEmpty()) Color(0xFF257DEF) else Color(0xFF94A3B8)
                    ) 
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF257DEF),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color(0xFFF8FAFC),
                    unfocusedContainerColor = Color(0xFFF8FAFC),
                    focusedLabelColor = Color(0xFF257DEF)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Default.Lock, 
                        contentDescription = null, 
                        tint = if (password.isNotEmpty()) Color(0xFF257DEF) else Color(0xFF94A3B8)
                    ) 
                },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    val description = if (passwordVisible) "Hide password" else "Show password"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description, tint = Color(0xFF94A3B8))
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF257DEF),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color(0xFFF8FAFC),
                    unfocusedContainerColor = Color(0xFFF8FAFC),
                    focusedLabelColor = Color(0xFF257DEF)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Forgot Password?",
                modifier = Modifier.align(Alignment.End),
                color = Color(0xFF257DEF),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(32.dp))

            // TOMBOL LOGIN DENGAN LOGIKA API
            Button(
                onClick = {
                    if (email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(currentContext, "Email dan password harus diisi", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true
                    scope.launch {
                        try {
                            val request = LoginRequest(email, password)
                            val response = RetrofitClient.instance.login(request)

                            if (response.isSuccessful) {
                                val loginResponse = response.body()
                                val token = loginResponse?.accessToken
                                
                                if (token != null) {
                                    prefManager.saveToken(token)
                                    Toast.makeText(currentContext, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                } else {
                                    Toast.makeText(currentContext, "Login Berhasil, tapi token tidak ditemukan", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                // Parsing error message dari body (misal: "Credentials incorrect")
                                val errorBody = response.errorBody()?.string()
                                val errorMsg = if (errorBody?.contains("\"message\":") == true) {
                                    errorBody.substringAfter("\"message\":\"").substringBefore("\"")
                                } else {
                                    response.message().ifEmpty { "Login Gagal (Error ${response.code()})" }
                                }
                                Toast.makeText(currentContext, errorMsg, Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(currentContext, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF7DD3FC), Color(0xFF257DEF))
                        )
                    ),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Login", 
                        fontSize = 18.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Social Login Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFF1F5F9), thickness = 1.dp)
                Text(
                    text = " Or sign in with ",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 12.dp),
                    fontWeight = FontWeight.Medium
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFF1F5F9), thickness = 1.dp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Social Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SocialLoginButton(
                    text = "Google",
                    icon = R.drawable.logogoogle,
                    modifier = Modifier.weight(1f),
                )
                SocialLoginButton(
                    text = "Apple",
                    icon = R.drawable.logoapple,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row {
                Text(text = "Don't have an account? ", color = Color(0xFF64748B), fontSize = 15.sp)
                Text(
                    text = "Sign Up",
                    color = Color(0xFF257DEF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.clickable { navController.navigate("signup") }
                )
            }
        }
    }
}
