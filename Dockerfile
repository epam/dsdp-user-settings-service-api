FROM amazoncorretto:17-al2023 AS builder
WORKDIR /application
ARG JAR_FILE=target/user-settings-service-api-*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM amazoncorretto:17-al2023
ENV USER_UID=1001 \
    USER_NAME=user-settings-service-api

RUN dnf update -y --security \
    && dnf install -y shadow-utils \
    && groupadd --gid ${USER_UID} ${USER_NAME} \
    && useradd --uid ${USER_UID} --gid ${USER_NAME} --shell /bin/nologin --no-create-home ${USER_NAME} \
    && dnf remove -y shadow-utils \
    && dnf clean all \
    && rm -rf /var/cache/dnf

WORKDIR /application

COPY --from=builder /application/dependencies/ ./
COPY --from=builder /application/spring-boot-loader/ ./
COPY --from=builder /application/snapshot-dependencies/ ./
COPY --from=builder /application/application/ ./

USER ${USER_NAME}

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} org.springframework.boot.loader.launch.JarLauncher ${0} ${@}"]