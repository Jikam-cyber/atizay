package atizay.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class MonCashClient {
    // === SANDBOX ===
    private static final String API_BASE = "https://sandbox.moncashbutton.digicelgroup.com/Api";
    private static final String OAUTH_URL = API_BASE + "/oauth/token";
    private static final String CREATE_URL = API_BASE + "/v1/CreatePayment";
    private static final String RETRIEVE_BY_ORDER_URL = API_BASE + "/v1/RetrieveOrderPayment";
    private static final String RETRIEVE_BY_TX_URL = API_BASE + "/v1/RetrieveTransactionPayment";

    private final String clientId;
    private final String clientSecret;
    private final HttpClient http = HttpClient.newHttpClient();

    public MonCashClient(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    /** 1) OAuth2 client_credentials */
    public String getAccessToken() throws Exception {
        String basic = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(OAUTH_URL))
                .header("Authorization", "Basic " + basic)
                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials&scope=read,write"))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() / 100 != 2) {
            throw new IllegalStateException("Auth MonCash échouée (" + res.statusCode() + "): " + res.body());
        }
        JsonObject json = JsonParser.parseString(res.body()).getAsJsonObject();
        return json.get("access_token").getAsString();
    }

    /** 2) CreatePayment -> renvoie payment_token.token */
    public String createPayment(String bearer, String orderId, double amount) throws Exception {
        JsonObject payload = new JsonObject();
        payload.addProperty("orderId", orderId);
        payload.addProperty("amount", amount);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(CREATE_URL))
                .header("Authorization", "Bearer " + bearer)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() / 100 != 2) {
            throw new IllegalStateException("CreatePayment échouée (" + res.statusCode() + "): " + res.body());
        }
        JsonObject json = JsonParser.parseString(res.body()).getAsJsonObject();
        JsonObject pt = json.getAsJsonObject("payment_token");
        if (pt == null || pt.get("token") == null) {
            throw new IllegalStateException("payment_token manquant: " + res.body());
        }
        return pt.get("token").getAsString();
    }

    /** 3a) Retrieve par orderId */
    public JsonObject retrieveByOrder(String bearer, String orderId) throws Exception {
        JsonObject payload = new JsonObject();
        payload.addProperty("orderId", orderId);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(RETRIEVE_BY_ORDER_URL))
                .header("Authorization", "Bearer " + bearer)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() / 100 != 2) {
            throw new IllegalStateException("RetrieveOrder échouée (" + res.statusCode() + "): " + res.body());
        }
        return JsonParser.parseString(res.body()).getAsJsonObject();
    }

    /** 3b) Retrieve par transactionId */
    public JsonObject retrieveByTransaction(String bearer, String transactionId) throws Exception {
        JsonObject payload = new JsonObject();
        payload.addProperty("transactionId", transactionId);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(RETRIEVE_BY_TX_URL))
                .header("Authorization", "Bearer " + bearer)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() / 100 != 2) {
            throw new IllegalStateException("RetrieveTransaction échouée (" + res.statusCode() + "): " + res.body());
        }
        return JsonParser.parseString(res.body()).getAsJsonObject();
    }
}
