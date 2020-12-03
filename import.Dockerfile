FROM adoptopenjdk/openjdk11:alpine-jre

ENV TZ=Europe/Berlin
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ENV USER booklink-openlib
RUN adduser -S ${USER}

RUN mkdir -p /opt/app/samples
WORKDIR /opt/app

COPY target/booklink-openlibrary.jar /opt/app/

USER ${USER}
ENTRYPOINT ["java", "-cp", "/opt/app/booklink-openlibrary.jar"]
CMD ["-Dloader.main=com.github.mrazjava.booklink.openlibrary.OpenLibraryImportApp", "org.springframework.boot.loader.PropertiesLauncher"]