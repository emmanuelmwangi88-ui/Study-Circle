package com.deepseek.studycircle.data

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavHostController
import com.deepseek.studycircle.models.User
import com.deepseek.studycircle.navigation.ROUTE_LOGIN
import com.deepseek.studycircle.navigation.ROUTE_REGISTER
import com.deepseek.studycircle.navigation.ROUTE_DASHBOARD
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import io.agora.chat.ChatClient
import io.agora.CallBack

class AuthViewModel(var navController: NavHostController, var context: Context) {

    private var mAuth = FirebaseAuth.getInstance()
    private val dbRef = FirebaseDatabase.getInstance().getReference("users")

    // Register function
    fun signup(fullname: String, email: String, password: String, confirmpass: String) {
        if (fullname.isBlank() || email.isBlank() || password.isBlank() || confirmpass.isBlank()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (password != confirmpass) {
            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = mAuth.currentUser?.uid ?: return@addOnCompleteListener
                    val userData = User(
                        name = fullname,
                        email = email,
                        uid = userId,
                        role = "user",
                        credits = 0, 
                        isFirstLogin = true,
                        lastLogin = System.currentTimeMillis()
                    )
                    
                    dbRef.child(userId).setValue(userData).addOnCompleteListener { innerTask ->
                        if (innerTask.isSuccessful) {
                            // Register to Agora Chat (Simplified: using uid as username and password)
                            // Note: In production, use tokens and server-side registration.
                            registerAgoraChat(userId, userId)
                            
                            Toast.makeText(context, "Registration successful!", Toast.LENGTH_LONG).show()
                            navController.navigate(ROUTE_LOGIN) {
                                popUpTo(ROUTE_REGISTER) { inclusive = true }
                            }
                        } else {
                            Toast.makeText(context, "Database error: ${innerTask.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Auth error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun registerAgoraChat(username: String, pass: String) {
        Thread {
            try {
                ChatClient.getInstance().createAccount(username, pass)
                Log.d("AgoraChat", "Registration successful")
            } catch (e: Exception) {
                Log.e("AgoraChat", "Registration failed: ${e.message}")
            }
        }.start()
    }

    // Login function
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(context, "Email and password cannot be blank", Toast.LENGTH_SHORT).show()
            return
        }
        
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = mAuth.currentUser?.uid ?: ""
                
                // Login to Agora Chat
                ChatClient.getInstance().login(userId, userId, object : CallBack {
                    override fun onSuccess() {
                        ChatClient.getInstance().chatManager().loadAllConversations()
                        Log.d("AgoraChat", "Login successful")
                    }

                    override fun onError(code: Int, error: String?) {
                        Log.e("AgoraChat", "Login failed: $error (code: $code)")
                        // If user not found, try to register then login
                        if (code == 204 || code == 208) {
                           registerAndLoginAgora(userId, userId)
                        }
                    }

                    override fun onProgress(progress: Int, status: String?) {}
                })

                Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                navController.navigate(ROUTE_DASHBOARD) {
                    popUpTo(ROUTE_LOGIN) { inclusive = true }
                }
            } else {
                Toast.makeText(context, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun registerAndLoginAgora(userId: String, pass: String) {
        Thread {
            try {
                ChatClient.getInstance().createAccount(userId, pass)
                ChatClient.getInstance().login(userId, pass, object : CallBack {
                    override fun onSuccess() { Log.d("AgoraChat", "Retry login successful") }
                    override fun onError(code: Int, error: String?) { Log.e("AgoraChat", "Retry login failed") }
                    override fun onProgress(p0: Int, p1: String?) {}
                })
            } catch (e: Exception) {}
        }.start()
    }

    // Logout function
    fun logout() {
        mAuth.signOut()
        ChatClient.getInstance().logout(true)
        navController.navigate(ROUTE_LOGIN) {
            popUpTo(0) { inclusive = true }
        }
    }
}
