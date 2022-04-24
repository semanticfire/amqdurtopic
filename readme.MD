# AMQ Durable Topic Testcase 

This repo holds a testcase environment for durable topics resending messages with activemq 5.16.x

## Expected behaviour
Once the server is started `mvn clean package tomee:run` there is message creator URI `http://localhost:8080/amqtest/fw/topic/test?msg=helloworld` which will create a durable message in the topic `test` which contains `helloworld`
The reader uri `http://localhost:8080/amqtest/ReadTopic?id=test&subid=blah1234` creates a durable subscription with id `blah1234` and waits for 5 seconds for the next message and returns with `OK` when there is no new message.
Once a message is received it will return immediatly with the message contents `helloworld` in this case.
Repeated messages can be put on the topic, once the durable subscriber is created it will retrieve those messages 1 by 1.
This is what is observed with Tomee 8.0.4 Plus, which contains ActiveMQ 5.15.12

## Problematic behaviour
When a newer version of Tomee with ActiveMQ 5.16.x is used the behaviour changes, this can be observed by starting the server with `mvn clean package tomee:run -Dtomee.version=8.0.5`
Now when a message is publised on the durable topic, the reader URI will get the message ( maximumRedeliveries + 1 ) times, the default setting is 6 +1 is 7 times
This can be observed by repeatedly getting the reader URI `http://localhost:8080/amqtest/ReadTopic?id=test&subid=blah1234` and seeing the same messages, only after the 7th time it will wait and return `OK`


## Curl Commands
putting a message on the topic
```
curl http://localhost:8080/amqtest/fw/topic/test?msg=helloworld
```

Fetching the message
```
curl "http://localhost:8080/amqtest/ReadTopic?id=test&subid=blah1234"
```
