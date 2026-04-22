package atizay.service;

import com.google.gson.JsonObject;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class MonCashService {

    @Value("${moncash.client.id:115c98eac1fe339e5ade88d01ef18720}")
    private String clientId;

    @Value("${moncash.client.secret:smHHGmemIK9HZeECX8ZnRJWzq4mIhiy1bSY_KU29mxOKPjsYBzkZY9wnzBEdvEkO}")
    private String clientSecret;

    private static final String GATEWAY_URL = "https://sandbox.moncashbutton.digicelgroup.com/Moncash-middleware";

    private MonCashClient monCashClient;

    @PostConstruct
    public void init() {
        this.monCashClient = new MonCashClient(clientId, clientSecret);
    }

    /**
     * Initialiser un paiement et obtenir l'URL de redirection
     */
    public String initiatePayment(String orderId, double amount) throws Exception {
        String accessToken = monCashClient.getAccessToken();
        String token = monCashClient.createPayment(accessToken, orderId, amount);
        return GATEWAY_URL + "/Payment/Redirect?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    /**
     * Vérifier le statut d'un paiement par orderId
     */
    public JsonObject verifyPaymentByOrder(String orderId) throws Exception {
        String accessToken = monCashClient.getAccessToken();
        return monCashClient.retrieveByOrder(accessToken, orderId);
    }

    /**
     * Vérifier le statut d'un paiement par transactionId
     */
    public JsonObject verifyPaymentByTransaction(String transactionId) throws Exception {
        String accessToken = monCashClient.getAccessToken();
        return monCashClient.retrieveByTransaction(accessToken, transactionId);
    }
}
