# Migrate Java web applications to Azure App Service

## Intro

Learning objectives
By the end of this module, you'll be able to:

- Discover and containerize your Java web app running on Linux machines using Azure Migrate App Containerization.
- Build a container image for your Java web application.
- Deploy your containerized application to AKS using Azure Migrate App Containerization.

## Azure Migrate App Containerization overview

## Exercise - Set up host environment

### Setup Airsonic application

I had to change the default location to "eastus"
```Bash
git clone https://github.com/MicrosoftDocs/mslearn-azuremigrate-appcontainerization-javatomcat.git
cd mslearn-azuremigrate-appcontainerization-javatomcat/Java\ Containerization/
chmod +x scripts/deploy.sh
./scripts/deploy.sh 'eastus' 'LearnAppContainerization'
```

```text
$ ./scripts/deploy.sh 'eastus' 'LearnAppContainerization'
{
  "id": "/subscriptions/bbafc323-7813-419f-87a2-e17b65802a51/resourceGroups/LearnAppContainerization",
  "location": "eastus",
  "managedBy": null,
  "name": "LearnAppContainerization",
  "properties": {
    "provisioningState": "Succeeded"
  },
  "tags": null,
  "type": "Microsoft.Resources/resourceGroups"
}
This command is implicitly deprecated because command group 'group deployment' is deprecated and will be removed in a future release. Use 'depl
oyment group' instead.
{"error":{"code":"DeploymentNotFound","message":"Deployment 'azuredeploy' could not be found."}}
Command ran in 3.593 seconds (init: 0.279, invoke: 3.313)

```