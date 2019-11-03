package Model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by PROG. TOBI on 12-Feb-18.
 */

public class HospitalModel implements Serializable{
    @SerializedName("hospital_name")
    String hospitalName;

    @SerializedName("emergency_no")
    String hospitalEmergencyNo;

    @SerializedName("position_latitude")
    Double positionLatitude;

    @SerializedName("position_longitude")
    Double positionLongitude;

    @SerializedName("hospital_details")
    String hospitalDetails;

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getHospitalEmergencyNo() {
        return hospitalEmergencyNo;
    }

    public void setHospitalEmergencyNo(String hospitalEmergencyNo) {
        this.hospitalEmergencyNo = hospitalEmergencyNo;
    }

    public Double getPositionLatitude() {
        return positionLatitude;
    }

    public void setPositionLatitude(Double positionLatitude) {
        this.positionLatitude = positionLatitude;
    }

    public Double getPositionLongitude() {
        return positionLongitude;
    }

    public void setPositionLongitude(Double positionLongitude) {
        this.positionLongitude = positionLongitude;
    }

    public String getHospitalDetails() {
        return hospitalDetails;
    }

    public void setHospitalDetails(String hospitalDetails) {
        this.hospitalDetails = hospitalDetails;
    }

    public HospitalModel(String name, String emergencyno, Double latitude, Double longitude, String details){
        this.hospitalName = name;
        this.hospitalEmergencyNo = emergencyno;
        this.positionLatitude = latitude;
        this.positionLongitude = longitude;
        this.hospitalDetails = details;

    }
}
