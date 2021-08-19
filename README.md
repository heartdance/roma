# roma
Intranet penetration

---

### server

1. example

   ```shell
   java -jar roma-server-1.0-SNAPSHOT.jar -p 9989
   ```

2. parameters

   | parameter | type    | example | description                            |
   | --------- | ------- | ------- | -------------------------------------- |
   | -p        | Integer | 9989    | port connected by client, default 9989 |

---

### client

1. example

   ```shell
   java -jar roma-client-1.0-SNAPSHOT.jar -h localhost -p 9989 -t 80/localhost:8080 -u 81/127.0.0.1:8081
   ```

2. parameters

   | parameter | type    | example           | description                                                  |
   | --------- | ------- | ----------------- | ------------------------------------------------------------ |
   | -h        | String  | 192.168.6.3       | Roma server host                                             |
   | -p        | Integer | 9989              | Roma server port, default 9989                               |
   | -t        | String  | 80/localhost:8080 | Tcp proxy mapping. The '80/localhost:8080' means you can from server host with port 80 to visit localhost 8080. Multiple proxies split by a comma |
   | -u        | String  | 81/127.0.0.1:8081 | Udp proxy mapping. The usage is the same as above.           |

