# 换成这个！能找到、能下载、不报错！
FROM eclipse-temurin:8-jre-focal

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 58081

ENTRYPOINT ["java", "-jar", "app.jar"]