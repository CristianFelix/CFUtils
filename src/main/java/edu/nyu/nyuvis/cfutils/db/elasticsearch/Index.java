package edu.nyu.nyuvis.cfutils.db.elasticsearch;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;

public class Index {
    
    Client client;
    String index;
    String type;
    
    public Index(String index, String type){
        this(type, index, "elasticsearch", "localhost", 9300);
    }
    
    public Index(String type, String index, String cluster, String server, int port){
        Settings settings = ImmutableSettings.settingsBuilder()
            .put("cluster.name", cluster).build();
         client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress(server, port));
         this.index = index;
         this.type = type;
    }
    public GetResponse get(String id){
        GetResponse response = client.prepareGet(this.index, this.type, id)
            .execute()
            .actionGet();
        return response;
    }
    
    public boolean Exists(){
        IndicesExistsRequest action = new IndicesExistsRequest(this.index);
        return this.client.admin().indices().exists(action).actionGet().isExists();
    }
    
    public CreateIndexResponse create(JsonObject settings){
        CreateIndexRequest action = new CreateIndexRequest(this.index);
        
        if(settings.get("settings") != null){
            String setString = settings.get("settings").getAsJsonObject().toString();
            action.settings(setString);
        }
        if(settings.get("mappings") != null){
            JsonObject mappings = settings.get("mappings").getAsJsonObject();
            mappings.entrySet().forEach(e -> {
                action.mapping(e.getKey(), e.getValue().toString());
            });
        }
        
        if(Exists()){
            DeleteIndexRequest deleteAction = new DeleteIndexRequest(this.index);
            this.client.admin().indices().delete(deleteAction).actionGet();
        }
        
        
        return this.client.admin().indices().create(action).actionGet();
    }
    
    public SearchResponse search() {
        SearchResponse response = client.prepareSearch(this.index).execute().actionGet();
        return response;
    }
    
    public void forEach(Consumer action, int limit){
        SearchResponse scrollResp = this.client.prepareSearch(this.index)
            .setSearchType(SearchType.SCAN)
            .setScroll(new TimeValue(60000))
            .setSize(200).execute().actionGet();
        
        int count = 0;
        while (true) {
            for (SearchHit hit : scrollResp.getHits().getHits()) {
                if(count >= limit)
                {
                    return;
                }
                count++;
                action.accept(hit.getSource());
            }
            scrollResp = this.client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(600000)).execute().actionGet();
            
            if (scrollResp.getHits().getHits().length == 0) { break; }
            
        }
    }
    
    
    
     public SearchRequestBuilder prepareSearch() {
        return client.prepareSearch(this.index);
    }
    
    
    public void close(){
        client.close();
    }


}
