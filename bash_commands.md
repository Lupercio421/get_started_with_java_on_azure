# Build a real-time event-driven Java solution in Azure

## Introduction

## What is event driven, and how fast is real time?

### Event-driven applications
Event-driven applications use the fire and forget principle. The event gets sent or fired toward the next system, which can be another service, an event hub, a stream, or a message broker like Kafka.

## Exercise - Build an Azure function to simulate telemetric data

For our example, we'll use event sourcing. Let's build a function that's simulating telemetric data and send it to an event hub. Later, another function can listen to this event and process and store it in a database created with Azure Cosmos DB.

### Preparte your env
```
RESOURCE_GROUP=java-event-driven-dl
EVENT_HUB_NAMESPACE=java-event-driven-hub-nme-spc-dl
EVENT_HUB_NAME=java-event-driven-hub-nme-dl
EVENT_HUB_AUTHORIZATION_RULE=java-event-driven-auth-rule-dl
COSMOS_DB_ACCOUNT=java-cosmos-with-dl
STORAGE_ACCOUNT=javaeventstoragedl
FUNCTION_APP=java-event-driven-fnc-app-dl
LOCATION=eastus
```

### Create the required components

#### Create a resource group

```
az group create \
    --name $RESOURCE_GROUP \
    --location $LOCATION
```

#### Create and configure an event hub

```
az eventhubs namespace create \
    --resource-group $RESOURCE_GROUP \
    --name $EVENT_HUB_NAMESPACE
```

--mesage-retention was not an accepted argument. So I decided to remove it:
```
az eventhubs eventhub create \
    --resource-group $RESOURCE_GROUP \
    --name $EVENT_HUB_NAME \
    --namespace-name $EVENT_HUB_NAMESPACE
```
```
az eventhubs eventhub authorization-rule create \
    --resource-group $RESOURCE_GROUP \
    --name $EVENT_HUB_AUTHORIZATION_RULE \
    --eventhub-name $EVENT_HUB_NAME \
    --namespace-name $EVENT_HUB_NAMESPACE \
    --rights Listen Send
```
```
{
  "createdAt": "2023-12-28T20:00:00.323Z",
  "id": "/subscriptions/bbafc323-7813-419f-87a2-e17b65802a51/resourceGroups/java-event-driven-dl/providers/Microsoft.EventHub/namespaces/java-ev
ent-driven-hub-nme-spc-dl/eventhubs/java-event-driven-hub-nme-dl",
  "location": "eastus",
  "messageRetentionInDays": 7,
  "name": "java-event-driven-hub-nme-dl",
  "partitionCount": 4,
  "partitionIds": [
    "0",
    "1",
    "2",
    "3"
  ],
  "resourceGroup": "java-event-driven-dl",
  "retentionDescription": {
    "cleanupPolicy": "Delete",
    "retentionTimeInHours": 168
  },
  "status": "Active",
  "type": "Microsoft.EventHub/namespaces/eventhubs",
  "updatedAt": "2023-12-28T20:00:00.547Z"
}
```

```
{
  "id": "/subscriptions/bbafc323-7813-419f-87a2-e17b65802a51/resourceGroups/java-event-driven-dl/providers/Microsoft.EventHub/namespaces/java-ev
ent-driven-hub-nme-spc-dl/eventhubs/java-event-driven-hub-nme-dl/authorizationRules/java-event-driven-auth-rule-dl",
  "name": "java-event-driven-auth-rule-dl",
  "resourceGroup": "java-event-driven-dl",
  "rights": [
    "Listen",
    "Send"
  ],
  "type": "Microsoft.EventHub/namespaces/eventhubs/authorizationrules"
}
```

#### Build, configure, and deploy the Azure function

This function will be the event-producing one. Ensure that you are only using letters and numbers in your storage account variables. I did not realize hyphens were a no-no.

```
az storage account create \
    --resource-group $RESOURCE_GROUP \
    --name $STORAGE_ACCOUNT"p" \
    --sku Standard_LRS
```

```
az functionapp create \
    --resource-group $RESOURCE_GROUP \
    --name $FUNCTION_APP"-p"\
    --storage-account $STORAGE_ACCOUNT"p" \
    --consumption-plan-location $LOCATION \
    --runtime java \
    --functions-version 4
```

To retrieve the connection strings for the storage account and the event hub, we can use the following commands and save them in environment variables.

```
AZURE_WEB_JOBS_STORAGE=$( \
    az storage account show-connection-string \
        --resource-group $RESOURCE_GROUP \
        --name $STORAGE_ACCOUNT"p" \
        --query connectionString \
        --output tsv)
echo $AZURE_WEB_JOBS_STORAGE
```

```
EVENT_HUB_CONNECTION_STRING=$( \
    az eventhubs eventhub authorization-rule keys list \
        --resource-group $RESOURCE_GROUP \
        --name $EVENT_HUB_AUTHORIZATION_RULE \
        --eventhub-name $EVENT_HUB_NAME \
        --namespace-name $EVENT_HUB_NAMESPACE \
        --query primaryConnectionString \
        --output tsv)
echo $EVENT_HUB_CONNECTION_STRING
```

To store the connection strings in the application settings of your Azure Function account, run the following command in your terminal:

```
az functionapp config appsettings set \
    --resource-group $RESOURCE_GROUP \
    --name $FUNCTION_APP"-p" \
    --settings \
        AzureWebJobsStorage=$AZURE_WEB_JOBS_STORAGE \
        EventHubConnectionString=$EVENT_HUB_CONNECTION_STRING
```

Next, create a local functions project with Maven.

