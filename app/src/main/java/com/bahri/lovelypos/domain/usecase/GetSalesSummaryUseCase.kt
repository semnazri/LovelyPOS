package com.bahri.lovelypos.domain.usecase

import com.bahri.lovelypos.domain.model.SummaryReport
import com.bahri.lovelypos.domain.repository.TransactionRepository

class GetSalesSummaryUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(startMs: Long, endMs: Long): SummaryReport {
        return repository.getSalesSummary(startMs, endMs)
    }
}
