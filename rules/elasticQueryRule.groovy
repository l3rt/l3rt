rule {
    sourceName = "testSource"
    ruleName = "myTestRule"
    params = [
            "index": "logstash-*",
            "query": [
                    query: [
                        range: ["@timestamp": [gt: lastSeenTimestamp ?: new Date()]]
                    ]
            ]
    ]

    reaction { messages ->
        messages.each {
            log(it.data.toString())
        }
    }
}