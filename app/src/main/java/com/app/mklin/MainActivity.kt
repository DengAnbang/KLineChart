package com.app.mklin

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.app.mklin.ui.theme.MKlinTheme
import com.github.fujianlian.klinechart.ConfigController
import com.github.fujianlian.klinechart.DataHelper
import com.github.fujianlian.klinechart.KChartConstant.Main.BOLL
import com.github.fujianlian.klinechart.KChartConstant.Main.EMA
import com.github.fujianlian.klinechart.KChartConstant.Main.MA
import com.github.fujianlian.klinechart.KChartConstant.Sub.OBV
import com.github.fujianlian.klinechart.KChartConstant.Sub.VOL
import com.github.fujianlian.klinechart.KLineChartAdapter
import com.github.fujianlian.klinechart.KLineChartView
import com.github.fujianlian.klinechart.KLineEntity
import com.github.fujianlian.klinechart.KLineOrderDataHelper
import com.github.fujianlian.klinechart.KLineOrderDataHelper.Companion.ORDER_TYPE_OPEN
import com.github.fujianlian.klinechart.KLineOrderDataHelper.Companion.SIDE_OPEN_LONG
import com.github.fujianlian.klinechart.entity.KChartConfig
import com.github.fujianlian.klinechart.entity.KLineOrderEntity
import com.github.fujianlian.klinechart.entity.KLineSelectorEntity
import com.github.fujianlian.klinechart.entity.LocalValues
import com.github.fujianlian.klinechart.formatter.ValueFormatter
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            MKlinTheme {
//                Greeting()
//            }
//        }

        setContentView(R.layout.layout_kline)
        ConfigController.getInstance().localValues = LocalValues(getString(R.string.time),
                                                                getString(R.string.open),
                                                                getString(R.string.high),
                                                                getString(R.string.low),
                                                                getString(R.string.close),
                                                                getString(R.string.change),
                                                                getString(R.string.change_rate),
                                                                getString(R.string.volume),
                                                                getString(R.string.amount),
                                                                getString(R.string.range),
                                                                getString(R.string.order))

        val orderDataHelper by lazy {
            KLineOrderDataHelper(object : KLineOrderDataHelper.OrderDataCallback {
                override fun getOpenOrderList(symbol: String): List<KLineOrderEntity>? {
//                    return emptyList()
                    return listOf(KLineOrderEntity("aaa", ORDER_TYPE_OPEN, "87000", "100", "100", "10", SIDE_OPEN_LONG, 1582329600000, "100", "100", "100", "100", "100"))
                }

                override fun getPositionOrderList(symbol: String): List<KLineOrderEntity>? {
                    TODO("Not yet implemented")
                }

                override fun getStopLimitOrderList(symbol: String): List<KLineOrderEntity>? {
                    TODO("Not yet implemented")
                }

                override fun getPlanOrderList(symbol: String): List<KLineOrderEntity>? {
                    TODO("Not yet implemented")
                }
            })
        }
        orderDataHelper.updateSymbol("aaa")
        orderDataHelper.setHistoryOrderCallback(object : KLineOrderDataHelper.HistoryOrderCallback {
            override fun getHistoryOrderList(symbol: String): List<KLineOrderEntity>? {
                return listOf(KLineOrderEntity("aaa", ORDER_TYPE_OPEN, "price:100", "100", "100", "10", SIDE_OPEN_LONG, 1582329600000, "left", "leftHigh", "leftLow", "title", "highTitle"))

            }

            override fun getLiquidationOrderList(symbol: String): List<KLineOrderEntity>? {
                TODO("Not yet implemented")
            }
        })

        val adapter by lazy { KLineChartAdapter() }
        val data = mutableListOf<KLineEntity>()
        val kline = findViewById<KLineChartView>(R.id.kLineChartView)
        kline.setKChatConfig(KChartConfig(ValueFormatter(), false, listOf(MA, BOLL, EMA), listOf(OBV, VOL), true, 1, true, null, true))
        kline.adapter = adapter
        kline.isScrollEnable = true
        kline.isScaleEnable = true
        kline.justShowLoading()
        kline.setShowOpenOrder(true)
        kline.setShowHistoryOrder(true)
//        kline.setOnSelectedChangedListener(object : OnSelectedChangedListener {
//            override fun onSelectedChanged(view: BaseKLineChartView?, point: Any?, index: Int) {
//                if (point is KLineEntity) {
//
//                }
//
//            }
//        })
        kline.setSelectorListener(object : KLineChartView.KChartKLineSelectorListener {
            override fun showSelector(selectorEntity: KLineSelectorEntity?) {
            }

            override fun closeSelector() {
            }

            override fun onOrderInfoClick() {
            }

            override fun onClickOrderHideShow(show: Boolean) {
            }
        })


        kline.setOrderDataHelper(orderDataHelper)
        GlobalScope.launch(Dispatchers.IO) {
            data.addAll(loadKLineData(this@MainActivity))
            withContext(Dispatchers.Main) {
                adapter.addFooterData(data)
                adapter.notifyDataSetChanged()
                kline.startAnimation()
                kline.refreshComplete()
            }

        }
    }
}


