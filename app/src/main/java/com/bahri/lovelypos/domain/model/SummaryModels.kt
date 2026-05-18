package com.bahri.lovelypos.domain.model

data class ItemSalesSummary(
    val menuItemName: String,
    val totalQty: Int,
    val totalRevenue: Long
)

data class PaymentMethodSummary(
    val paymentMethod: String,
    val count: Int,
    val total: Long
)

data class DailyRevenue(
    val dateLabel: String,
    val total: Long,
    val count: Int
)

data class SummaryReport(
    val totalRevenue: Long,
    val transactionCount: Int,
    val topItems: List<ItemSalesSummary>,
    val paymentBreakdown: List<PaymentMethodSummary>,
    val dailyRevenue: List<DailyRevenue>
)
