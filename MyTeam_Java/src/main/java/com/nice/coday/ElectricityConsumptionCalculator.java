package com.nice.coday;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;


public class ElectricityConsumptionCalculatorImpl implements ElectricityConsumptionCalculator {

    @Override
    public ConsumptionResult calculateElectricityAndTimeConsumption(ResourceInfo resourceInfo) throws IOException {
//        ConsumptionResult consumptionResult = new ConsumptionResult();
//        ConsumptionDetails dummyconsumptionDetails = new ConsumptionDetails();
//        consumptionResult.setConsumptionDetails(dummyconsumptionDetails);
//        Map<String, Long> dummytotalChargingStationTime = new HashMap<>();
//        consumptionResult.setConsumptionDetails(consumptionDetails);
//        consumptionResult.setTotalChargingStationTime(totalChargingStationTime);
//        return consumptionResult;

//        ConsumptionResult consumptionResult = new ConsumptionResult();
//        List<ConsumptionDetails> consumptionDetails = new ArrayList<>();
//        Map<String, Long> totalChargingStationTime = new HashMap<>();
//
//        parseVehicleTypeInfo(resourceInfo.getVehicleTypeInfoPath(), consumptionDetails);
//        parseChargingStationInfo(resourceInfo.getChargingStationInfoPath(), totalChargingStationTime);
//        parseEntryExitPointInfo(resourceInfo.getEntryExitPointInfoPath());
//        parseTimeToChargeVehicleInfo(resourceInfo.getTimeToChargeVehicleInfoPath(), consumptionDetails);
//        parseTripDetails(resourceInfo.getTripDetailsPath(), consumptionDetails, totalChargingStationTime);
//
//        consumptionResult.setConsumptionDetails(consumptionDetails);
//        consumptionResult.setTotalChargingStationTime(totalChargingStationTime);
//
//        return consumptionResult;


        ConsumptionResult consumptionResult = new ConsumptionResult();
        List<ConsumptionDetails> consumptionDetails = new ArrayList<>();
        Map<String, Long> totalChargingStationTime = new HashMap<>();

        // Initialize consumptionDetails with vehicle types
        parseVehicleTypeInfo(resourceInfo.getVehicleTypeInfoPath(), consumptionDetails);

        // Initialize totalChargingStationTime with charging stations
        Map<String, Long> chargingStationDistances = parseChargingStationInfo(resourceInfo.getChargingStationInfoPath(), totalChargingStationTime);

        // Process time to charge information
        Map<String, Map<String, Long>> timeToChargeMap = parseTimeToChargeVehicleInfo(resourceInfo.getTimeToChargeVehicleInfoPath());

        // Process trip details and update consumptionDetails and totalChargingStationTime
        parseTripDetails(resourceInfo.getTripDetailsPath(), consumptionDetails, totalChargingStationTime, chargingStationDistances, timeToChargeMap);

        // Set results in the ConsumptionResult object
        consumptionResult.setConsumptionDetails(consumptionDetails);
        consumptionResult.setTotalChargingStationTime(totalChargingStationTime);

        return consumptionResult;
    }

