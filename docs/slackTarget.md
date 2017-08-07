# Slack target

## Configuration

The following configuration should be added to `targetSettings` object in your config:

```json
        "slack": {
          "accessToken": ""
        }
```

## Usage

```groovy
slack("channel", "message")
```