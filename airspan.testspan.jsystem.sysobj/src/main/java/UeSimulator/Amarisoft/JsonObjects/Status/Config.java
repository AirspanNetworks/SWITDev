
package UeSimulator.Amarisoft.JsonObjects.Status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import UeSimulator.Amarisoft.JsonObjects.ConfigFile.Cell;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "message",
    "ue_add"
})
public class Config {

    @JsonProperty("message")
    private String message;
    @JsonProperty("version")
    private String version;
    @JsonProperty("time")
    private Double time;
    @JsonProperty("type")
    private String type;
   // @JsonProperty("cells")
   // private List<Cell> cells = null;
    
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }
    
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }
    
    @JsonProperty("time")
    public Double getTime() {
        return time;
    }

    @JsonProperty("time")
    public void setTime(Double time) {
        this.time = time;
    }
    
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
    
    

}
