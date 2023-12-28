package com.learn;

import com.learn.TelemetryItem.status;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.OutputBinding;
import com.microsoft.azure.functions.annotation.Cardinality;
import com.microsoft.azure.functions.annotation.CosmosDBOutput;
import com.microsoft.azure.functions.annotation.EventHubTrigger;

public class Function {

    @FunctionName("processSensorData")
    public void processSensorData(
            @EventHubTrigger(
                    name = "msg",
                    eventHubName = "", // blank because the value is included in the connection string
                    cardinality = Cardinality.ONE,
                    connection = "EventHubConnectionString")
            TelemetryItem item,
            @CosmosDBOutput(
                    name = "databaseOutput",
                    databaseName = "TelemetryDb",
                    collectionName = "TelemetryInfo",
                    connectionStringSetting = "CosmosDBConnectionString")
            OutputBinding<TelemetryItem> document,
            final ExecutionContext context) {

        context.getLogger().info("Event hub message received: " + item.toString());

        if (item.getPressure() > 30) {
            item.setNormalPressure(false);
        } else {
            item.setNormalPressure(true);
        }

        if (item.getTemperature() < 40) {
            item.setTemperatureStatus(status.COOL);
        } else if (item.getTemperature() > 90) {
            item.setTemperatureStatus(status.HOT);
        } else {
            item.setTemperatureStatus(status.WARM);
        }

        document.setValue(item);
    }
}