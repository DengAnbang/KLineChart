
KLineChartView初始化
    a.配置适配器KLineChartAdapter()
        addFooterData添加数据
            数据结构：KLineEntity()
            Close:String
            High:String
            Low :String
            Open :String
            Volume :String
            Amount :String
            date :String   时间戳
    b.配置KLineChartView()
        justShowLoading()加载loading
        refreshComplete() 刷新数据完成，loading消失

KChartConfig配置
        formatter：格式化数字
        isTimeLine：时分线或是k线
        mainNames：主图指标线显示类型 MA，EMA， BOLL，SAR, 
        subNames：副图指标线显示类型 VOL，MACD，KDJ，RSI， WR，OBV，STOCHRSI，MASTOCHRSI，MAOBV，ROC，MAROC，CCI，TRIX，DMI,
        showSubChart:是否显示副图,
        timeInterval
        showCustomDraw
        interval,
        isFutures
    

颜色配置
    res/values/themes.xml


LocalValues 默认文字配置 用于文字配置（目前是在弹窗中）
