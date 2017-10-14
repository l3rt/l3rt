rule {
    ruleName = "myTestRule2"
    params = [
            index      : "logstash-*",
            queryString: "*",
            config     : [
                    sources       : [
                            ["url": "elasticSearch:http://localhost:9200"]
                    ],
                    targetSettings: [
                            jira: [
                                    url     : "http://localhost",
                                    username: "test",
                                    password: "test"
                            ]
                    ]
            ]
    ]

    reaction { messages ->
        log(messages)
        messages.each {
            jira("MYP", "test", it.data.toString(), "Task")
        }
    }
}