package com.example.tonezone.splashscreen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import com.example.tonezone.MainViewModel
import com.example.tonezone.MainViewModelFactory
import com.example.tonezone.R

class SplashScreenFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels {
        MainViewModelFactory(requireActivity())
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mainViewModel.initAuthorization()
        return inflater.inflate(R.layout.fragment_splash_screen, container, false)
    }

}