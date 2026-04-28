package org.example.irrigation;

import org.json.JSONObject;

public class IrrigationAdvisor {

    public static String getRecommendation(String city, String crop, double area, String unit, String soil, int daysSinceIrrigation) {

        JSONObject obj = WeatherService.getWeather(city);

        if (obj == null) return "Weather data not available for " + city;

        double temp = obj.getJSONObject("main").getDouble("temp");
        int humidity = obj.getJSONObject("main").getInt("humidity");

        double rain = 0;
        if (obj.has("rain")) {
            rain = obj.getJSONObject("rain").optDouble("1h", 0);
        }
        
        // Unit Conversion
        double areaInAcres = area;
        if ("Hectare".equalsIgnoreCase(unit)) {
            areaInAcres = area * 2.47;
        }

        // Base water requirement per crop per acre
        double baseRequirement = 0;
        switch (crop) {
            case "Wheat": baseRequirement = 500; break;
            case "Rice": baseRequirement = 1200; break;
            case "Maize": baseRequirement = 700; break;
            case "Sugarcane": baseRequirement = 1500; break;
            case "Cotton": baseRequirement = 800; break;
            case "Soybean": baseRequirement = 600; break;
            case "Mustard": baseRequirement = 400; break;
            default: baseRequirement = 500; // default generic
        }

        double adjustedRequirement = baseRequirement;

        // Weather Adjustments
        if (temp > 35) {
            adjustedRequirement *= 1.30;
        }
        if (humidity < 40) {
            adjustedRequirement *= 1.20;
        } else if (humidity > 80) {
            adjustedRequirement *= 0.90;
        }
        if (rain > 2) {
            adjustedRequirement *= 0.50;
        }

        // Soil Adjustments
        if ("Sandy".equalsIgnoreCase(soil)) {
            adjustedRequirement *= 1.20; // Increase by 20%
        } else if ("Clay".equalsIgnoreCase(soil)) {
            adjustedRequirement *= 0.80; // Decrease by 20%
        }
        // Loamy -> no change

        double totalWater = adjustedRequirement * areaInAcres;

        // Irrigation Interval Logic
        int recommendedGap = 5;
        switch (crop) {
            case "Wheat": recommendedGap = 7; break;
            case "Rice": recommendedGap = 3; break;
            case "Maize": recommendedGap = 5; break;
            case "Cotton": recommendedGap = 6; break;
            default: recommendedGap = 5;
        }

        String nextAction = "";
        if (daysSinceIrrigation >= recommendedGap) {
            if (rain > 5) {
                nextAction = "Delay irrigation due to rain";
            } else if (temp > 35) {
                nextAction = "Irrigate NOW (Early morning/evening)";
            } else {
                nextAction = "Irrigate NOW";
            }
        } else {
            nextAction = "in " + (recommendedGap - daysSinceIrrigation) + " days";
        }

        return String.format(
                "--- Weather Conditions ---\n" +
                "Temperature: %.1f °C\n" +
                "Humidity: %d%%\n" +
                "Rain: %.1f mm\n\n" +
                "--- Water Calculation ---\n" +
                "Base requirement: %.0f L/acre\n" +
                "Adjusted: %.0f L/acre\n" +
                "Land Area: %.2f acres\n" +
                "Total Water Required: %.0f Liters\n\n" +
                "--- Irrigation Schedule ---\n" +
                "Last irrigation: %d days ago\n" +
                "Recommended interval: %d days\n" +
                "Next action: %s\n\n" +
                "Soil Type: %s",
                temp, humidity, rain,
                baseRequirement, adjustedRequirement, areaInAcres, totalWater,
                daysSinceIrrigation, recommendedGap, nextAction, soil
        );
    }
}