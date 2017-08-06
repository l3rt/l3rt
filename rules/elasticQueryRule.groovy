rule {
    sourceName = "testSource"
    ruleName = "myTestRule"
    params = [
            "index": "logstash-*",
            "query": """{
                        "query": {
                             "range" : {
                                    "@timestamp" : {
                                        "gt": {lastProcessedTimestamp}
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