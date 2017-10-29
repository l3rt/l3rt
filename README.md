[![Build Status](https://travis-ci.org/l3rt/l3rt.svg?branch=master)](https://travis-ci.org/l3rt/l3rt)

![l3rt](lert.png)

# L3rt

Simple alerting application. It was inspired by elastalert and Jenkins pipelines. The goal is to provide users with more sophisticated rule system.

# Why l3rt?

* Zero-dependency. No additional database required.
* Infrastructure-as-code friendly. Manage your rules as regular files
* Infinite flexibility in rule configuration
* Built-in web UI playground

### Request a feature

[![Feature Requests](http://feathub.com/l3rt/l3rt?format=svg)](http://feathub.com/l3rt/l3rt)


Currently, l3rt supports the following sources as an input:

* ElasticSearch
* [AWS ElasticSearch](docs/awsElasticSearch.md)

Supported outputs:

* [Email](docs/mailTarget.md)
* [Hipchat](docs/hipchatTarget.md)
* [Slack](docs/slackTarget.md)
* [Jira](docs/jiraTarget.md)
* Missing yours? Add a [feature request](http://feathub.com/l3rt/l3rt) or submit a pull request 

Supported rules:

* ElasticSearch: [queryString rule](docs/elasticsearchQueryStringRule.md) - returns messages matched by a [query string](https://www.elastic.co/guide/en/elasticsearch/reference/5.5/query-dsl-query-string-query.html)
* ElasticSearch: [query rule](docs/elasticsearchQueryRule.md) - free-form rule that is fully managed by ElasticSearch's `search` query
* ElasticSearch: [count rule](docs/elasticsearchCountRule.md) - returns number of events occurred for the given period of time (or multiple periods)
* Missing yours? Add a [feature request](http://feathub.com/l3rt/l3rt) or submit a pull request 

## UI

You can use a web UI to create and debug your rules. Just start L3rt and go to http://localhost:8080/

![ui](UI.png)

## How to use

Create a config file where you need to specify common configuration for all your rules:

```json
{
  "sources": [
    {"url": "elasticSearch:http://localhost:9200"}
  ],
  "targetSettings": {
    "mailServer": {
      "host": "smtp.gmail.com",
      "port": "465",
      "auth":  true,
      "username": "myemail@test.com",
      "password": "password"
    }
  }
}
```

You can find more info about config [here](docs/config.md).

Create your rule. All rules are groovy based DSLs, so you can use all power of groovy in your rules.

`/opt/l3rt/rules/exampleRule.groovy`

```groovy
rule {
    ruleName = "myTestRule"
    params = [
            index: "logstash-*",
            queryString: "message:Error"
    ]

    reaction { messages ->
        messages.each {
            if (it.data.message.contains("Critical")) {
                email("manager@mycompany.com", "Error", it.data.message.toString())
                hipchat("Room with logs", it.data.message.toString(), "RED", true)
            } else {
                hipchat("Room with logs", it.data.message.toString(), "YELLOW", true)
            }
        }
    }
}
```

## Build & Launch 

### Development mode

You need:
* Java >=8
* Node ~v8.\*.\*
* Npm

1. Launch `lert.Application` as a plain java application with optional parameters:
* `-Dlogback.configurationFile=logback-dev.xml` - dev logger config
* `-Dconfig.file=~/l3rt/conf.json` - specifies the config location
* `-Drules=~/l3rt/rules/` - specifies a folder with rules if this parameter hasn't been defined in the config

2. Launch client side
* `cd ./client`
* `npm install`
* `npm run dev`
* Go to http://localhost:8888

### Manually 

1. Build
`./gradlew application:jar `

2. Run
`java -Dconfig.file=/l3rt/config/config.json -Drules=/l3rt/rules/ -jar ./application/build/libs/l3rt-0.1.2.jar`

NOTE: l3rt requires >= Java 8 and Docker

### Via Docker

Create your config file `~/conf.json` on your local machine. Then start it as follows:

`docker run -it -v ~/conf.json:/l3rt/config/config.json -p 8080:8080 dimafeng/l3rt:0.1.2`

Optionally you can specify location of your rules `-v ~/rules:/l3rt/rules`.

Go to [http://localhost:8080](http://localhost:8080) and play with L3rt!

## Contributions

This project is in heavy development stage so all contributions are appreciated. 
