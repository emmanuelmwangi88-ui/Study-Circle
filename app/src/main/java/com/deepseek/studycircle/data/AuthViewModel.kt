package com.deepseek.studycircle.data

import android.content.Context
import android.widget.Toast
import androidx.navigation.NavHostController
import com.deepseek.studycircle.models.User
import com.deepseek.studycircle.navigation.ROUTE_LOGIN
import com.deepseek.studycircle.navigation.ROUTE_REGISTER
import com.deepseek.studycircle.navigation.ROUTE_DASHBOARD
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

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

    // Login function - Simplified as UserViewModel handles data loading and bonuses
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(context, "Email and password cannot be blank", Toast.LENGTH_SHORT).show()
            return
        }
        
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                navController.navigate(ROUTE_DASHBOARD) {
                    popUpTo(ROUTE_LOGIN) { inclusive = true }
                }
            } else {
                Toast.makeText(context, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Logout function
    fun logout() {
        mAuth.signOut()
        navController.navigate(ROUTE_LOGIN) {
            popUpTo(0) { inclusive = true }
        }
    }
}
