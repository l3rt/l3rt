# L3rt

Simple alerting application. It was inspired by elastalert and the main idea is to provide users with more sophisticated rule system.

Currently l3rt support the following sources as an input:

* ElasticSearch

Supported output:

* Email
* Hipchat

## How to use

Create a config file:

```json
{
  "sources": [
    {
      "name": "elasticTest",
      "sourceType": "lert.elasticsearch.ElasticSearchProcessor",
      "params": {
        "host": "localhost",
        "port": "9200",
        "schema": "http"
      }
    }
  ],
  "rules": [
         "/opt/l3rt/rules/exampleRule.groovy"
  ],
  "targetSettings": {
    ... see target settings
  }
}
```

Specify the target settings:

### Mail

```json
        "mailServer": {
          "host": "...",
          "port": "...",
          "auth": "...",
          "username": "...",
          "password": "..."
        }
```

### Hipchat

```json
        "hipchat": {
           "accessToken": "...",
           "baseUrl": "https://api.hipchat.com/v2/"
        }
```

## Rules

All rules are groovy based DSLs, so you can use all power of groovy in your rules.

`exampleRule.groovy`

```groovy
rule {
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
            email("test@mycompany.com", "Error", it.data.toString())
            hipchat("Room with logs", it.data.toString(), "RED", true)
        }
    }
}
```

## Build & Launch 

`./gradlew application:jar `

`CONFIG=~/conf.json java -jar ./l3rt-0.1.0.jar`

NOTE: l3rt requires >= Java 8