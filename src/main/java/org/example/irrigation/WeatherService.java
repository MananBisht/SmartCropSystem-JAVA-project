package org.example.irrigation;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;

public class WeatherService {

    public static JSONObject getWeather(String city) {

        try {

            String apiKey = "71f45171e0a412b7cf17ac11665942e3";

            String link = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey + "&units=metric";

            URL url = new URL(link);

            Scanner sc = new Scanner(url.openStream());

            StringBuilder data = new StringBuilder();

            while (sc.hasNextLine()) {
                data.append(sc.nextLine());
            }

            sc.close();

            JSONObject obj = new JSONObject(data.toString());

            return obj;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
