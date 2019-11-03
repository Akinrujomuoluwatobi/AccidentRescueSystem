package Model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;


public class AccidentResponse {

    @SerializedName("total_count")
    private int totalCount;

    @SerializedName("incomplete_results")
    private boolean incomplete;

    @SerializedName("items")
    public List<HospitalModel> hospital = new ArrayList<>();

}
