# Deploy Spring microservices to Azure

## Introduction

Azure Spring Apps (ASA) is a managed service that makes it easy to deploy Spring Boot applications to Azure. ASA is jointly supported by Microsoft and VMware.

## Create an Azure Spring Apps instance

### Set up the Azure CLI

Install the spring extension for Azure CLI, by running:
```
az extension add -n spring -y
```

### Create an Azure Spring Apps instance

Creating the resource group
```
RESOURCE_GROUP_NAME=spring-cloud-workshop-dl
SPRING_CLOUD_NAME=azure-spring-cloud-workshop-dl
```

```
az group create \
-g "$RESOURCE_GROUP_NAME" \
-l eastus
```
```
{                                                                                                     
  "id": "/subscriptions/bbafc323-7813-419f-87a2-e17b65802a51/resourceGroups/spring-cloud-workshop-dl",
  "location": "eastus",                                                                               
  "managedBy": null,                                                                                  
  "name": "spring-cloud-workshop-dl",                                                                 
  "properties": {                                                                                     
    "provisioningState": "Succeeded"                                                                  
  },                                                                                                  
  "tags": null,                                                                                       
  "type": "Microsoft.Resources/resourceGroups"                                                        
} 
```
Creating The Azure Spring Apps instance:
```
az spring create \
    -g "$RESOURCE_GROUP_NAME" \
    -n "$SPRING_CLOUD_NAME" \
    --sku standard
```

```

```
We'll run Azure CLI commands referencing the same resource group and Azure Spring Apps instance. So le[GitHub guide to create a personal token.url](..%2F..%2FAppData%2FLocal%2FTemp%2FGitHub%20guide%20to%20create%20a%20personal%20token.url)t's set them as defaults, so we don't have to specify them again.

```
az configure --defaults group=${RESOURCE_GROUP_NAME}
az configure --defaults spring=${SPRING_CLOUD_NAME}
```
## Configure a Spring Cloud Config Server

### Create a GitHub personal token

Used the github [doc](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens) to create a classic personal access token. 

## Build a Spring Boot microservice

In this module, we'll build a cloud-enabled Spring Boot microservice. It uses a Spring Cloud service registry and a Spring Cloud Config Server, which are both managed and supported by Azure Spring Apps.

### Create the application on Azure Spring Apps
```
az spring app create --name todo-service --resource-group "$RESOURCE_GROUP_NAME" --service "$SPRING_CLOUD_NAME" --runtime-version Java_17
```

### Create a MySQL database

```
az mysql server create \
    --name ${SPRING_CLOUD_NAME}-mysql \
    --resource-group "$RESOURCE_GROUP_NAME" \
    --sku-name B_Gen5_1 \
    --storage-size 5120 \
    --admin-user "spring"
```

```
--password=V7J9P5m1Ue8v3Ta9SjW6kg"
```

Create a todos database in that server, and open up its firewall. The command is missing a resource group argument, so I will add it myself
```
az mysql db create \
    --name "todos" \
    --server-name ${SPRING_CLOUD_NAME}-mysql \
    --resource-group "$RESOURCE_GROUP_NAME"
```

```
az mysql server firewall-rule create \
    --name ${SPRING_CLOUD_NAME}-mysql-allow-azure-ip \
    --resource-group "$RESOURCE_GROUP_NAME" \
    --server ${SPRING_CLOUD_NAME}-mysql \
    --start-ip-address "0.0.0.0" \
    --end-ip-address "0.0.0.0"
```

```
{
  "endIpAddress": "0.0.0.0",
  "id": "/subscriptions/bbafc323-7813-419f-87a2-e17b65802a51/resourceGroups/spring-cloud-workshop-dl/providers/Microsoft.DBforMySQL/servers/azure-s
pring-cloud-workshop-dl-mysql/firewallRules/azure-spring-cloud-workshop-dl-mysql-allow-azure-ip",
  "name": "azure-spring-cloud-workshop-dl-mysql-allow-azure-ip",
  "resourceGroup": "spring-cloud-workshop-dl",
  "startIpAddress": "0.0.0.0",
  "type": "Microsoft.DBforMySQL/servers/firewallRules"
}
```

### Bind the MySQL database to the application
Lesson on Service Connector. I learned that "DB for MySQL single server" is on the retirement path. 

### Create a Spring Boot microservice

Now that we provisioned the Azure Spring Apps instance and configured the service binding, let's get the code for todo-service ready.

I ran the command with IntelliJ in full screen.
```
curl https://start.spring.io/starter.tgz -d type=maven-project -d dependencies=web,mysql,data-jpa,cloud-eureka,cloud-config-client -d baseDir=todo-service -d bootVersion=3.1.5.RELEASE -d javaVersion=17 | tar -xzvf -
```