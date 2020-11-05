FROM openjdk:11-jre-slim

ENV TZ=Europe/Berlin
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ENV USER booklink-openlib
RUN useradd ${USER}

RUN mkdir -p /opt/app
WORKDIR /opt/app

COPY target/booklink-openlibrary-*.jar /opt/app/booklink-depot-openlibrary.jar

USER ${USER}
ENTRYPOINT ["java", "-cp", "/opt/app/booklink-depot-openlibrary.jar"]
CMD ["-Dloader.main=com.github.mrazjava.booklink.openlibrary.OpenLibraryDepotApp", "org.springframework.boot.loader.PropertiesLauncher"]