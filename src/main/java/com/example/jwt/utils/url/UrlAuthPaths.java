package com.example.jwt.utils.url;

public interface UrlAuthPaths {

    String   BASE                     =  UrlBaseSupplier.API_V1 + "/auth";

    String   OAUTH2                 =  "/oauth2";

    String   REGISTER                 =  "/register";
    String   BULK_REGISTER            =  "/bulk-register";
    String   BULK_REGISTER_REPORT     =  "/bulk-register-report/{filename}";
    String   LOGIN                    =  "/login";
    String   REFRESH_TOKEN            =  "/refresh-token";
    String   LOGOUT                   =  "/logout";
}
