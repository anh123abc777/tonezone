package com.example.tonezone.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.tonezone.MainViewModel
import com.example.tonezone.MainViewModelFactory
import com.example.tonezone.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels{
        MainViewModelFactory(requireActivity())
    }

    private val viewModel: LoginViewModel by viewModels{
        LoginViewModelFactory(requireActivity().application)
    }

    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentLoginBinding.inflate(inflater)
        binding.viewModel = viewModel

        binding.lifecycleOwner = viewLifecycleOwner

        handleCheckAuth()
        handleNavigateToRegister()
        handleLogin()

        return binding.root
    }

    private fun handleLogin(){
        binding.buttonLogin.setOnClickListener {
            login()
        }
    }

    private fun login(){
       if (binding.editTextTextPersonName.text.isEmpty()){
           binding.editTextTextPersonName.error = "User is required"
           binding.editTextTextPersonName.requestFocus()
           return
       }

        if (binding.editTextTextPassword.text.isEmpty()){
            binding.editTextTextPassword.error = "Password is required!"
            binding.editTextTextPassword.requestFocus()
            return
        }

        viewModel.login()
    }

    private fun handleCheckAuth(){
        viewModel.isLoggingIn.observe(viewLifecycleOwner){
            if (it){
                mainViewModel.checkUser(
                    binding.editTextTextPersonName.text.toString(),
                    binding.editTextTextPassword.text.toString())
                viewModel.checkUserComplete()

            }
        }
    }

    private fun handleNavigateToRegister(){
        viewModel.registerUser.observe(viewLifecycleOwner){
            if (it!=null){
                findNavController().navigate(
                    LoginFragmentDirections
                        .actionLoginFragmentToRegisterFragment())
                viewModel.navigateRegisterComplete()
            }
        }
    }


}