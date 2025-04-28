package Service;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class ExchangeRateService {
    private final String exchangeApiKey = "111111ea7c697a26d9704cbef405f1";
    private final String fixerApiKey = "aaaa9b67dd5bf75330aee3706632e0961743";
    private final String baseCurrency = "EUR";
    private final String targetBaseCurrency = "CNY";
    private Map<String, Double> exchangeRates = new HashMap<>();
    private Map<String, Map<String, Double>> historicalRatesCache = new HashMap<>();
    private long lastApiCallTimestamp = 0;
    private static final String CACHE_FILE = "history_cache.json";
    private static final long ONE_DAY_MS = 24 * 60 * 60 * 1000;

    public ExchangeRateService() {
        loadCache();
    }

    public Map<String, Double> getExchangeRates() {
        return exchangeRates;
    }

    public Map<String, Double> getHistoricalRates(String date) {
        return historicalRatesCache.getOrDefault(date, new HashMap<>());
    }

    public void fetchExchangeRates(Consumer<Map<String, Double>> onSuccess, Consumer<String> onError) {
        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://v6.exchangerate-api.com/v6/" + exchangeApiKey + "/latest/" + baseCurrency))
                        .GET()
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                Gson gson = new Gson();
                JsonObject json = gson.fromJson(response.body(), JsonObject.class);
                JsonObject rates = json.getAsJsonObject("conversion_rates");

                Map<String, Double> newRates = new HashMap<>();
                double eurToCny = rates.get(targetBaseCurrency).getAsDouble();
                String[] currencies = {"USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "HKD", "SGD", "NZD", "CNY"};
                for (String currency : currencies) {
                    if (rates.has(currency)) {
                        newRates.put(currency, rates.get(currency).getAsDouble() / eurToCny);
                    }
                }
                exchangeRates.clear();
                exchangeRates.putAll(newRates);
                onSuccess.accept(exchangeRates);
            } catch (IOException | InterruptedException e) {
                onError.accept(e.getMessage());
            }
        }).start();
    }

    public void fetchHistoricalRates() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastApiCallTimestamp < ONE_DAY_MS && !historicalRatesCache.isEmpty()) {
            return;
        }

        historicalRatesCache.clear();
        HttpClient client = HttpClient.newHttpClient();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Calendar cal = java.util.Calendar.getInstance();
        String[] currencies = {"USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "HKD", "SGD", "NZD", "CNY"};
        Gson gson = new Gson();

        for (int i = 11; i >= 0; i--) {
            cal.setTime(new java.util.Date());
            cal.add(java.util.Calendar.MONTH, -i);
            cal.set(java.util.Calendar.DAY_OF_MONTH, cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
            String date = sdf.format(cal.getTime());
            try {
                String symbols = String.join(",", currencies);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://data.fixer.io/api/" + date + "?access_key=" + fixerApiKey + "&base=" + baseCurrency + "&symbols=" + symbols))
                        .GET()
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                JsonObject json = gson.fromJson(response.body(), JsonObject.class);
                if (!json.get("success").getAsBoolean()) {
                    JsonObject error = json.getAsJsonObject("error");
                    String errorType = error.has("type") ? error.get("type").getAsString() : "unknown";
                    String errorInfo = error.has("info") ? error.get("info").getAsString() : "no info";
                    throw new IOException("API error: type=" + errorType + ", info=" + errorInfo);
                }
                JsonObject rates = json.getAsJsonObject("rates");
                Map<String, Double> monthlyRates = new HashMap<>();
                double eurToCny = rates.get(targetBaseCurrency).getAsDouble();
                for (String currency : currencies) {
                    if (rates.has(currency) && !currency.equals(targetBaseCurrency)) {
                        monthlyRates.put(currency, rates.get(currency).getAsDouble() / eurToCny);
                    }
                }
                historicalRatesCache.put(date, monthlyRates);
            } catch (IOException | InterruptedException e) {
                System.err.println("Failed to fetch historical rates for " + date + ": " + e.getMessage());
            }
        }

        lastApiCallTimestamp = currentTime;
        saveCache();
    }

    private void saveCache() {
        try (FileWriter writer = new FileWriter(CACHE_FILE)) {
            Gson gson = new Gson();
            Map<String, Object> cache = new HashMap<>();
            cache.put("timestamp", lastApiCallTimestamp);
            cache.put("rates", historicalRatesCache);
            gson.toJson(cache, writer);
        } catch (IOException e) {
            System.err.println("Failed to save cache: " + e.getMessage());
        }
    }

    private void loadCache() {
        File file = new File(CACHE_FILE);
        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file)) {
            Gson gson = new Gson();
            Map<String, Object> cache = gson.fromJson(reader, new TypeToken<Map<String, Object>>(){}.getType());
            lastApiCallTimestamp = ((Double) cache.get("timestamp")).longValue();
            Map<String, Map<String, Double>> rates = (Map<String, Map<String, Double>>) cache.get("rates");
            historicalRatesCache.putAll(rates);
        } catch (IOException e) {
            System.err.println("Failed to load cache: " + e.getMessage());
        }
    }
}