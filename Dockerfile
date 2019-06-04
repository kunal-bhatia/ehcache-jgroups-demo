FROM openjdk:11-jdk-slim AS java-build
WORKDIR /app/
RUN mkdir -p target/dependency
COPY target/ehcache-jgroups-demo-*.jar  target/dependency/service.jar
RUN (cd target/dependency && jar -xf service.jar)

FROM gcr.io/distroless/java:11-debug
ARG DEPENDENCY=/app/target/dependency
COPY --from=java-build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=java-build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=java-build ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","com.tutorial.k8s.ehcache.demo.EhcacheJgroupsDemoApplication"]