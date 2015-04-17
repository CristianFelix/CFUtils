/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.nyu.nyuvis.cfutils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 *
 * @author cristian
 */
public class JSON {
    public static JsonObject getObject(String json){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonParser jp = new JsonParser();
            JsonElement je = jp.parse(json);
            return je.getAsJsonObject();
    }
    
    public static String toJson(Object obj){
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(obj);
        return json;
    }
    
    public static JsonObject fromFile(String file){
        JsonObject jsonObject = new JsonObject();

        try {
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(new FileReader(file));
            jsonObject = jsonElement.getAsJsonObject();
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } 

        return jsonObject;
    }
}
