# Deploy a Quarkus application to Azure Container Apps

## Exercise - Create a Quarkus application

Azure Container Apps is a fully managed serverless container service on Azure. It allows you to run containerized applications without worrying about orchestration or managing complex infrastructure such as Kubernetes.

You want to create a Quarkus application that hosts its logic in Azure Container Apps and hosts its database in an Azure PostgreSQL database.

### At a command prompt, generate the to-do application

```
mvn io.quarkus.platform:quarkus-maven-plugin:3.6.4:create \
    -DprojectGroupId=com.example.demo \
    -DprojectArtifactId=todo \
    -DclassName="com.example.demo.TodoResource" \
    -Dpath="/api/todos" \
    -DjavaVersion=11 \
	-Dextensions="resteasy-jackson, hibernate-orm-panache, jdbc-postgresql, docker"
```

### Executing the application

```
cd todo
./mvnw quarkus:dev
```

```
2023-12-26 17:50:53,162 INFO  [io.qua.hib.orm.dep.dev.HibernateOrmDevServicesProcessor] (build-32) Setting quarkus.hibernate-orm.database.generation=drop-a
nd-create to initialize Dev Services managed database
__  ____  __  _____   ___  __ ____  ______
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/
2023-12-26 17:50:54,847 WARN  [org.hib.eng.jdb.spi.SqlExceptionHelper] (JPA Startup Thread) SQL Warning Code: 0, SQLState: 00000

2023-12-26 17:50:54,849 WARN  [org.hib.eng.jdb.spi.SqlExceptionHelper] (JPA Startup Thread) table "myentity" does not exist, skipping
2023-12-26 17:50:54,850 WARN  [org.hib.eng.jdb.spi.SqlExceptionHelper] (JPA Startup Thread) SQL Warning Code: 0, SQLState: 00000
2023-12-26 17:50:54,851 WARN  [org.hib.eng.jdb.spi.SqlExceptionHelper] (JPA Startup Thread) table "todo" does not exist, skipping
2023-12-26 17:50:54,852 WARN  [org.hib.eng.jdb.spi.SqlExceptionHelper] (JPA Startup Thread) SQL Warning Code: 0, SQLState: 00000
2023-12-26 17:50:54,853 WARN  [org.hib.eng.jdb.spi.SqlExceptionHelper] (JPA Startup Thread) sequence "myentity_seq" does not exist, skipping
2023-12-26 17:50:54,854 WARN  [org.hib.eng.jdb.spi.SqlExceptionHelper] (JPA Startup Thread) SQL Warning Code: 0, SQLState: 00000
2023-12-26 17:50:54,854 WARN  [org.hib.eng.jdb.spi.SqlExceptionHelper] (JPA Startup Thread) sequence "todo_seq" does not exist, skipping
2023-12-26 17:50:54,942 INFO  [io.quarkus] (Quarkus Main Thread) todo 1.0.0-SNAPSHOT on JVM (powered by Quarkus 3.6.4) started in 13.348s. Listening on: ht
tp://localhost:8080
```

### POST Request to http://127.0.0.1:8080/api/todos, with request body:

```
{
    "description": "Take Quarkus MS Learn",
    "details": "Take the MS Learn on deploying Quarkus to Azure Container Apps",
    "done": "true"
}
```

Response
```
{
    "id": 1,
    "description": "Take Quarkus MS Learn",
    "details": "Take the MS Learn on deploying Quarkus to Azure Container Apps",
    "done": true,
    "createdAt": "2023-12-26T22:51:58.964870500Z"
}
```

### Test the application

```
./mvnw clean test
```

## Exercise - Set up Azure Container Apps

### Preparing the working environment

```
export AZ_PROJECT="azure-deploy-quarkus-DL"
export AZ_RESOURCE_GROUP="rg-${AZ_PROJECT}"
export AZ_LOCATION="eastus"
export AZ_CONTAINERAPP="ca-${AZ_PROJECT}"
export AZ_CONTAINERAPP_ENV="cae-${AZ_PROJECT}"
export AZ_POSTGRES_DB_NAME="postgres-${AZ_PROJECT}"
export AZ_POSTGRES_USERNAME=~
export AZ_POSTGRES_PASSWORD=~
export AZ_POSTGRES_SERVER_NAME="psql-${AZ_PROJECT}"
```

### Creating the resource group

```
az group create \
    --name $AZ_RESOURCE_GROUP \
    --location $AZ_LOCATION
```