@OptIn(DelicateCoroutinesApi::class)
@Composable
fun Greeting(modifier: Modifier = Modifier) {
    val adapter by lazy { KLineChartAdapter() }
    val data = mutableListOf<KLineEntity>()

    AndroidView(factory = { context ->
        val view = LinearLayout.inflate(context, R.layout.layout_kline, null)
        val kline = view.findViewById<KLineChartView>(R.id.kLineChartView)
        kline.adapter = adapter
        kline.justShowLoading()
        GlobalScope.launch(Dispatchers.IO) {
            data.addAll(loadKLineData(context))
            withContext(Dispatchers.Main) {
                adapter.addFooterData(data)
                adapter.notifyDataSetChanged()
                kline.startAnimation()
                kline.refreshComplete()
            }

        }


        view
    }, modifier = modifier.fillMaxSize())

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MKlinTheme {
        Greeting()
    }
}

@SuppressLint("DefaultLocale")
fun loadKLineData(context: Context): List<KLineEntity> {
//    val inputStream: InputStream = context.assets.open("ibm.json")
//    val size: Int = inputStream.available()
//    val buffer = ByteArray(size)
//    inputStream.read(buffer)
//    inputStream.close()
//    val json = String(buffer, Charsets.UTF_8)
//
//    val gson = Gson()
//    val listType = object : TypeToken<List<KLineEntity>>() {}.type
//    val data:List<KLineEntity> =gson.fromJson(json, listType)
    val data = mutableListOf<KLineEntity>()
    for (i in 0..100) {
        val klineEntity1 = KLineEntity()
        val basePrice = 86875 + (Math.random() - 0.5) * 200
        val highOffset = Math.random() * 100
        val lowOffset = Math.random() * 100
        val openOffset = (Math.random() - 0.5) * 100
        val closeOffset = (Math.random() - 0.5) * 100

        var high = basePrice + highOffset
        var low = basePrice - lowOffset
        var open = basePrice + openOffset
        var close = basePrice + closeOffset

        // Ensure high >= low, open and close are within high and low
        if (high < low) {
            val temp = high
            high = low
            low = temp
        }
        if (open < low) open = low
        if (open > high) open = high
        if (close < low) close = low
        if (close > high) close = high

        klineEntity1.setClose(String.format("%.2f", close))
        klineEntity1.setHigh(String.format("%.2f", high))
        klineEntity1.setLow(String.format("%.2f", low))
        klineEntity1.setOpen(String.format("%.2f", open))
        klineEntity1.setVolume(String.format("%.0f", 5168400 + (Math.random() - 0.5) * 200))
        klineEntity1.setAmount(String.format("%.2f", 1223 + (Math.random() - 0.5) * 100))
        klineEntity1.setDate((1577836800000L + 86400000L * i).toString())
        data.add(klineEntity1)
    }

    DataHelper.calculate(data)
    return data
}
