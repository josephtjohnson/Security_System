module com.udacity.catpoint.image{
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.core;
    requires software.amazon.awssdk.services.rekognition;
    requires software.amazon.awssdk.regions;
    requires java.desktop;
    requires org.slf4j;
    exports service;
}