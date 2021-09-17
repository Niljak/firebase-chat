package com.niljak.firebasechat.views

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.niljak.firebasechat.R
import com.niljak.firebasechat.databinding.FragmentAuthBinding
import com.niljak.firebasechat.databinding.FragmentSignInBinding
import com.niljak.firebasechat.databinding.FragmentSignUpBinding
import com.niljak.firebasechat.viewmodels.AuthViewModel
import com.niljak.firebasechat.viewmodels.ValidationError

class AuthView : BoundFragment<FragmentAuthBinding>(
    R.layout.fragment_auth,
    FragmentAuthBinding::inflate
) {
    enum class Tab {
        SignIn, SignUp;

        companion object {
            fun fromPosition(position: Int): Tab? {
                return if (position < 0 || position >= values().size) {
                    null
                } else {
                    values()[position]
                }
            }
        }
    }

    private val adapter by lazy { AuthViewAdapter(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabs, binding.viewPager) { tab, position ->
            val selectedTab = Tab.fromPosition(position) ?: return@TabLayoutMediator
            when (selectedTab) {
                Tab.SignIn -> tab.text = getString(R.string.sign_in)
                Tab.SignUp -> tab.text = getString(R.string.sign_up)
            }
        }.attach()
    }
}

private class AuthViewAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = AuthView.Tab.values().size

    override fun createFragment(position: Int): Fragment =
        when (AuthView.Tab.fromPosition(position)) {
            AuthView.Tab.SignIn -> SignInFragment()
            AuthView.Tab.SignUp -> SignUpFragment()
            null -> throw RuntimeException("Invalid position for adapter.")
        }
}

class SignInFragment : BoundFragment<FragmentSignInBinding>(
    R.layout.fragment_sign_in,
    FragmentSignInBinding::inflate
) {
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        authViewModel.errors.observe(viewLifecycleOwner, { errors ->
            binding.email.error = errors.mapToErrorText(ValidationError.MissingEmail, getString(R.string.missing_email))
            binding.password.error = errors.mapToErrorText(ValidationError.MissingPassword, getString(R.string.missing_password))
        })

        binding.signIn.setOnClickListener {
            authViewModel.signIn(binding.email.text.toString(), binding.password.text.toString())
        }
    }
}

class SignUpFragment : BoundFragment<FragmentSignUpBinding>(
    R.layout.fragment_sign_up,
    FragmentSignUpBinding::inflate
) {
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        authViewModel.errors.observe(viewLifecycleOwner, { errors ->
            binding.username.error = errors.mapToErrorText(ValidationError.MissingUsername, getString(R.string.missing_username))
            binding.email.error = errors.mapToErrorText(ValidationError.MissingEmail, getString(R.string.missing_email))
            if (errors.contains(ValidationError.PasswordTooShort)) {
                binding.password.error = getString(R.string.password_too_short)
            } else {
                binding.password.error = errors.mapToErrorText(ValidationError.MissingPassword, getString(R.string.missing_password))
            }
            binding.repeatPassword.error = errors.mapToErrorText(ValidationError.MismatchedPasswords, getString(R.string.password_does_not_match))
        })
        binding.signUp.setOnClickListener {
            authViewModel.signUp(
                username = binding.username.text.toString(),
                email = binding.email.text.toString(),
                password = binding.password.text.toString(),
                repeatPassword = binding.repeatPassword.text.toString()
            )
        }
    }
}

private fun Set<ValidationError>.mapToErrorText(error: ValidationError, text: String): String? =
    if (contains(error)) {
        text
    } else {
        null
    }