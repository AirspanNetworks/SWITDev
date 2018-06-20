
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

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "message",
    "ue_add"
})
public class UeAdd {

    @JsonProperty("message")
    private String message;
    @JsonProperty("ue_add")
    private String ueAdd = null;
    @JsonProperty("info")
    private List<String> info = null;
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

    @JsonProperty("ue_add")
    public String getUeAdd() {
        return ueAdd;
    }

    @JsonProperty("ue_add")
    public void setUeAdd(String ueAdd) {
        this.ueAdd = ueAdd;
    }
    
    @JsonProperty("info")
    public void setInfo(List<String> info)
    {
    	this.info = info;
    }

    @JsonProperty("info")
	public List<String> getInfo() {
		return info;
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
