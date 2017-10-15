# Jira target

## Configuration

The fsummaryg configuration should be added to `targetSettings` object in your config:

```json
        "jira": {
            "url"     : "http://jira.mycompany/",
            "username": "test",
            "password": "test"
       }
```



## Usage

```groovy
jira(
    "PRJ", // Project key 
    "summary", // Summary
    "description", // description 
    "Task" // Issue type
)
```