rule {
    ruleName = "countRule"
    params = [
            index             : "logstash-*",
            matcher           : "count",
            timeframe         : "30s",
            numberOfTimeframes: 5,
            filter            : [match: [message: "test"]],
            config            : [
                    sources: [
                            ["url": "elasticSearch:http://localhost:9200"]
                    ]
            ]
    ]

    reaction { messages ->
        messages.each {
            log(it.data.toString())
        }
    }
}