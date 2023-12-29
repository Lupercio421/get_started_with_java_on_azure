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
AZ_RESOURCE_GROUP=javacontainerizationdemorg
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

I manually added the resource myself on the Azure portal. 

## Containerize a Java app!


### Clone the Java Application