    // Parse VehicleTypeInfo.csv
    private void parseVehicleTypeInfo(Path vehicleTypeInfoPath, List<ConsumptionDetails> consumptionDetails) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(vehicleTypeInfoPath.toFile()))) {
            String line;
            br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    String vehicleType = parts[0];
                    double numberOfUnitsForFullyCharge = Double.parseDouble(parts[1]);
                    long mileage = Long.parseLong(parts[2]);

                    // Initialize ConsumptionDetails with mileage
                    consumptionDetails.add(new ConsumptionDetails(vehicleType, numberOfUnitsForFullyCharge, 0L, 0L, mileage));
                    System.out.println("Parsed Vehicle Type Info: " + vehicleType + ", Units: " + numberOfUnitsForFullyCharge + ", Mileage: " + mileage);
                }
            }
        }
    }



    // Parse ChargingStationInfo.csv
    private Map<String, Long> parseChargingStationInfo(Path chargingStationInfoPath, Map<String, Long> totalChargingStationTime) throws IOException {
        Map<String, Long> chargingStationDistances = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(chargingStationInfoPath.toFile()))) {
            String line;
            br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String chargingStation = parts[0];
                    long distanceFromStart = Long.parseLong(parts[1]);

                    // Initialize with 0 time
                    totalChargingStationTime.put(chargingStation, 0L);
                    chargingStationDistances.put(chargingStation, distanceFromStart);
                }
            }
        }
        return chargingStationDistances;
    }

    // Parse EntryExitPointInfo.csv
    private void parseEntryExitPointInfo(Path entryExitPointInfoPath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(entryExitPointInfoPath.toFile()))) {
            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {
                // Split line by comma
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String entryExitPoint = parts[0];
                    long distanceFromStart = Long.parseLong(parts[1]);

                    // Process entry and exit points here (can be stored or used based on your logic)
                }
            }
        }
    }

    // Parse TimeToChargeVehicleInfo.csv
    private Map<String, Map<String, Long>> parseTimeToChargeVehicleInfo(Path timeToChargeVehicleInfoPath) throws IOException {
        Map<String, Map<String, Long>> timeToChargeMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(timeToChargeVehicleInfoPath.toFile()))) {
            String line;
            br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String vehicleType = parts[0];
                    String chargingStation = parts[1];
                    long timeToChargePerUnit = Long.parseLong(parts[2]);

                    // Initialize the time map for each vehicle type if not already present
                    timeToChargeMap.computeIfAbsent(vehicleType, k -> new HashMap<>()).put(chargingStation, timeToChargePerUnit);
                }
            }
        }
        return timeToChargeMap;
    }
    // Parse TripDetails.csv
    private void parseTripDetails(Path tripDetailsPath, List<ConsumptionDetails> consumptionDetails,
                                  Map<String, Long> totalChargingStationTime,
                                  Map<String, Long> chargingStationDistances,
                                  Map<String, Map<String, Long>> timeToChargeMap) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(tripDetailsPath.toFile()))) {
            String line;
            br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    String id = parts[0];
                    String vehicleType = parts[1];
                    double remainingBatteryPercentage = Double.parseDouble(parts[2]);
                    String entryPoint = parts[3];
                    String exitPoint = parts[4];

                    // Find the consumption details for the vehicle type
                    ConsumptionDetails vehicleDetails = findConsumptionDetails(consumptionDetails, vehicleType);

                    if (vehicleDetails != null) {
                        // Calculate energy consumed and time taken
                        double energyConsumed = (100 - remainingBatteryPercentage) / 100 * vehicleDetails.getTotalUnitConsumed();
                        long timeTaken = vehicleDetails.getTotalTimeRequired(); // Update logic might be required here

                        // Debugging prints
                        System.out.println("Vehicle Type: " + vehicleType);
                        System.out.println("Energy Consumed: " + energyConsumed);
                        System.out.println("Time Taken: " + timeTaken);

                        // Update consumption details
                        vehicleDetails.setTotalUnitConsumed(vehicleDetails.getTotalUnitConsumed() + energyConsumed);
                        vehicleDetails.setTotalTimeRequired(vehicleDetails.getTotalTimeRequired() + timeTaken);
                        vehicleDetails.setNumberOfTripsFinished(vehicleDetails.getNumberOfTripsFinished() + 1);

                        // Find the last reachable charging station
                        String lastChargingStation = findLastReachableChargingStation(entryPoint, vehicleDetails.getMileage(), chargingStationDistances);
                        if (lastChargingStation != null) {
                            // Retrieve the charging time for the last reachable station
                            long chargingTime = timeToChargeMap.getOrDefault(vehicleType, Collections.emptyMap()).getOrDefault(lastChargingStation, 0L);
                            System.out.println("Charging Station: " + lastChargingStation + ", Charging Time: " + chargingTime);
                            totalChargingStationTime.merge(lastChargingStation, chargingTime, Long::sum);
                        }
                    } else {
                        System.out.println("No details found for vehicle type: " + vehicleType);
                    }
                }
            }
        }
    }


    private ConsumptionDetails findConsumptionDetails(List<ConsumptionDetails> details, String vehicleType) {
        for (ConsumptionDetails detail : details) {
            if (detail.getVehicleType().equals(vehicleType)) {
                return detail;
            }
        }
        return null;
    }


    private String findLastReachableChargingStation(String entryPoint, long mileage, Map<String, Long> chargingStationDistances) {
        long entryDistance = chargingStationDistances.getOrDefault(entryPoint, 0L);
        String lastStation = null;
        long maxDistance = entryDistance + mileage;

        for (Map.Entry<String, Long> entry : chargingStationDistances.entrySet()) {
            long stationDistance = entry.getValue();
            if (stationDistance <= maxDistance && (lastStation == null || stationDistance > chargingStationDistances.get(lastStation))) {
                lastStation = entry.getKey();
            }
        }

        return lastStation;
    }


}