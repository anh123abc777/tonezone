package com.example.tonezone.register

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.tonezone.databinding.FragmentRegisterBinding
import com.example.tonezone.network.FirebaseRepository
import com.example.tonezone.network.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class RegisterFragment : Fragment() {

    private lateinit var binding : FragmentRegisterBinding
    private val mAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val firebaseRepo = FirebaseRepository()
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRegisterBinding.inflate(inflater)

        binding.register.setOnClickListener{
//            startActivity(Intent(context,RegisterFragment::class.java))
            registerUser()
        }

        return binding.root
    }

    private fun registerUser(){

        if (binding.editTextFullName.text.isEmpty()){
            binding.editTextFullName.error = "Full name is required"
            binding.editTextFullName.requestFocus()
            return
        }

        if (binding.email.text.isEmpty()){
            binding.email.error = "Email is required!"
            binding.email.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(binding.email.text).matches()){
            binding.email.error = "Please provide valid email!"
            binding.email.requestFocus()
            return
        }

        if (binding.password.text.isEmpty()){
            binding.password.error = "Password is required!"
            binding.password.requestFocus()
            return
        }

        if (binding.password.text.isEmpty()){
            binding.password.error = "Password is required!"
            binding.password.requestFocus()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        mAuth.createUserWithEmailAndPassword(
                binding.email.text.toString(),
                binding.password.text.toString(),
            )
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    val registeredUser = task.result.user
                    val user = User(
                        id= registeredUser!!.uid,
                        display_name = binding.editTextFullName.text.toString()!!,
                        email = registeredUser.email!!
                    )

                    registerUserOnFirebase(user)

                }else{
                    Toast.makeText(context,"Failed to register! Try again!",Toast.LENGTH_LONG).show()
                    binding.progressBar.visibility = View.GONE
                }

            }

    }

    private fun registerUserOnFirebase(user: User){
        db.collection("User")
            .document()
            .set(user)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Toast.makeText(context,"User has been registered",Toast.LENGTH_LONG).show()
                    binding.progressBar.visibility = View.GONE

                }else{
                    Toast.makeText(context,"Failed to register! Try again!",Toast.LENGTH_LONG).show()
                    binding.progressBar.visibility = View.GONE
                }
            }

        db.collection("User")
            .whereEqualTo("id",user.id)
            .addSnapshotListener { documents, _ ->
                if (documents!=null){
                    for (doc in documents){
                        user.id = doc.id
                        db.collection("User")
                            .document(doc.id)
                            .set(user)
                    }
                }
            }
    }

}