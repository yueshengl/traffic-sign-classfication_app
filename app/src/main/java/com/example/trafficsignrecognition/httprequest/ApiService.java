package com.example.trafficsignrecognition.httprequest;

import com.example.trafficsignrecognition.dto.ResetPasswordDTO;
import com.example.trafficsignrecognition.dto.UpdatePasswordDTO;
import com.example.trafficsignrecognition.dto.UserDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    // 注册接口
    @POST("/api/user/register")
    Call<String> register(@Body UserDTO userDTO);

    // 登录接口
    @POST("/api/user/login")
    Call<String> login(@Body UserDTO userDTO);

    @POST("/api/user/updatePassword")
    Call<String> updatePassword(@Body UpdatePasswordDTO updatePasswordDTO);

    @POST("/api/user/resetPassword")
    Call<String> resetPassword(@Body ResetPasswordDTO resetPasswordDTO);

}

