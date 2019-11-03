package com.royalteck.progtobi.accidentrescuesystem.Network;


import java.util.ArrayList;

import Model.AccidentResponse;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by PROG. TOBI on 10-Jul-17.
 */

public interface APIService {
    /*@FormUrlEncoded
    @POST("")
    Call<> userLogin(@Field("uname") String uname,
                     @Field("pk") String upass);*/

    @GET("add_hospital.php")
    Call<AccidentResponse> getData();


}
