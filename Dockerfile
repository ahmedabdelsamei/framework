FROM java:8
VOLUME /tmp

COPY build/libs/vc.int.dynamic.rest.jar /app.jar

EXPOSE 8484
EXPOSE 8383
EXPOSE 9008
# RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-Xdebug", "-Xrunjdwp:server=y,transport=dt_socket,address=7171,suspend=n","-Dsun.management.jmxremote.level=FINEST","-Dsun.management.jmxremote.handlers=java.util.logging.ConsoleHandler","-Djava.util.logging.ConsoleHandler.level=FINEST","-Dcom.sun.management.jmxremote.local.only=false","-Dcom.sun.management.jmxremote.ssl=false","-Dcom.sun.management.jmxremote.authenticate=false","-Dcom.sun.management.jmxremote.port=9008","-Dcom.sun.management.jmxremote.rmi.port=9008","-Dcom.sun.management.jmxremote.host=0.0.0.0","-Djava.rmi.server.hostname=0.0.0.0","-jar","/app.jar"]
