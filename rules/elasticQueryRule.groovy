rule {
    sourceName = "testSource"
    ruleName = "myTestRule"
    params = [
            index: "logstash-*",
            query: [
                    query: [
                            range: ["@timestamp": [gt: lastSeenTimestamp]]
                    ]
            ],
            config : [
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