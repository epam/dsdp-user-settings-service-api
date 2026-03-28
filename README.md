# user-settings-service-api

This service provides web api and interacts with the database for processing account settings of specific system user (email, phone etc.).

## Local development:

### Installation

#### Prerequisites

1. Installed [Java OpenJDK 11](https://openjdk.org/install/), [Maven](https://maven.apache.org/)
   and [Docker](https://www.docker.com/).
2. Maven is configured to use Nexus repository with all needed dependencies.

#### Configuring

* Configuration can be changed
  here [application-local.yml](user-settings-service-api/src/main/resources/application-local.yml).
* Any jvm attributes can be added to JAVA_OPTS environment variable in
  user-settings-service in [docker-compose.yml](docker-compose.yml).

#### Quick installation

1. Build the service
    ```shell
    mvn package
    ```
2. Run Docker-compose
    ```shell
    docker-compose up -d
    ```
3. Go to http://localhost:8001/openapi to open services Swagger, or connect to localhost:5005 with
   remote debug.
4. In case if you need to rebuild the service you also need to remove service docker image:
   ```shell
   docker rmi user-settings-service -f
   ```
5. Go to http://localhost:8002 to open Redis Commander and login with:
    * `username: admin`
    * `password: qwerty`
   > **_NOTE:_**  Redis Commander doesn't work if **redis-master** is down. 
   **redis-sentinel** configuration can be changed here [sentinel.conf](docker-local/redis/sentinel.conf).
6. Go to http://localhost:5555 to open pgAdmin and login with:
    * `email: admin@email.com`
    * `password: root`
    > **_NOTE:_**  if you need more data for development, you can add it to [init.sql](docker-local/postgres/init.sql)
    before running the **docker-compose** command.
7. Go to http://localhost:3030 to open Kafka UI

### Test execution

* Stop the docker-compose if it's running:
    ```shell
    docker-compose stop
    ```

* Tests could be run via maven command:
    * `mvn verify` OR using appropriate functions of your IDE. To avoid `The filename or extension is too long` error on Windows, please uncomment `<fork>false</fork>` in `spring-boot-maven-plugin` configuration.

## License

 The user-settings-service-api is Open Source software released under
the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0).