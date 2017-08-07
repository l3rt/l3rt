# Count Rule

```groovy
rule {
    ruleName = "countRule"
    params = [
            index: "logstash-*",
            matcher: "count",
            timeframe: "30s",
            filter: [match: [message: "test"]]
    ]

    reaction { messages -> // here will be only one message
        messages.each {
            if (it.data.count > 10) {
                log("your message")
            }
        }
    }
}
```