package com.delta.activities;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by naritc on 13-Apr-18.
 */

public class VolleyUtils {

    public static Map<String, String> getBasicAuthHeaders() {
        final Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Basic " + Constants.CLIENT_CREDENTIALS_ENCODED);

        return headers;
    }

    public static Map<String, String> getBearerAuthheaders(String accessToken) {
        final Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + accessToken);

        return headers;
    }

}
