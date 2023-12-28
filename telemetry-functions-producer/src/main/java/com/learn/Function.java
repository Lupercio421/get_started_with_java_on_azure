package com.learn;

import com.microsoft.azure.functions.annotation.EventHubOutput;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import com.microsoft.azure.functions.ExecutionContext;

import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {
    /**
     * This function listens at endpoint "/api/HttpExample". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpExample
     * 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
     */

    @FunctionName("generateSensorData")
    @EventHubOutput(
            name = "event",
            eventHubName = "", // blank because the value is included in the connection string
            connection = "EventHubConnectionString")
    public TelemetryItem generateSensorData(
            @TimerTrigger(
                    name = "timerInfo",
                    schedule = "*/10 * * * * *") // every 10 seconds
            String timerInfo,
            final ExecutionContext context) {
        context.getLogger().info("Java Timer trigger function executed at: " + java.time.LocalDateTime.now());
        double temperature = Math.random() * 100;
        double pressure = Math.random() * 50;
        return new TelemetryItem(temperature, pressure);
    }

//    @FunctionName("HttpExample")
//    public HttpResponseMessage run(
//            @HttpTrigger(
//                name = "req",
//                methods = {HttpMethod.GET, HttpMethod.POST},
//                authLevel = AuthorizationLevel.ANONYMOUS)
//                HttpRequestMessage<Optional<String>> request,
//            final ExecutionContext context) {
//        context.getLogger().info("Java HTTP trigger processed a request.");
//
//        // Parse query parameter
//        final String query = request.getQueryParameters().get("name");
//        final String name = request.getBody().orElse(query);
//
//        if (name == null) {
//            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a name on the query string or in the request body").build();
//        } else {
//            return request.createResponseBuilder(HttpStatus.OK).body("Hello, " + name).build();
//        }
//    }
}
