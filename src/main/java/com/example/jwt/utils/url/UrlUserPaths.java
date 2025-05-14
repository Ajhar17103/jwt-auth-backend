package com.example.jwt.utils.url;

public interface UrlUserPaths {

    String     BASE            =  UrlBaseSupplier.API_V1 + "/users";

    String     USER_LIST       =  "/list";
    String     BY_ID           =  "/{id}";
    String     UPDATE          =  "/update/{id}";
    String     DELETE          =  "/delete/{id}";
}
