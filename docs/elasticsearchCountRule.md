# Count Rule

```groovy
rule {
    ruleName = "countRule"
    params = [
            index: "logstash-*",
            matcher: "count",
            timeframe: "30s",
            // numberOfTimeframes: 3, // you can capture multiple time frames
            filter: [match: [message: "test"]]
    ]

    /*
     here will be only one message if `numberOfTimeframes` is not defined or =1, otherwise the number of messages matches `numberOfTimeframes`
     */
    reaction { messages -> 
        messages.each {
            if (it.data.count > 10) {
                log("your message")
            }
        }
    }
}
```