```Bash
mvn archetype:generate --batch-mode \
    -DarchetypeGroupId=com.microsoft.azure \
    -DarchetypeArtifactId=azure-functions-archetype \
    -DappName=$FUNCTION_APP"-p" \
    -DresourceGroup=$RESOURCE_GROUP \
    -DappRegion=$LOCATION \
    -DappServicePlanName=$LOCATION"plan" \
    -DgroupId=com.learn \
    -DartifactId=telemetry-functions-producer
```

```Bash
cd telemetry-functions-producer
rm -r src/test
```

For local execution, the application settings need to be retrieved and stored at the local.settings.json file. You can do that automatically by running the fetch-app-settings command.

```Bash
func azure functionapp fetch-app-settings $FUNCTION_APP"-p"
```

### Run Locally

```Bash
mvn clean package
mvn azure-functions:run
```

### Deploy to Azure

```Bash
mvn azure-functions:deploy
```

## Exercise - Process the events and store the data in Azure Cosmos DB

A second function can listen to events of the specific namespace in the Azure event hub and process and store them in a database created with Azure Cosmos DB.

### Create a database with Azure Cosmos DB

```Bash
az cosmosdb create \
    --resource-group $RESOURCE_GROUP \
    --name $COSMOS_DB_ACCOUNT
```

```Bash
az cosmosdb sql database create \
    --resource-group $RESOURCE_GROUP \
    --account-name $COSMOS_DB_ACCOUNT \
    --name TelemetryDb
```

defining temperatureStatus as the partition key
```Bash
az cosmosdb sql container create \
    --resource-group $RESOURCE_GROUP \
    --account-name $COSMOS_DB_ACCOUNT \
    --database-name TelemetryDb \
    --name TelemetryInfo \
    --partition-key-path '/temperatureStatus'
```

```text
BadRequest) Message: {"code":"BadRequest","message":"Message: {\"Errors\":[\"The partition key component definition path 'C:\\/Program Files\\/
Git\\/temperatureStatus' could not be accepted, failed near position '0'. Partition key paths must contain only valid characters and not contain
 a trailing slash or wildcard character.\"]}\
```

As an alternative, I went onto Cosmos DB instance in the Azure portal, and created the container instance manually.

## Build, configure, and deploy another Azure function

For our scenario, you'll create one consuming Azure function as an example. To create the function, following best practices, it will be independent, with its own storage account and bindings for loose coupling and scalability.

```Bash
az storage account create \
    --resource-group $RESOURCE_GROUP \
    --name $STORAGE_ACCOUNT"c" \
    --sku Standard_LRS
```

```Bash
az functionapp create \
    --resource-group $RESOURCE_GROUP \
    --name $FUNCTION_APP"-c"\
    --storage-account $STORAGE_ACCOUNT"c" \
    --consumption-plan-location $LOCATION \
    --runtime java \
    --functions-version 4
```

### Retrieve the connection strings

```Bash
AZURE_WEB_JOBS_STORAGE=$( \
    az storage account show-connection-string \
        --resource-group $RESOURCE_GROUP \
        --name $STORAGE_ACCOUNT"c" \
        --query connectionString \
        --output tsv)
echo $AZURE_WEB_JOBS_STORAGE
```

```Bash
COSMOS_DB_CONNECTION_STRING=$( \
    az cosmosdb keys list \
        --resource-group $RESOURCE_GROUP \
        --name $COSMOS_DB_ACCOUNT \
        --type connection-strings \
        --query 'connectionStrings[0].connectionString' \
        --output tsv)
echo $COSMOS_DB_CONNECTION_STRING
```

storing in the application settings for your Azure Functions account

```Bash
az functionapp config appsettings set \
    --resource-group $RESOURCE_GROUP \
    --name $FUNCTION_APP"-c" \
    --settings \
        AzureWebJobsStorage=$AZURE_WEB_JOBS_STORAGE \
        EventHubConnectionString=$EVENT_HUB_CONNECTION_STRING \
        CosmosDBConnectionString=$COSMOS_DB_CONNECTION_STRING
```

### Create the functions application

```Bash
cd ..
mvn archetype:generate --batch-mode \
    -DarchetypeGroupId=com.microsoft.azure \
    -DarchetypeArtifactId=azure-functions-archetype \
    -DappName=$FUNCTION_APP"-c" \
    -DresourceGroup=$RESOURCE_GROUP \
    -DappRegion=$LOCATION \
    -DappServicePlanName=$LOCATION"plan" \
    -DgroupId=com.learn \
    -DartifactId=telemetry-functions-consumer
```

Deleting the test files

```Bash
cd telemetry-functions-consumer
rm -r src/test
```

Update the local settings for local execution and debugging.

```Bash
func azure functionapp fetch-app-settings $FUNCTION_APP"-c"
```

Modifying the Function class


The TelemetryItem objects can be seen as the consumer-driven contract between the participants of this event-driven system.

### Run Locally

```Bash
mvn clean package
mvn azure-functions:run
```

### Deploy on Azure

```Bash
mvn azure-functions:deploy
```

```text
[INFO] Trying to deploy artifact to java-event-driven-fnc-app-dl-c...
[INFO] Successfully deployed the artifact to https://java-event-driven-fnc-app-dl-c.azurewebsites.net
...
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  01:00 min
[INFO] Finished at: 2023-12-28T16:43:16-05:00
[INFO] ------------------------------------------------------------------------

```

Wonderful! We deployed the whole telemetry scenario by sending the data toward an event hub and consuming the data with a different independent function. The function processes the data and then stores the result in a database created with Azure Cosmos DB