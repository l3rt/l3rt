# AWS ElasticSearch

L3rt perfectly works with AWS hosted ElasticSearch. In order to configure connection with the ElasticSearch, you need to 
specify three parameters `awsRegion`, `awsAccessKey` and `awsSecretKey` in your config.

```json
{
  "sources": [
    {
      "url": "elasticSearch:https://[...].us-east-1.es.amazonaws.com",
      "params": {
        "awsRegion":"your region e.g. us-east-1",
        "awsAccessKey": "your access key...",
        "awsSecretKey": "your secret key..."
      }
    }
  ]
}
```

If you want to use the AWS credentials provider, it's also supported. You just have to leave `"awsRegion":"us-east-1"` in your config file while
`awsAccessKey` and `awsSecretKey` will be read from your:
* Environment variables – `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` 
* The default credential profiles file - ` ~/.aws/credentials`
* Amazon ECS container credentials