package com.peng.plant.wattmap_kakaoapi.data;


import org.jetbrains.annotations.NotNull;

import ted.gun0912.clustering.clustering.TedClusterItem;
import ted.gun0912.clustering.geometry.TedLatLng;

public class ImageData implements TedClusterItem {

    private String path;
    private String name;
    private Double latitude;
    private Double Longitude;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    private int count = 1;

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

    public void setLatitude(String attribute) {
    }

    public void setLongitude(String attribute) {
    }


    @NotNull
    @Override
    public TedLatLng getTedLatLng() {
        return new TedLatLng(getTedLatLng().getLatitude(),getTedLatLng().getLongitude());
    }
}
