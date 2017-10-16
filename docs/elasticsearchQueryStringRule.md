# Query String Rule

```groovy

rule {
    //...
    params = [
            index: "logstash-*",
            queryString: "message:Error"
    ]

    reaction { messages -> // All messages that match the query
            // Your logic here
    }

}

```

You can find more about query string [here](https://www.elastic.co/guide/en/elasticsearch/reference/5.5/query-dsl-query-string-query.html)