rule {
    sourceName = "testSource"
    ruleName = "myTestRule"
    params = [
            "index": "logstash-*",
            "query": """{
                        "query": {
                             "range" : {
                                    "@timestamp" : {
                                        "gte": {lastProcessedTimestamp}
                                    }
                             }
                        }
                     }
                    """
    ]

    reaction { messages ->
        messages.each {
            log(it.data.'@timestamp')
        }
    }
}