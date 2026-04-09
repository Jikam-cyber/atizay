package atizay.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.net.URLEncoder;

@Service
public class MonCashService {

    // === SANDBOX CREDENTIALS ===
    private static final String CLIENT_ID     = "115c98eac1fe339e5ade88d01ef18720";
    private static final String CLIENT_SECRET = "smHHGmemIK9HZeECX8ZnRJWzq4mIhiy1bSY_KU29mxOKPjsYBzkZY9wnzBEdvEkO";

    // === API ENDPOINTS (SANDBOX) ===
    private static final String API_BASE              = "https://sandbox.moncashbutton.digicelgroup.com/Api";
    private static final String OAUTH_URL             = API_BASE + "/oauth/token";
    private static final String CREATE_URL            = API_BASE + "/v1/CreatePayment";
    private static final String GATEWAY_URL           = "https://sandbox.moncashbutton.digicelgroup.com/Moncash-middleware";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * 1) Obtenir un token d'accès OAuth2
     */
    public String getAccessToken() throws Exception {
        String basicAuth = Base64.getEncoder().encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OAUTH_URL))
                .header("Authorization", "Basic " + basicAuth)
                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials&scope=read,write"))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("Échec de l'authentification MonCash : " + response.body());
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        return json.get("access_token").getAsString();
    }

    /**
     * 2) Créer un paiement et obtenir l'URL de redirection
     */
    public String initiatePayment(String orderId, double amount) throws Exception {
        String accessToken = getAccessToken();

        JsonObject payload = new JsonObject();
        payload.addProperty("orderId", orderId);
        payload.addProperty("amount", amount);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CREATE_URL))
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Échec de la création du paiement MonCash : " + response.body());
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonObject paymentToken = json.getAsJsonObject("payment_token");
        
        if (paymentToken == null || paymentToken.get("token") == null) {
            throw new RuntimeException("Token de paiement manquant dans la réponse MonCash");
        }

        String token = paymentToken.get("token").getAsString();
        
        // URL de redirection finale vers la passerelle Digicel
        return GATEWAY_URL + "/Payment/Redirect?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }
}
