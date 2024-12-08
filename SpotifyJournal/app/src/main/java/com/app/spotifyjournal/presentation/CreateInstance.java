package com.app.spotifyjournal.presentation;

import android.util.Log;

import com.google.api.gax.rpc.ApiException;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.InsertInstanceRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.InstancesSettings;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

public class CreateInstance {
    public CreateInstance() {
    }

    public void createInstanceWithKey(String projectId, String zone, String instanceName,
                                      String machineType, String sourceImage, InputStream keyFilePath)
            throws IOException, InterruptedException, ExecutionException {
        ServiceAccountCredentials credentials = ServiceAccountCredentials
                .fromStream(keyFilePath);

        try (InstancesClient instancesClient = InstancesClient.create(
                InstancesSettings.newBuilder()
                        .setCredentialsProvider(() -> credentials)
                        .build())) {

            // Check if the instance already exists
            try {
                Instance existingInstance = instancesClient.get(projectId, zone, instanceName);
                Log.w("Already created", "Instance already exists: " + instanceName);
                return;
            }
            catch (ApiException e) {
                if (e.getStatusCode().getCode().name().equals("NOT_FOUND")) {
                    Log.w("Will create", "Instance does not exist. Proceeding with creation...");
                }
                else {
                    throw e;
                }
            }

            String machineTypeUrl = String.format(
                    "zones/%s/machineTypes/%s", zone, machineType);

            AttachedDisk bootDisk = AttachedDisk.newBuilder()
                    .setBoot(true)
                    .setInitializeParams(AttachedDiskInitializeParams.newBuilder()
                            .setSourceImage(sourceImage)
                            .setDiskSizeGb(10L)
                            .build())
                    .build();

            NetworkInterface networkInterface = NetworkInterface.newBuilder()
                    .setName("default")
                    .build();

            Instance instance = Instance.newBuilder()
                    .setName(instanceName)
                    .setZone(zone)
                    .setMachineType(machineTypeUrl)
                    .addDisks(bootDisk)
                    .addNetworkInterfaces(networkInterface)
                    .build();

            InsertInstanceRequest insertRequest = InsertInstanceRequest.newBuilder()
                    .setProject(projectId)
                    .setZone(zone)
                    .setInstanceResource(instance)
                    .build();

            Operation operation = instancesClient.insertAsync(insertRequest).get();
            if (operation.hasError()) {
                System.out.println("Error during instance creation: " + operation.getError().toString());
            } else {
                System.out.println("Instance created: " + instanceName);
            }
        } catch (ApiException e) {
            System.out.println("Error during API call: " + e.getMessage());
        }

        keyFilePath.close();
    }
}
