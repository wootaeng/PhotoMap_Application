package com.peng.plant.wattmap_kakaoapi;

import android.media.ExifInterface;

public class ImagelatlngEXIF {

    private boolean valid = false;
    private Float lat, lon;

    public ImagelatlngEXIF(ExifInterface exif){
        String attrLATITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        String attrLATITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
        String attrLONGITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
        String attrLONGITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
        
        if ((attrLATITUDE != null) && (attrLATITUDE_REF != null) && (attrLONGITUDE != null) && (attrLONGITUDE_REF != null)){
            valid =true;
            
            if (attrLATITUDE_REF.equals("N")){
                lat = convertToDegree(attrLATITUDE);
            }else {
                lat = 0 - convertToDegree(attrLATITUDE);
            }

            if (attrLONGITUDE_REF.equals("E")){
                lon = convertToDegree(attrLONGITUDE);
            } else {
                lon = 0 - convertToDegree(attrLONGITUDE);
            }
        }
    }

    private Float convertToDegree(String stringDMS) {
        Float result = null;
        String[] DMS = stringDMS.split(",",3);
        
        String[] stringD = DMS[0].split("/",2);
        Double D0 = new Double(stringD[0]);
        Double D1 = new Double(stringD[1]);
        Double FloatD = D0 / D1;

        String[] stringM = DMS[1].split("/",2);
        Double M0 = new Double(stringM[0]);
        Double M1 = new Double(stringM[1]);
        Double FloatM = M0 / M1;

        String[] stringS = DMS[2].split("/",2);
        Double S0 = new Double(stringS[0]);
        Double S1 = new Double(stringS[1]);
        Double FloatS = S0 / S1;

        result = new Float(FloatD + (FloatM/60) + (FloatS / 3600));

        return result;

    }

    public boolean isValid(){
        return valid;
    }

    @Override
    public String toString(){
        return (String.valueOf(lat) + ", " + String.valueOf(lon));
    }

    public Float getLat(){
        return lat;
    }

    public Float getLon(){
        return lon;
    }

    public int getLatE6(){
        return (int) (lat * 1000000);
    }

    public int getLonE6(){
        return (int) (lon * 1000000);
    }

}
