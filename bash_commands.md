# Deploy a Java web app to Azure App Service

## Introduction
In this module, you'll deploy a Java web application to Tomcat on Azure App Service.

## Overview of a JavaServer Faces application

## Exercise - Create a JavaServer Faces web app on Tomcat
### Create a Maven project

mvn archetype:generate \
-DgroupId=com.microsoft.azure.samples \
-DartifactId=azure-javaweb-app \
-DarchetypeArtifactId=maven-archetype-webapp \
-Dversion=1.0-SNAPSHOT \
-DinteractiveMode=false

### Modify the name of index.jsp
mv index.jsp index.xhtml

### Creating a JSF backing bean
mkdir src/main/java
mkdir src/main/java/com
mkdir src/main/java/com/microsoft
mkdir src/main/java/com/microsoft/azure
mkdir src/main/java/com/microsoft/azure/samples

/c/Program Files

cp target/azure-javaweb-app.war /c/Program Files/apache-tomcat-9.0.39/webapps/

/c/Program Files/apache-tomcat-9.0.84/bin

# Permissions issues prevented me from running the startup shell script. I have decided to not complete the hands on lesson