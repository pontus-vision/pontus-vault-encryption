package com.pontusvision.vault;

import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.json.Json;
import com.bettercloud.vault.json.JsonObject;
import com.bettercloud.vault.json.JsonValue;
import com.bettercloud.vault.rest.Rest;
import com.bettercloud.vault.rest.RestException;
import com.bettercloud.vault.rest.RestResponse;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


public class Main
{

  public static void main(String[] args)
  {
    try
    {
      final VaultConfig config = new VaultConfig().build();

      String keyNamePrefix = args.length == 0 ? "key" : args[0];

      int numEntries = 1000000;
      long startTime = System.nanoTime();
      for (int i = 0; i < numEntries; i++)
      {
        String keyName = keyNamePrefix + i;

        final RestResponse keyCreation = new Rest()// NOPMD
                                                   .url(config.getAddress() + "/v1/transit/keys/" + keyName)
                                                   .header("X-Vault-Token", config.getToken())
                                                   .optionalHeader("X-Vault-Namespace", config.getNameSpace())
                                                   .connectTimeoutSeconds(config.getOpenTimeout())
                                                   .readTimeoutSeconds(config.getReadTimeout())
                                                   .sslVerification(config.getSslConfig().isVerify())
                                                   .sslContext(config.getSslConfig().getSslContext())
                                                   .body(
                                                       "{\"type\": \"aes256-gcm96\", \"exportable\": true }".getBytes())
                                                   .post();

        // Validate response
        int restStatus = keyCreation.getStatus();
        if (!(restStatus >= 200 && restStatus < 300))
        {
          throw new VaultException("Vault responded with HTTP status code: " + keyCreation.getStatus()
              + "\nResponse body: " + new String(keyCreation.getBody(), StandardCharsets.UTF_8),
              keyCreation.getStatus());
        }

        final RestResponse keyData = new Rest()// NOPMD
                                               .url(
                                                   config.getAddress() + "/v1/transit/export/encryption-key/" + keyName)
                                               .header("X-Vault-Token", config.getToken())
                                               .optionalHeader("X-Vault-Namespace", config.getNameSpace())
                                               .connectTimeoutSeconds(config.getOpenTimeout())
                                               .readTimeoutSeconds(config.getReadTimeout())
                                               .sslVerification(config.getSslConfig().isVerify())
                                               .sslContext(config.getSslConfig().getSslContext())
                                               .get();

        restStatus = keyData.getStatus();
        if (!(restStatus >= 200 && restStatus < 300))
        {
          throw new VaultException("Vault responded with HTTP status code: " + keyData.getStatus()
              + "\nResponse body: " + new String(keyData.getBody(), StandardCharsets.UTF_8),
              keyData.getStatus());
        }

        String strBody = new String(keyData.getBody());

//        System.out.println(strBody);

        JsonValue  rawDataJson = Json.parse(strBody);
        JsonObject keysJson    = rawDataJson.asObject().get("data").asObject().get("keys").asObject();

        Integer latestVersion = keysJson.names().stream().mapToInt(value -> Integer.parseInt(value)).max().getAsInt();

        String keyB64 = keysJson.getString(latestVersion.toString());

        byte[] keyBin = Base64.getDecoder().decode(keyB64);

        //      KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        //      keyGenerator.init(256);
        SecretKey key    = new SecretKeySpec(keyBin, "AES"); //AES-256 key
        Cipher    cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        String plainText = "my secret message!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!";
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        String encryptedStr = Base64.getEncoder().encodeToString(encrypted);

//        System.out.println("Before: " + plainText + "; after = " + encryptedStr);

        //      JsonObject retVal = new JsonObject(strBody);

        //      Base64.getDecoder()
      }

      long endTime = System.nanoTime();

      System.out.println("msgs/sec = " + 10e9*numEntries/(endTime-startTime) );

    }
    catch (VaultException | RestException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e)
    {
      e.printStackTrace();
    }

  }
}