```
{
  "id": "/subscriptions/bbafc323-7813-419f-87a2-e17b65802a51/resourceGroups/rg-azure-deploy-quarkus",
  "location": "eastus",
  "managedBy": null,
  "name": "rg-azure-deploy-quarkus",
  "properties": {
    "provisioningState": "Succeeded"
  },
  "tags": null,
  "type": "Microsoft.Resources/resourceGroups"
}
```

### Creating an instance of Azure Database for PostgreSQL

```
az postgres flexible-server create \
    --resource-group "$AZ_RESOURCE_GROUP" \
    --location "$AZ_LOCATION" \
    --name "$AZ_POSTGRES_SERVER_NAME" \
    --database-name "$AZ_POSTGRES_DB_NAME" \
    --admin-user "$AZ_POSTGRES_USERNAME" \
    --admin-password "$AZ_POSTGRES_PASSWORD" \
    --public-access "All" \
    --tier "Burstable" \
    --sku-name "Standard_B1ms" \
    --storage-size 256 \
    --version "14"
```

```
Resource group 'rg-azure-deploy-quarkus-DL' exists ? : True 
Creating PostgreSQL Server 'psql-azure-deploy-quarkus-dl' in group 'rg-azure-deploy-quarkus-DL'...
Your server 'psql-azure-deploy-quarkus-dl' is using sku 'Standard_B1ms' (Paid Tier). Please refer to https://aka.ms/postgres-pr
icing for pricing details
Configuring server firewall rule to accept connections from '0.0.0.0' to '255.255.255.255'...
Creating PostgreSQL database 'postgres-azure-deploy-quarkus-DL'...
Make a note of your password. If you forget, you would have to reset your password with "az postgres flexible-server update -n 
psql-azure-deploy-quarkus-dl -g rg-azure-deploy-quarkus-DL -p <new-password>".
Try using 'az postgres flexible-server connect' command to test out connection.
{
  "connStr": ~
eploy-quarkus-DL?sslmode=require",
  "databaseName": "postgres-azure-deploy-quarkus-DL",
  "firewallName": "AllowAll_2023-12-27_11-34-44",
  "host": "psql-azure-deploy-quarkus-dl.postgres.database.azure.com",
  "id": "/subscriptions/bbafc323-7813-419f-87a2-e17b65802a51/resourceGroups/rg-azure-deploy-quarkus-DL/providers/Microsoft.DBfo
rPostgreSQL/flexibleServers/psql-azure-deploy-quarkus-dl",
  "location": "East US",
  "pswd": ~,
  "resourceGroup": "rg-azure-deploy-quarkus-DL",
  "skuname": "Standard_B1ms",
  "username": "postgres",
  "version": "14"
}
```

### Configuring Quarkus to access the PostgreSQL database

You first need to obtain the connection string for the database

```
export POSTGRES_CONNECTION_STRING=$(
    az postgres flexible-server show-connection-string \
    --database-name "$AZ_POSTGRES_DB_NAME" \
    --admin-user "$AZ_POSTGRES_USERNAME" \
    --admin-password "$AZ_POSTGRES_PASSWORD" \
    --query "connectionStrings.jdbc" \
    --output tsv
)

export POSTGRES_CONNECTION_STRING_SSL="$POSTGRES_CONNECTION_STRING&ssl=true&sslmode=require"

echo "POSTGRES_CONNECTION_STRING_SSL=$POSTGRES_CONNECTION_STRING_SSL"
```

```
POSTGRES_CONNECTION_STRING_SSL=jdbc:postgresql://{server}.postgres.database.azure.com:5432/postgres-azure-deploy-quarkus-DL?user=~&password=~&sslmode=require&ssl=true&sslmode=require
```

I don't know how the {server} is being used as a variable, but this is what caused the quarkus application to not start. Thus, I had to make the connection string in application.properties as:

```
quarkus.datasource.jdbc.url=jdbc:postgresql://hostname-found-from-azure-pg-portal.postgres.database.azure.com:5432/postgres-azure-deploy-quarkus-DL?user=postgres&password=postgres&sslmode=require&ssl=true&sslmode=require
```

### Executing the Quarkus application locally to test the remote database connection

```
./mvnw clean quarkus:dev
```

## Deploy a Quarkus application to Azure Container Apps

