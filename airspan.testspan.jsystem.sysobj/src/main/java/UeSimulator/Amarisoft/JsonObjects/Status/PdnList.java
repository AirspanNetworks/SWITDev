
package UeSimulator.Amarisoft.JsonObjects.Status;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "apn",
    "ipv4"
})
public class PdnList {

    @JsonProperty("apn")
    private String apn;
    @JsonProperty("ipv4")
    private String ipv4;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("apn")
    public String getApn() {
        return apn;
    }

    @JsonProperty("apn")
    public void setApn(String apn) {
        this.apn = apn;
    }

    @JsonProperty("ipv4")
    public String getIpv4() {
        return ipv4;
    }

    @JsonProperty("ipv4")
    public void setIpv4(String ipv4) {
        this.ipv4 = ipv4;
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
