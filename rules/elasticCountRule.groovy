rule {
    ruleName = "countRule"
    params = [
            index: "logstash-*",
            matcher: "count",
            timeframe: "30s",
            filter: [:]
    ]

    reaction { messages ->
        messages.each {
            log(it.data.toString())
        }
    }
}