package com.arsansys.remapartners.di

import com.arsansys.remapartners.presentation.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val AppModule = module {

    viewModel { HomeViewModel(get()) }
}
