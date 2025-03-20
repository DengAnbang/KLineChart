package com.github.fujianlian.klinechart.entity

import com.github.fujianlian.klinechart.KLineOrderDataHelper
import com.github.fujianlian.klinechart.KLineOrderDataHelper.Companion.SIDE_CLOSE_LONG
import com.github.fujianlian.klinechart.KLineOrderDataHelper.Companion.SIDE_CLOSE_SHORT
import com.github.fujianlian.klinechart.KLineOrderDataHelper.Companion.SIDE_OPEN_LONG
import com.github.fujianlian.klinechart.utils.UiUtils

/**
 * 交易订单数据
 */
data class KLineOrderEntity(
    var symbol: String? = null,
    var orderType: Int = 0,
    var price: String? = "0",
    var highPrice: String? = "0",
    var lowPrice: String? = "0",
    var profit: String? = "0",
    var side: Int = 0,
    var createTime: Long = 0,
    var leftTitle: String? = null,
    var leftHighTitle: String? = null,
    var leftLowTitle: String? = null,
    var titlePrefix: String? = "",
    var highTitlePrefix: String? = "",
    var lowTitlePrefix: String? = "",
    var vol: String? = "",
    var highVol: String? = "",
    var lowVol: String? = ""
) {

    fun isPositionOrder(): Boolean {
        return orderType == KLineOrderDataHelper.ORDER_TYPE_POSITION
    }

    fun isLong(): Boolean {
        return side == SIDE_OPEN_LONG || side == SIDE_CLOSE_LONG
    }

    fun isBuy(): Boolean {
        return side == SIDE_OPEN_LONG || side == SIDE_CLOSE_SHORT
    }

    fun parseLeftTitle(formattedVol: String): String {
        leftTitle?.let {
            return it
        }
        return when (orderType) {
            KLineOrderDataHelper.ORDER_TYPE_OPEN -> {
                if (UiUtils.isRtl()) "$formattedVol $titlePrefix" else "$titlePrefix $formattedVol"
            }

            KLineOrderDataHelper.ORDER_TYPE_PLAN -> {
                if (UiUtils.isRtl()) "$formattedVol $titlePrefix" else "$titlePrefix $formattedVol"
            }

            else -> {
                ""
            }
        }
    }

    fun parsePNLTitle(formattedProfit: String): String {
        val prefix = if (getProfitFloat() > 0) "+" else ""
        return "$titlePrefix $prefix$formattedProfit"
    }

    fun parseLeftHighTitle(formattedHighVol: String): String {
        leftHighTitle?.let {
            return it
        }
        return when (orderType) {
            KLineOrderDataHelper.ORDER_TYPE_STOP -> {
                if (UiUtils.isRtl()) "$formattedHighVol $highTitlePrefix" else "$highTitlePrefix $formattedHighVol"
            }

            else -> {
                ""
            }
        }
    }

    fun getDisplayTpPrice(): String {
        val infix = if (isLong()) "≥" else "≤"
        return "$infix$highPrice"
    }

    fun getDisplayLsPrice(): String {
        val infix = if (isLong()) "≤" else "≥"
        return "$infix$lowPrice"
    }

    fun parseLeftLowTitle(formattedLowVol: String): String {
        leftLowTitle?.let {
            return it
        }
        return when (orderType) {
            KLineOrderDataHelper.ORDER_TYPE_STOP -> {
                if (UiUtils.isRtl()) "$formattedLowVol $lowTitlePrefix" else "$lowTitlePrefix $formattedLowVol"
            }

            else -> {
                ""
            }
        }
    }

    fun getPriceFloat(): Float {
        price ?: return 0f
        return price!!.toFloat()
    }

    fun getHighPriceFloat(): Float {
        highPrice ?: return 0f
        return highPrice!!.toFloat()
    }

    fun getLowPriceFloat(): Float {
        lowPrice ?: return 0f
        return lowPrice!!.toFloat()
    }

    fun getProfitFloat(): Float {
        profit ?: return 0f
        return profit!!.toFloat()
    }

}