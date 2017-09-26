# Query Rule

```groovy

rule {
    //...
    params = [
            "index": "logstash-*",
            "query": [query: [
                                        bool: [
                                            must: [
                                                    ["range": ["@timestamp": ["gt": lastSeenTimestamp]]],
                                                    ["match": [ "message": "error" ]]
                                                ]
                                            ]
                                         ]
                                    ]
    ]

    reaction { messages -> // All messages that match the query
            // Your logic here
    }

}

```

* As a query you can use all power of the [search API](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-request-body.html)