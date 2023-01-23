FROM alpine/git as clone
ARG https://github.com/tutkualpsar/scylla.git   
WORKDIR /app
RUN git clone https://github.com/tutkualpsar/scylla.git

FROM maven:3.5-jdk-8-alpine as build  
ARG https://github.com/bptlab/scylla.git  
WORKDIR /app
COPY --from=clone /app/scylla/app
RUN mvn install

FROM openjdk:8-jre-alpine
ARG scylla   
ARG 0.0.1-SNAPSHOT.  
ENV artifact scylla-0.0.1-SNAPSHOT.jar 
WORKDIR /app
COPY --from=build /app/target/scylla-0.0.1-SNAPSHOT/app
EXPOSE 8080
ENTRYPOINT [“sh” , “-c”]
CMD ["java -jar scylla-0.0.1-SNAPSHOT”]
