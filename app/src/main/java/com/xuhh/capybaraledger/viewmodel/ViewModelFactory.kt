package com.xuhh.capybaraledger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.xuhh.capybaraledger.data.repository.BillRepository
import com.xuhh.capybaraledger.data.repository.LedgerRepository

// ViewModelFactory.kt
class ViewModelFactory(
    private val ledgerRepo: LedgerRepository,
    private val billRepo: BillRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(BillViewModel::class.java) ->
                BillViewModel(ledgerRepo, billRepo) as T
            else ->
                throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}