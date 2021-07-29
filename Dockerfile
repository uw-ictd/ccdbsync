FROM maven:3-jdk-8 as compiler

COPY . /ccdbsync

RUN cd /ccdbsync && \
    mvn validate && \
    mvn clean package && \
    mv target/ccdbsync-*-jar-with-dependencies.jar /ccdbsync.jar
	
FROM ubuntu:18.04 

COPY syncCCDB.sh /syncCCDB.sh
COPY --from=compiler /ccdbsync.jar /ccdbsync.jar
COPY entrypoint.sh /entrypoint.sh
COPY config /config

RUN chmod +x /syncCCDB.sh
RUN chmod +x /ccdbsync.jar
RUN chmod +x /entrypoint.sh

#Install Cron
RUN apt-get update
RUN apt-get -y install cron
RUN apt-get -y install vim

# Install OpenJDK-8
RUN apt-get install -y openjdk-8-jdk && \ 
    apt-get install -y ant;
    
# Fix certificate issues
RUN apt-get install ca-certificates-java && \
    update-ca-certificates -f;

RUN apt-get install -y maven;

RUN apt-get install -y dos2unix;

RUN apt-get clean;

#Convert scripts to unix
RUN dos2unix /syncCCDB.sh
RUN dos2unix /entrypoint.sh

# Setup JAVA_HOME -- useful for docker commandline
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/
RUN export JAVA_HOME

ENTRYPOINT /entrypoint.sh
