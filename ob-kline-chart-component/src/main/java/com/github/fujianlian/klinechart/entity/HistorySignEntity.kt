package com.github.fujianlian.klinechart.entity

/**
 * K线信息栏中的历史订单数据
 */
data class HistorySignEntity(
    val label: String,
    val prefix: String,
    val vol: String,
    val price: String,
    var orderCount: Int,
    val isBuy: Boolean,
    val time: Long
) {
    fun getDisplayInfo(): String {
        val orderCountStr = if (orderCount > 1) " $orderCount+" else ""
        return "$prefix $vol@ $price$orderCountStr"
    }
}
