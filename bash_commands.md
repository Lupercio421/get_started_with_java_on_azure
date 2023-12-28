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
We'll run Azure CLI commands referencing the same resource group and Azure Spring Apps instance. So let's set them as defaults, so we don't have to specify them again.

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
--psswd=~"
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

### Configure Spring Boot to create the database tables
In order to automatically generate the database tables when the application is deployed, add this line to your src/main/resources/application.properties configuration file:

YAML
```
spring.jpa.hibernate.ddl-auto=create-drop
```

### Deploy the application

Run the commands one by one. To make sure your maven project builds successfully before attempting to trigger the azure spring app deploy command. 
```
cd todo-service
./mvnw clean package -DskipTests
az spring app deploy --name todo-service --service "$SPRING_CLOUD_NAME" --resource-group "$RESOURCE_GROUP_NAME" --artifact-path target/demo-0.0.1-SNAPSHOT.jar
cd ..
```

Checking the logs
```
az spring app logs --name todo-service --service "$SPRING_CLOUD_NAME" --resource-group "$RESOURCE_GROUP_NAME" -f
```

### Test the project in the cloud

Contrary to the instruction in the lesson, the Registration status in my Azure Spring Apps Instance says 1/1

```
curl https://primary:0qtkTVf7gC9oXfZ2V2aIg9N1JfWxu1mVSjKUFdhOFDYIuuaxEyuecf2EJ1kXm19p@azure-spring-cloud-workshop-dl.test.azuremicroservices.io/todo-service/default/
```

```
[
    {
        "id": 1,
        "description": "First item",
        "done": true
    },
    {
        "id": 2,
        "description": "Second item",
        "done": true
    },
    {
        "id": 3,
        "description": "Third item",
        "done": false
    }
]
```

## Build a Spring Cloud Gateway

Gateways are used to route public HTTP traffic to microservices:

- They handle the routing logic.
- They secure the access to the microservices, which won't be publicly available.
- They can also have Quality of Service (QoS) capabilities, like doing HTTP rate limiting.

### Create a Spring Cloud Gateway

```
curl https://start.spring.io/starter.tgz -d type=maven-project -d dependencies=cloud-gateway,cloud-eureka,cloud-config-client -d baseDir=todo-gateway -d bootVersion=3.1.5.RELEASE -d javaVersion=17 | tar -xzvf -
```

```
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   300    0   166  100   134   1230    993 --:--:-- --:--:-- --:--:--  2238

gzip: stdin: not in gzip format
tar: Child returned status 1
tar: Error is not recoverable: exiting now

```

Did not have enough time to research what the issue was. I was thinking the dependencies have been renamed.

## Learn how to monitor performance issues by using distributed tracing.

## Learn how to scale Spring Boot microservices

Spring Boot microservices and gateways running inside Azure Spring Apps can be scaled vertically or horizontally:

- Vertical scaling means you increase (or decrease) the CPU and RAM of a given service.
- Horizontal scaling means you can add (or remove) nodes for a given service.