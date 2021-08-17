package com.peng.plant.wattmap_kakaoapi.data;

public class ImageData {

    private String path;
    private String name;
    private Double latitude;
    private Double Longitude;

    public ImageData(String path, String name, Double latitude, Double longitude) {
        this.path = path;
        this.name = name;
        this.latitude = latitude;
        Longitude = longitude;
    }

    public ImageData() {

    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return Longitude;
    }

    public void setLongitude(Double longitude) {
        Longitude = longitude;
    }
}
