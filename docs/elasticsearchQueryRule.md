# Query Rule

```groovy

rule {
    //...
    params = [
            "index": "logstash-*",
            "query": """{
                        "query": {
                            "bool": {
                                "must": [
                                        {"range": {"@timestamp": {"gt": {lastProcessedTimestamp}}}},
                                        {"match": { "message": "error" }}
                                    ]
                                }
                             }
                        }
                     }
                    """
    ]

    reaction { messages -> // All messages that match the query
            // Your logic here
    }

}

```

* As a query you can use all power of the [search API](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-request-body.html)
* `{lastProcessedTimestamp}` is a placeholder that represents a timestamp of last element that was processed during the previous step. It will be replaced by a `Long` value during query execution.