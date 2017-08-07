# Mail target

## Configuration

The following configuration should be added to `targetSettings` object in your config:

```json
        "mailServer": {
          "host": "smtp.gmail.com",
          "port": "465",
          "auth":  true,
          "username": "myemail@test.com",
          "password": "password"
        }
```

## Usage

```groovy
email("test@test.com", "Subject", "Message")
```