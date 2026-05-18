// MODIFIED — Milestone 6
package com.bahri.lovelypos.domain.usecase

import com.bahri.lovelypos.domain.model.SummaryReport
import com.bahri.lovelypos.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetSalesSummaryUseCase(private val repository: TransactionRepository) {
    operator fun invoke(startMs: Long, endMs: Long): Flow<SummaryReport> {
        return repository.getSalesSummary(startMs, endMs)
    }
}
