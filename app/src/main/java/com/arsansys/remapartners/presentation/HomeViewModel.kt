package com.arsansys.remapartners.presentation

import androidx.lifecycle.ViewModel
import com.arsansys.remapartners.data.service.UserService
import javax.inject.Inject

class HomeViewModel @Inject constructor(private val userService: UserService):
ViewModel() {

    fun getUsers() {
        // Implement the logic to get users from the userService
    }
}