package dz.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dz.designsystem.theme.DentalDashboardTheme

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    isFirstRunSetup: Boolean = false,
    initialUsername: String = "",
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onSubmitLogin: (username: String, password: String) -> Unit = { _, _ -> },
    onSubmitSetup: (username: String, password: String) -> Unit = { _, _ -> }
) {
    var username by remember(initialUsername) { mutableStateOf(initialUsername) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val displayedError = localError ?: errorMessage

    fun handleSubmit() {
        val trimmedUser = username.trim()
        if (trimmedUser.length < 3) {
            localError = "Le nom d'utilisateur doit contenir au moins 3 caractères."
            return
        }
        if (password.length < 6) {
            localError = "Le mot de passe doit contenir au moins 6 caractères."
            return
        }
        if (isFirstRunSetup && password != confirmPassword) {
            localError = "Les mots de passe ne correspondent pas."
            return
        }
        localError = null
        if (isFirstRunSetup) {
            onSubmitSetup(trimmedUser, password)
        } else {
            onSubmitLogin(trimmedUser, password)
        }
    }

    DentalDashboardTheme(darkTheme = true) {
        val backdropBrush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF141414),
                Color(0xFF1E242B),
                Color(0xFF141414)
            )
        )

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(backdropBrush)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF23252A),
                border = BorderStroke(1.dp, Color(0xFFD4AF38).copy(alpha = 0.35f)),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = Color(0xFFD4AF38).copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, Color(0xFFD4AF38).copy(alpha = 0.5f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = "Cabinet Dentaire",
                                tint = Color(0xFFD4AF38),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "CABINET DENTAIRE",
                            color = Color(0xFFD4AF38),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (isFirstRunSetup) "Initialisation Administrateur" else "Connexion Praticien",
                            color = Color(0xFFE8E2DC),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (isFirstRunSetup) {
                                "Définissez le mot de passe du compte administrateur (tous droits d'édition) ou configurez ADMIN_BOOTSTRAP_PASSWORD dans les Secrets."
                            } else {
                                "Veuillez saisir vos identifiants pour accéder au tableau de bord."
                            },
                            color = Color(0xFFA0A5AD),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }

                    AnimatedVisibility(visible = displayedError != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFB32630).copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, Color(0xFFB32630).copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = displayedError.orEmpty(),
                                color = Color(0xFFF5D5C7),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            localError = null
                        },
                        label = { Text("Nom d'utilisateur") },
                        placeholder = { Text("ex: dr.bouzid") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = "Nom d'utilisateur")
                        },
                        singleLine = true,
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD4AF38),
                            unfocusedBorderColor = Color(0xFF474B54),
                            focusedLabelColor = Color(0xFFD4AF38),
                            unfocusedLabelColor = Color(0xFFA0A5AD),
                            focusedTextColor = Color(0xFFE8E2DC),
                            unfocusedTextColor = Color(0xFFE8E2DC),
                            focusedLeadingIconColor = Color(0xFFD4AF38),
                            unfocusedLeadingIconColor = Color(0xFFA0A5AD)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_username_input")
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            localError = null
                        },
                        label = { Text("Mot de passe") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Mot de passe")
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("login_toggle_password")
                            ) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "Masquer le mot de passe" else "Afficher le mot de passe"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = if (isFirstRunSetup) ImeAction.Next else ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { if (!isFirstRunSetup) handleSubmit() }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD4AF38),
                            unfocusedBorderColor = Color(0xFF474B54),
                            focusedLabelColor = Color(0xFFD4AF38),
                            unfocusedLabelColor = Color(0xFFA0A5AD),
                            focusedTextColor = Color(0xFFE8E2DC),
                            unfocusedTextColor = Color(0xFFE8E2DC),
                            focusedLeadingIconColor = Color(0xFFD4AF38),
                            unfocusedLeadingIconColor = Color(0xFFA0A5AD),
                            focusedTrailingIconColor = Color(0xFFD4AF38),
                            unfocusedTrailingIconColor = Color(0xFFA0A5AD)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input")
                    )

                    if (isFirstRunSetup) {
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                localError = null
                            },
                            label = { Text("Confirmer le mot de passe") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = "Confirmer le mot de passe")
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            enabled = !isLoading,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { handleSubmit() }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFD4AF38),
                                unfocusedBorderColor = Color(0xFF474B54),
                                focusedLabelColor = Color(0xFFD4AF38),
                                unfocusedLabelColor = Color(0xFFA0A5AD),
                                focusedTextColor = Color(0xFFE8E2DC),
                                unfocusedTextColor = Color(0xFFE8E2DC),
                                focusedLeadingIconColor = Color(0xFFD4AF38),
                                unfocusedLeadingIconColor = Color(0xFFA0A5AD)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_confirm_password_input")
                        )
                    }

                    Button(
                        onClick = { handleSubmit() },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD4AF38),
                            contentColor = Color(0xFF141414)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("login_submit_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF141414)
                            )
                        } else {
                            Text(
                                text = if (isFirstRunSetup) "Créer et se connecter" else "Se connecter",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = "Mode hors ligne",
                            tint = Color(0xFF6DA56A),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Authentification locale chiffrée (PBKDF2)",
                            color = Color(0xFF6DA56A),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
