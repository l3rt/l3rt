rule {
    sourceName = "testSource"
    ruleName = "myTestRule"
    params = [
            "index": "logstash-*",
            "query": [
                    query: [
                        range: ["@timestamp": [gt: lastSeenTimestamp]]
                    ]
            ]
    ]

    reaction { messages ->
        messages.each {
            log(it.data.toString())
        }
    }
}