In this unit, you create the Azure Container Apps environment using the Azure CLI. Then, you containerize the Quarkus application into a Docker image, push it to Azure Registry and deploy the image to Azure Container Apps.

### Setting up the Dockerfile for the Quarkus application

Let's take one of these Docker file (Dockerfile.jvm), rename it to Dockerfile and move it to the root folder.

```
mv src/main/docker/Dockerfile.jvm ./Dockerfile
```

Make sure that the Quarkus application is packaged as a jar file.

```
./mvnw package
```

### Creating the Azure Container App Environment and deploy the container

```
az containerapp up \
    --name "$AZ_CONTAINERAPP" \
    --environment "$AZ_CONTAINERAPP_ENV" \
    --location "$AZ_LOCATION" \
    --resource-group "$AZ_RESOURCE_GROUP" \
    --ingress external \
    --target-port 8080 \
    --source .
```

Though I don't know why I am getting the output below:

```
Using resource group 'rg-azure-deploy-quarkus-DL'
Creating ContainerAppEnvironment 'cae-azure-deploy-quarkus-DL' in resource group rg-azure-deploy-quarkus-DL
No Log Analytics workspace provided.
Generating a Log Analytics workspace with name "workspace-rgazuredeployquarkus9tU2"
Creating Azure Container Registry caa912f74e76acr in resource group rg-azure-deploy-quarkus-DL
No resource or more than one were found with name 'caa912f74e76acr'.
```

check the deployment by executing the following command that lists all the resources created by the az containerapp up command

```
az resource list \
    --location "$AZ_LOCATION" \
    --resource-group "$AZ_RESOURCE_GROUP" \
    --output table
```

### Executing the deployed Quarkus application

```
export AZ_APP_URL=$(
    az containerapp show \
        --name "$AZ_CONTAINERAPP" \
        --resource-group "$AZ_RESOURCE_GROUP" \
        --query "properties.configuration.ingress.fqdn" \
        --output tsv \
)

echo "AZ_APP_URL=$AZ_APP_URL"
```

## Exercise - Secure the Azure Container Apps environment

At the moment, we can access the database from any client (using Azure CLI and running Quarkus locally), which isn't secure. So we need to add a firewall rule to only allow IP addresses within the Azure Container Apps environment to access the database server.

### Accessing the PostgreSQL server from the CLI

```
az postgres flexible-server execute \
    --name "$AZ_POSTGRES_SERVER_NAME" \
    --admin-user "$AZ_POSTGRES_USERNAME" \
    --admin-password "$AZ_POSTGRES_PASSWORD" \
    --querytext "select * from Todo" \
    --output table
```

```
The command requires the extension rdbms-connect. Do you want to install it now? The command will continue to run after the extension is installed. (Y/n): Y
```

### Remove the permissive firewall rule

You can list the existing firewall rules w/:

```
az postgres flexible-server firewall-rule list \
    --name "$AZ_POSTGRES_SERVER_NAME" \
    --resource-group "$AZ_RESOURCE_GROUP" \
    --output table
```

```
az config set extension.use_dynamic_install=yes_without_prompt
```

```
Command group 'config' is experimental and under development. Reference and support levels: https://aka.ms/CLI_refstatus
```

We need to change that and update the firewall rules of the PostgreSQL server. In our case, it's just a matter of removing the public rule.

```
az postgres flexible-server firewall-rule delete \
    --name "$AZ_POSTGRES_SERVER_NAME" \
    --resource-group "$AZ_RESOURCE_GROUP" \
    --rule-name <name of the AllowAll firewall rule> \
    --yes
```

Querying the database from the CLI once again with the same command from above will return:

```
Unable to connect to flexible server: connection to server failed: Operation timed out
```

### Adding a new firewall rule

```
az postgres flexible-server firewall-rule create \
    --name "$AZ_POSTGRES_SERVER_NAME" \
    --resource-group "$AZ_RESOURCE_GROUP" \
    --rule-name "Allow_Azure-internal-IP-addresses" \
    --start-ip-address "0.0.0.0" \
    --end-ip-address "0.0.0.0"
```

Setting the start-ip-address and the end-ip-address to 0.0.0.0 allows access from all Azure-internal IP addresses but doesn't allow any external IP addresses. This practice helps to secure the database from external access.

If you try to access the database from the CLI, it should fail.
But if you try to retrieve the to-dos from the database from the Quarkus application running on Azure Container Apps, it succeeds:

```
curl https://$AZ_APP_URL/api/todos
```