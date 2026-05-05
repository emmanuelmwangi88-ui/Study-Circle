package com.deepseek.studycircle.data

import android.content.Context
import android.widget.Toast
import androidx.navigation.NavHostController
import com.deepseek.studycircle.models.CreditTransaction
import com.deepseek.studycircle.models.User
import com.deepseek.studycircle.navigation.ROUTE_LOGIN
import com.deepseek.studycircle.navigation.ROUTE_REGISTER
import com.deepseek.studycircle.navigation.ROUTE_DASHBOARD
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AuthViewModel(var navController: NavHostController, var context: Context) {

    private var mAuth = FirebaseAuth.getInstance()
    private val dbRef = FirebaseDatabase.getInstance().getReference("Users")
    private val transRef = FirebaseDatabase.getInstance().getReference("Transactions")

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
                        credits = 0, // Will be awarded on first login
                        isFirstLogin = true
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

    // Login function
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(context, "Email and password cannot be blank", Toast.LENGTH_SHORT).show()
            return
        }
        
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = mAuth.currentUser?.uid ?: return@addOnCompleteListener
                val userRef = dbRef.child(userId)
                
                userRef.get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val user = snapshot.getValue(User::class.java)
                        
                        if (user != null && user.isFirstLogin) {
                            // Give welcome bonus credits on first login
                            val bonus = CreditCalculator.WELCOME_BONUS
                            val updates = mapOf(
                                "credits" to bonus,
                                "isFirstLogin" to false
                            )
                            
                            userRef.updateChildren(updates).addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    // Record the transaction
                                    val tRef = transRef.child(userId)
                                    val transId = tRef.push().key ?: System.currentTimeMillis().toString()
                                    val transaction = CreditTransaction(
                                        id = transId,
                                        userId = userId,
                                        amount = bonus,
                                        type = CreditCalculator.TransactionType.SIGNUP_BONUS.name,
                                        timestamp = System.currentTimeMillis(),
                                        description = CreditCalculator.TransactionType.SIGNUP_BONUS.description
                                    )
                                    tRef.child(transId).setValue(transaction)
                                    Toast.makeText(context, "Welcome! +$bonus credits awarded!", Toast.LENGTH_LONG).show()
                                }
                                navController.navigate(ROUTE_DASHBOARD) {
                                    popUpTo(ROUTE_LOGIN) { inclusive = true }
                                }
                            }
                        } else {
                            Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                            navController.navigate(ROUTE_DASHBOARD) {
                                popUpTo(ROUTE_LOGIN) { inclusive = true }
                            }
                        }
                    } else {
                        // If user auth exists but DB record doesn't (rare)
                        navController.navigate(ROUTE_DASHBOARD)
                    }
                }.addOnFailureListener {
                    // Database disconnected or rules issue, but allow entry
                    navController.navigate(ROUTE_DASHBOARD)
                }
            } else {
                Toast.makeText(context, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Profile function
    fun getUserData(callback: (User?) -> Unit) {
        val userId = mAuth.currentUser?.uid ?: return callback(null)
        dbRef.child(userId).get().addOnSuccessListener { snapshot ->
            callback(snapshot.getValue(User::class.java))
        }.addOnFailureListener {
            callback(null)
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
