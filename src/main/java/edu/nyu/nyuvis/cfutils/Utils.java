package edu.nyu.nyuvis.cfutils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author cristian
 */
public  class Utils {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32;22m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
      
    public static Properties getProperties(String[] args){
        return getProperties(args, null);
    }
    
    public static Properties getProperties(String file){
        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = new FileInputStream(file);
            prop.load(input);
        } catch (IOException ex) {
            
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    
                }
            }
        }
        return  prop;
    }
    
    public static Properties getProperties(String[] args, Properties def){
        Properties p = new Properties(def);
        String candidateKey = "";
        for (String arg : args) {
            if (arg.startsWith("-")) {
                if(candidateKey.length() > 0)
                    p.setProperty(candidateKey, "");
                candidateKey = arg.substring(1);
            } else {
                p.setProperty(candidateKey, arg);
                candidateKey = "";
            }
        }
         if(candidateKey.length() > 0)
            p.setProperty(candidateKey, "");
        
        return p;
    }
    
    
    //Print Utils
    public static void info(Object obj, boolean newLine){
        if(obj != null)
            if(newLine)
                System.out.println(ANSI_BLUE + obj.toString() + ANSI_RESET);
            else
                System.out.print(ANSI_BLUE + obj.toString() + ANSI_RESET);
    }
    
    public static void info(Object obj){
        info(obj, true);
    }
    public static void error(Object obj){
        if(obj != null)
            System.out.println(ANSI_RED + obj.toString() + ANSI_RESET);
    }
    public static void out(Object obj){
        System.out.println(obj);
    }
    
    public static void success(Object obj){
        if(obj != null)
            System.out.println(ANSI_GREEN + obj.toString() + ANSI_RESET);
    }
    
}
