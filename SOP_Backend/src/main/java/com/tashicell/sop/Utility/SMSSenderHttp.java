package com.tashicell.sop.Utility;

import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by sw_en on 5/25/2022.
 */
public class SMSSenderHttp {
    public String sendSMS(String mobileNumber, String messageContent){
        sms sendsms = new sms();
        String prefix= "975";
        if(mobileNumber.startsWith(prefix)){
            mobileNumber = mobileNumber;
        }
        else{
            mobileNumber = prefix.concat(mobileNumber);
        }

        String result = "Failed";

        HashMap<String, String> hashmap = new HashMap<>();
        // method to add the key,value pair in hashmap
        hashmap.put("UserName", "alert");
        hashmap.put("PassWord", "alert");
        /*hashmap.put("UserName", "sdulcms");
        hashmap.put("PassWord", "Sdulcms");*/
        hashmap.put("UserData", messageContent);
        hashmap.put("Concatenated", "1");
        hashmap.put("Mode", "0");
        hashmap.put("SenderId", "TashiCell");
        hashmap.put("Deferred", "false");
        hashmap.put("Number", mobileNumber);
        hashmap.put("Dsr", "false");
        hashmap.put("Flash", "0");
        hashmap.put("Date", "");
        hashmap.put("Hour", "0");
        hashmap.put("Minute", "0");
        hashmap.put("Second", "0");
        hashmap.put("VP", "720");
        hashmap.put("VlrData", "0");
        hashmap.put("mbt", "0");


        Response response = null;
        try {
            response = sendsms.post(hashmap);
            if(response.code()==200){
                result="Success";
            }
            else {
                result="Failed Sending SMS";
            }
            response.body().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
