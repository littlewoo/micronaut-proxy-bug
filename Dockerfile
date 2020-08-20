FROM openjdk:14-alpine
COPY build/libs/micronaut-proxy-bug-*-all.jar micronaut-proxy-bug.jar
EXPOSE 8080
CMD ["java", "-Dcom.sun.management.jmxremote", "-Xmx128m", "-jar", "micronaut-proxy-bug.jar"]