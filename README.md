## YDB Payments Application

### How to build

Requirements
* Java 17 or newer
* Maven 3.0.0 or newer

To build the application as a single executable jar file, run the command:
```
mvn clean package spring-boot:repackage
```
After that, the compiled `ydb-pay2025-app-1.0.0-SNAPSHOT.jar` can be found in the target folder.

### How to launch

The application is built as a single executable jar file and can be run with the command:
```
java -jar ydb-pay2025-app-1.0.0-SNAPSHOT.jar <options> <commands>
```
Where `options` are application parameters (see the Application Parameters section), and `commands` are the sequence of
commands the application will execute one after the other. Currently, the following commands are supported:
* clean    - clean the database, all tables will be deleted
* init     - prepare the database, application will create the table structure and insert initial data
* test     - run test workload

### Application parameters

Application parameters allow you to configure different aspects of the application's operation, primarily the database connection address.
The main parameters list:
* `app.connection` - database connection address. Specified as `<schema>://<endpoint>:<port>/<database>`
* `app.threadsCount` - number of threads of test workload. Defaults to the number of CPU cores on the host.
* `app.workloadDuration` - test duration in seconds when running the run command. Default is 60 seconds.
* `app.testRps` - limit on the number of operations per second during the test workload. Default is 100.
* `app.batchMaxSize` - limit on the number of payments in one transaction. By default is 5000.
* `app.saldoShiftMs` - time shift for the saldo updates reading. Default is 500ms.

All parameters can be passed directly when launching the application (in the format `--param_name=value`) or can be
preconfigured in an `application.properties` file saved next to the executable jar of the application.


### Prometeus metrics

To enable Prometeus metrics exports, you can add the next options to `application.properties`.
```properties
# Enable Spring Boot Actuator
management.endpoints.web.exposure.include=health,metrics,prometheus,info
management.endpoint.health.enabled=true
management.endpoint.metrics.enabled=true
management.endpoint.prometheus.enabled=true
```
