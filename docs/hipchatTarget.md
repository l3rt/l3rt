# HipChat target

## Configuration

The following configuration should be added to `targetSettings` object in your config:

```json
        "hipchat": {
           "accessToken": "...",
           "baseUrl": "https://api.hipchat.com/v2/"
        }
```



## Usage

```groovy
hipchat(
    "room name", // Room name
    "message", // Your message
    "RED", // Message color - YELLOW, GREEN, RED, PURPLE, GRAY, RANDOM
    true // If this should trigger a notification 
)
```