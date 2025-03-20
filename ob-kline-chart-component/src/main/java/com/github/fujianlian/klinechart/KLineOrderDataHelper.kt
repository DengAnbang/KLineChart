package com.github.fujianlian.klinechart

import android.util.Pair
import com.github.fujianlian.klinechart.entity.HistorySignEntity
import com.github.fujianlian.klinechart.entity.KLineOrderEntity

/**
 * 管理K线中的订单数据
 */
class KLineOrderDataHelper(callback: OrderDataCallback) {

    private var symbol: String? = null
    private var historyOrderCallback: HistoryOrderCallback? = null
    private var dataCallback: OrderDataCallback
    private var historyOrderList: List<KLineOrderEntity>? = null
    private var liquidationOrderList: List<KLineOrderEntity>? = null

    init {
        dataCallback = callback
    }

    /**
     * 更新交易对
     */
    fun updateSymbol(newSymbol: String?) {
        symbol = newSymbol
    }

    fun setHistoryOrderCallback(callback: HistoryOrderCallback) {
        historyOrderCallback = callback
    }

    /**
     * 获取当前委托数据
     */
    fun getOpenOrderData(): List<KLineOrderEntity>? {
        symbol ?: return null
        return dataCallback.getOpenOrderList(symbol!!)
    }

    /**
     * 获取当前持仓数据
     */
    fun getPositionOrderData(): List<KLineOrderEntity>? {
        symbol ?: return null
        return dataCallback.getPositionOrderList(symbol!!)
    }

    /**
     * 获取止盈止损数据
     */
    fun getStopLimitOrderData(): List<KLineOrderEntity>? {
        symbol ?: return null
        return dataCallback.getStopLimitOrderList(symbol!!)
    }

    /**
     * 获取计划单数据
     */
    fun getPlanOrderData(): List<KLineOrderEntity>? {
        symbol ?: return null
        return dataCallback.getPlanOrderList(symbol!!)
    }

    /**
     * 获取历史订单信息
     * @param startDate 开始日期（毫秒）
     * @param endDate 结束日期（毫秒）
     * @param isSameFrame 是否正在进行同一帧的绘制，确保同一帧绘制过程中只取一次数据，以保证性能
     */
    fun getHistoryInfoByDateRange(startDate: Long,
                                  endDate: Long,
                                  isSameFrame: Boolean): Pair<HistorySignEntity?, HistorySignEntity?>? {
        symbol ?: return null
        if (historyOrderList == null || !isSameFrame) {
            historyOrderList = historyOrderCallback?.getHistoryOrderList(symbol!!)
        }
        if (historyOrderList.isNullOrEmpty()) {
            return null
        }
        val buyList = historyOrderList!!.filter { it.createTime in startDate until endDate && it.isBuy() }
        val sellList = historyOrderList!!.filter { it.createTime in startDate until endDate && !it.isBuy() }
        var buyEntity: HistorySignEntity? = null
        var sellEntity: HistorySignEntity? = null
        if (buyList.isNotEmpty()) {
            // 时间角度的最后一笔订单为接口返回的第一个
            val lastBuy = buyList.first()
            buyEntity = HistorySignEntity(LABEL_BUY, lastBuy.titlePrefix!!, lastBuy.vol!!,
                lastBuy.price ?: "", buyList.size, lastBuy.isBuy(), lastBuy.createTime)
        }
        if (sellList.isNotEmpty()) {
            // 时间角度的最后一笔订单为接口返回的第一个
            val lastSell = sellList.first()
            sellEntity = HistorySignEntity(LABEL_SELL, lastSell.titlePrefix!!, lastSell.vol!!,
                lastSell.price ?: "", sellList.size, lastSell.isBuy(), lastSell.createTime)
        }
        if (buyEntity == null && sellEntity == null) {
            return null
        }
        return Pair(buyEntity, sellEntity)
    }

    fun getLiquidationInfoByDateRange(startDate: Long,
                                  endDate: Long,
                                  isSameFrame: Boolean): Pair<HistorySignEntity?, HistorySignEntity?>? {
        symbol ?: return null
        if (liquidationOrderList == null || !isSameFrame) {
            liquidationOrderList = historyOrderCallback?.getLiquidationOrderList(symbol!!)
        }
        if (liquidationOrderList.isNullOrEmpty()) {
            return null
        }
        val buyList = liquidationOrderList!!.filter { it.createTime in startDate until endDate && it.isBuy() }
        val sellList = liquidationOrderList!!.filter { it.createTime in startDate until endDate && !it.isBuy() }
        var buyEntity: HistorySignEntity? = null
        var sellEntity: HistorySignEntity? = null
        if (buyList.isNotEmpty()) {
            // 时间角度的最后一笔订单为接口返回的第一个
            val lastBuy = buyList.first()
            buyEntity = HistorySignEntity(LABEL_LIQ, lastBuy.titlePrefix!!, lastBuy.vol!!,
                lastBuy.price ?: "", buyList.size, lastBuy.isBuy(), lastBuy.createTime)
        }
        if (sellList.isNotEmpty()) {
            // 时间角度的最后一笔订单为接口返回的第一个
            val lastSell = sellList.first()
            sellEntity = HistorySignEntity(LABEL_LIQ, lastSell.titlePrefix!!, lastSell.vol!!,
                lastSell.price ?: "", sellList.size, lastSell.isBuy(), lastSell.createTime)
        }
        if (buyEntity == null && sellEntity == null) {
            return null
        }
        return Pair(buyEntity, sellEntity)
    }

    /**
     * 连接到ContractDataUtils的中间层，用来获取相关数据
     */
    interface OrderDataCallback {
        fun getOpenOrderList(symbol: String): List<KLineOrderEntity>?
        fun getPositionOrderList(symbol: String): List<KLineOrderEntity>?
        fun getStopLimitOrderList(symbol: String): List<KLineOrderEntity>?
        fun getPlanOrderList(symbol: String): List<KLineOrderEntity>?
    }

    /**
     * 连接到ContractKLinePresenter的中间层，用来获取历史订单数据
     */
    interface HistoryOrderCallback {
        fun getHistoryOrderList(symbol: String): List<KLineOrderEntity>?
        fun getLiquidationOrderList(symbol: String): List<KLineOrderEntity>?
    }

    companion object {
        // 当前委托
        const val ORDER_TYPE_OPEN = 1
        // 当前持仓
        const val ORDER_TYPE_POSITION = 2
        // 历史委托
        const val ORDER_TYPE_HISTORY = 3
        // 止盈止损
        const val ORDER_TYPE_STOP = 4
        // 计划订单
        const val ORDER_TYPE_PLAN = 5
        // 强制平仓
        const val ORDER_TYPE_LIQUIDATION = 6
        // 开多
        const val SIDE_OPEN_LONG = 1
        // 平空
        const val SIDE_CLOSE_SHORT = 2
        // 开空
        const val SIDE_OPEN_SHORT = 3
        // 平多
        const val SIDE_CLOSE_LONG = 4
        // 当前持仓盈亏
        const val HOLD_POSITION_PNL = "PNL"
        // 历史订单买卖标签
        private const val LABEL_BUY = "B"
        private const val LABEL_SELL = "S"
        private const val LABEL_LIQ = "L"
    }
}