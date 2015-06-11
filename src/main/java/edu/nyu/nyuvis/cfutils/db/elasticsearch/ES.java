/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.nyu.nyuvis.cfutils.db.elasticsearch;

import edu.nyu.nyuvis.cfutils.JSON;
import edu.nyu.nyuvis.cfutils.Utils;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;

/**
 *
 * @author cristian
 */
public class ES {
    public static void main(String args[]){
        Properties p = Utils.getProperties(args);
        switch(args[0]) {
        case "Import":
            Import(p);
            break;
        default:
            throw new AssertionError();
        }
    }
    
    
    public static void export(String propieties){ export(Utils.getProperties(propieties.split(" "))); }
    public static void export(Properties properties){
        PrintWriter writer;
        try {
            Index index = ES.getIndex(properties);
            String file = properties.getProperty("file");
            Integer limit = Integer.MAX_VALUE;
            if(properties.getProperty("limit") != null)
                limit = Integer.parseInt(properties.getProperty("limit"));
            
            System.out.println("Exporting to " + file);
            writer = new PrintWriter(file, "UTF-8");
            
            index.forEach(d -> {
                writer.println(JSON.toJson(d));
            }, limit);
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            Logger.getLogger(ES.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static Settings getSettings(String file){
        Path path = Paths.get(file); 
        String result = "";
         try (Stream<String> lines = Files.lines(path, Charset.defaultCharset())) {
            result = lines.limit(1).findFirst().get();
        } catch (Exception ex){
            ex.printStackTrace();
        }
         return ImmutableSettings.settingsBuilder().loadFromSource(result).build(); 
    }
    
    public static void Import (Properties properties)
    {
        System.out.println("Starting");
        Index index = ES.getIndex(properties);
        index.create(JSON.fromFile(properties.getProperty("settings")));
        
        String file = properties.getProperty("file");
        Integer limit = Integer.MAX_VALUE;
        if(properties.getProperty("limit") != null)
            limit = Integer.parseInt(properties.getProperty("limit"));

        System.out.println("Importing from " + file + " to " + index.index + "/" + index.type);
        Integer[] count = new Integer[] {0};
        
        BulkProcessor Processor;
        Processor = BulkProcessor.builder(
                index.client,
                new BulkProcessor.Listener() {
                    @Override
                    public void beforeBulk(long executionId,
                            BulkRequest request) {
                        
                    }

                    @Override
                    public void afterBulk(long executionId,
                            BulkRequest request,
                            BulkResponse response) {
                        System.out.println(response.getItems()[0].getFailureMessage());
                    }

                    @Override
                    public void afterBulk(long executionId,
                            BulkRequest request,
                            Throwable failure) {
                        System.out.println("Bulked failure");
                        failure.printStackTrace();
                    }
                    

            })
            .setBulkActions(100)
            .setBulkSize(new ByteSizeValue(1, ByteSizeUnit.GB))
            .setFlushInterval(TimeValue.timeValueSeconds(5))
            .setConcurrentRequests(1)
            .build();
        
        Path path = Paths.get(file); 
        Charset charset = Charset.defaultCharset();
        if(properties.containsKey("charset"))
            charset = Charset.forName(properties.getProperty("charset"));
        
        try (Stream<String> lines = Files.lines(path, charset)) {
            lines.skip(0).limit(limit).forEach((String line) -> {
                Processor.add(new IndexRequest(index.index, index.type).source(line));
                count[0]++;
            });
            Processor.flush();
        } catch (Exception ex){
            ex.printStackTrace();
                System.err.println("Line: " + count[0] + "\n" + ex.getMessage());
        }
        
    }
    
    
        
    public static Index getIndex(Properties properties) { return getIndex(properties, "");}
    public static Index getIndex(Properties properties, String prefix){
        Properties p = new Properties();
        p.setProperty(prefix + "Host", "localhost");
        p.setProperty(prefix + "Port", "9300");
        p.setProperty(prefix + "Cluster", "elasticsearch");
        p.putAll(properties);
        
        String  Host = p.getProperty(prefix + "Host");
        String  Cluster = (p.getProperty(prefix + "Cluster"));
        Integer Port = Integer.parseInt(p.getProperty(prefix + "Port"));
        String  Index = p.getProperty(prefix + "Index");
        String  Type = p.getProperty(prefix + "Type");
        String  auth = p.getProperty(prefix + "Auth");
        System.out.println(auth);
        Index from = new Index(Type, Index, Cluster, Host, Port);
        
        return from;
    }
    
    public static void reindex(Properties properties){
        Properties p = new Properties();
        p.setProperty("fromHost", "localhost");
        p.setProperty("fromPort", "9300");
        p.setProperty("fromCluster", "elasticsearch");
        p.setProperty("toHost", "localhost");
        p.setProperty("toPort", "9300");
        p.setProperty("toCluster", "elasticsearch");
        p.putAll(properties);
        
        String  fromHost = p.getProperty("fromHost");
        String  fromCluster = (p.getProperty("fromCluster"));
        Integer fromPort = Integer.parseInt(p.getProperty("fromPort"));
        String  fromIndex = p.getProperty("fromIndex");
        String  fromType = p.getProperty("fromType");
        
        String  toHost = p.getProperty("toHost");
        String  toCluster = (p.getProperty("toCluster"));
        Integer toPort = Integer.parseInt(p.getProperty("toPort"));
        String  toIndex = p.getProperty("toIndex");
        String  toType = p.getProperty("toType");
        
        Index from = new Index(fromType, fromIndex, fromCluster, fromHost, fromPort);
        Index to = new Index(toType, toIndex, toCluster, toHost, toPort);
        
        BulkProcessor toProcessor = BulkProcessor.builder(
        to.client,  
        new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId,
                                   BulkRequest request) { 
                System.out.println("Going Bulk");
            } 

            @Override
            public void afterBulk(long executionId,
                                  BulkRequest request,
                                  BulkResponse response) {
                System.out.println("Bulked: " + response.getItems().length);
            } 

            @Override
            public void afterBulk(long executionId,
                                  BulkRequest request,
                                  Throwable failure) { 
                System.out.println("Bulked failure");
                failure.printStackTrace();
            } 

        })
        .setBulkActions(1000) 
        .setBulkSize(new ByteSizeValue(1, ByteSizeUnit.GB)) 
        .setFlushInterval(TimeValue.timeValueSeconds(5)) 
        .setConcurrentRequests(1) 
        .build();
                
        //Get Scroll
        SearchResponse scrollResp = from.client.prepareSearch(fromIndex)
            .setSearchType(SearchType.SCAN)
            .setScroll(new TimeValue(60000))
            .setSize(200).execute().actionGet();
        
        int count = 0;
        while (true) {
            System.out.println(count);
            for (SearchHit hit : scrollResp.getHits().getHits()) {
                toProcessor.add(new IndexRequest(toIndex, toType, hit.getId())
                    .source(hit.getSource()));
            }
            scrollResp = from.client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(600000)).execute().actionGet();
            
            if (scrollResp.getHits().getHits().length == 0) { break; }
            count++;
        }
        
        toProcessor.flush();
        
    }
    public static void reindex(String propieties){
        reindex(Utils.getProperties(propieties.split(" ")));
    }
}
