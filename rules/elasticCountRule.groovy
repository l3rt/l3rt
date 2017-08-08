rule {
    ruleName = "countRule"
    params = [
            index: "logstash-*",
            matcher: "count",
            timeframe: "30s",
            numberOfTimeframes: 5,
            filter: [match: [message: "test"]]
    ]

    reaction { messages ->
        messages.each {
            log(it.data.toString())
        }
    }
}