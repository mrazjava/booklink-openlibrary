FROM openjdk:11-jre-slim

ENV TZ=Europe/Berlin
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ENV USER booklink-openlib
RUN adduser -S ${USER}

RUN mkdir -p /opt/app/samples
WORKDIR /opt/app

COPY target/booklink-openlibrary-*.jar /opt/app/booklink-openlibrary.jar

USER ${USER}
ENTRYPOINT ["java", "-cp", "/opt/app/booklink-openlibrary.jar"]
CMD ["-Dloader.main=com.github.mrazjava.booklink.openlibrary.OpenLibraryRestApp", "org.springframework.boot.loader.PropertiesLauncher"]

EXPOSE 8070