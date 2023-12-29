# Containerize and deploy a Java app to Azure

## Introduction

### Learning objectives
By the end of this module, you'll be able to:

- Containerize a Java app.
- Build a container image for the Java app.
- Run the container image locally.
- Push the container image to Azure Container Registry.
- Deploy the container image to Azure Kubernetes Service.

## Set up your Azure environment

```Bash
az account set --subscription "bbafc323-7813-419f-87a2-e17b65802a51"
```

### Define local variables

```Bash
AZ_RESOURCE_GROUP=MC_javacontainerizationdemorg_javacontainerizationdemoaks_eastus
AZ_CONTAINER_REGISTRY=loopydanjavacontainerregistry
AZ_KUBERNETES_CLUSTER=javacontainerizationdemoaks
AZ_LOCATION=eastus
AZ_KUBERNETES_CLUSTER_DNS_PREFIX=loopydanjavacontainerizationdemoaks
```

### Create an Azure Resource Group

Removing the | jq argument

```Bash
az group create \
    --name $AZ_RESOURCE_GROUP \
    --location $AZ_LOCATION
```

### Create an Azure Container Registry

```Bash
az acr create \
    --resource-group $AZ_RESOURCE_GROUP \
    --name $AZ_CONTAINER_REGISTRY \
    --sku Basic
```

```Bash
az configure \
    --defaults acr=$AZ_CONTAINER_REGISTRY
```

```Bash
az acr login -n $AZ_CONTAINER_REGISTRY
```
### Create an Azure Kubernetes Cluster

Command completion time: 

```Bash
az aks create \
    --resource-group $AZ_RESOURCE_GROUP \
    --name $AZ_KUBERNETES_CLUSTER \
    --attach-acr $AZ_CONTAINER_REGISTRY \
    --dns-name-prefix=$AZ_KUBERNETES_CLUSTER_DNS_PREFIX \
    --generate-ssh-keys
```

But I get this error instead.
```text
(QuotaExceeded) Provisioning of resource(s) for container service javacontainerizationdemoaks in resource group MC_javacontainerizationdemorg_javacontainerizationdemoaks_eastus failed. Message: Operation could not be
 completed as it results in exceeding approved standardDSv2Family Cores quota. Additional details - Deployment Model: Resource Manager, Location: eastus, Current Limit: 4, Current Usage: 0, Additional Required: 6, (M
inimum) New Limit Required: 6.
```

I did check the resource group on the azure portal and the aks service was created.

## Containerize a Java app!


### Clone the Java Application

```Bash
git clone https://github.com/Azure-Samples/containerize-and-deploy-Java-app-to-Azure.git
```

```Bash
cd containerize-and-deploy-Java-app-to-Azure/Project/Airlines
```
```Bash
mvn clean install
```

### Construct a Dockerfile

I don't like to use VIM, so I would not run this command next time.
```Bash
vi Dockerfile
```

## Build and run a container image for the Java app

```Bash
docker build -t flightbookingsystemsample .
```

```Bash
docker image ls
```

```Bash
docker run -p 8080:8080 flightbookingsystemsample
```

## Push the container image to Azure Container Registry

### Push a container image

```Bash
docker tag flightbookingsystemsample $AZ_CONTAINER_REGISTRY.azurecr.io/flightbookingsystemsample
```

```Bash
docker push $AZ_CONTAINER_REGISTRY.azurecr.io/flightbookingsystemsample
```

```Bash
az acr repository show -n $AZ_CONTAINER_REGISTRY --image flightbookingsystemsample:latest
```

```text
{
  "changeableAttributes": {
    "deleteEnabled": true,
    "listEnabled": true,
    "readEnabled": true,
    "writeEnabled": true
  },
  "createdTime": "2023-12-29T21:59:01.4350853Z",
  "digest": "sha256:3c8b55409ae47c511d370bca1c5957171c1b16a3e45dd34c804eb7590259ea63",
  "lastUpdateTime": "2023-12-29T21:59:01.4350853Z",
  "name": "latest",
  "signed": false
}
```

## Deploy the container image to Azure Kubernetes Service

### Deploy a container image

Install kubectl locally
```Bash
az aks install-cli
```
```Bash
az aks get-credentials --resource-group $AZ_RESOURCE_GROUP --name $AZ_KUBERNETES_CLUSTER
```
```text
Merged "javacontainerizationdemoaks" as current context in C:\Users\Daniel\.kube\config
```

Change directories into Project\Airlines
```Bash
kubectl apply -f deployment.yml
```
```text
deployment.apps/flightbookingsystemsample created
service/flightbookingsystemsample created
```

Use kubectl to monitor the status of the deployment
```Bash
kubectl get all
```

You can view the app logs within each pod as well

```Bash
kubectl logs pod/flightbookingsystemsample-5bfc8cc545-hllgg 
```