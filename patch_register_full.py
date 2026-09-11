import re

with open("app/src/main/java/com/example/ui/screens/AuthScreens.kt", "r") as f:
    content = f.read()

parts = content.split("@Composable\nfun PublicRegisterScreen")
if len(parts) < 2:
    print("Could not find PublicRegisterScreen")
    exit(1)

head = parts[0]

new_register_screen = """@Composable
fun PublicRegisterScreen(navController: NavController, authViewModel: AuthViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    if (authState is AuthState.Success) {
        SuccessAnimationDialog(
            title = "Registration Submitted",
            message = "Your account has been created successfully.\\nYour account is waiting for Admin approval.\\n\\nStatus: PENDING ADMIN APPROVAL",
            onDismiss = {
                navController.navigateUp()
                authViewModel.logout()
            }
        )
    }

    if (authState is AuthState.Error) {
        ErrorDialog(
            message = (authState as AuthState.Error).message,
            onDismiss = { authViewModel.resetState() }
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = LoginBackgroundGradient)
    ) {
        TopAppBar(
            title = { Text("") },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(30.dp))
                    .background(GlassContainerColor)
                    .border(1.dp, GlassBorderColor, RoundedCornerShape(30.dp))
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(24.dp))

                GlassTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Full Name",
                    icon = Icons.Default.Person
                )
                Spacer(modifier = Modifier.height(16.dp))
                GlassTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    icon = Icons.Default.Email
                )
                Spacer(modifier = Modifier.height(16.dp))
                GlassTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = "Mobile Number",
                    icon = Icons.Default.Phone
                )
                Spacer(modifier = Modifier.height(16.dp))
                GlassTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    icon = Icons.Default.Lock,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onPasswordVisibilityChange = { passwordVisible = !passwordVisible }
                )
                Spacer(modifier = Modifier.height(16.dp))
                GlassTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirm Password",
                    icon = Icons.Default.Lock,
                    isPassword = true,
                    passwordVisible = confirmPasswordVisible,
                    onPasswordVisibilityChange = { confirmPasswordVisible = !confirmPasswordVisible }
                )
                Spacer(modifier = Modifier.height(32.dp))
                
                GlassLoginButton(
                    text = "Create Account",
                    onClick = {
                        if (name.isBlank()) {
                            Toast.makeText(context, "Please enter your full name", Toast.LENGTH_SHORT).show()
                            return@GlassLoginButton
                        }
                        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            Toast.makeText(context, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                            return@GlassLoginButton
                        }
                        if (mobile.isBlank() || !android.util.Patterns.PHONE.matcher(mobile).matches()) {
                            Toast.makeText(context, "Please enter a valid mobile number", Toast.LENGTH_SHORT).show()
                            return@GlassLoginButton
                        }
                        if (password.isBlank()) {
                            Toast.makeText(context, "Please enter your password", Toast.LENGTH_SHORT).show()
                            return@GlassLoginButton
                        }
                        if (confirmPassword.isBlank()) {
                            Toast.makeText(context, "Please confirm your password", Toast.LENGTH_SHORT).show()
                            return@GlassLoginButton
                        }
                        if (password != confirmPassword) {
                            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                            return@GlassLoginButton
                        }
                        authViewModel.register(name.trim(), email.trim(), mobile.trim(), password)
                    },
                    isLoading = authState is AuthState.Loading,
                    showSuccess = authState is AuthState.Success
                )
            }
        }
    }
}
"""

with open("app/src/main/java/com/example/ui/screens/AuthScreens.kt", "w") as f:
    f.write(head + new_register_screen)
