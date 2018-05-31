
package UeSimulator.Amarisoft.JsonObjects.ConfigFile;

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
    "dl_earfcn",
    "n_antenna_dl",
    "n_antenna_ul",
    "global_timing_advance"
})
public class Cell {

    @JsonProperty("dl_earfcn")
    private Integer dlEarfcn;
    @JsonProperty("n_antenna_dl")
    private Integer nAntennaDl;
    @JsonProperty("n_antenna_ul")
    private Integer nAntennaUl;
    @JsonProperty("global_timing_advance")
    private Integer globalTimingAdvance;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("dl_earfcn")
    public Integer getDlEarfcn() {
        return dlEarfcn;
    }

    @JsonProperty("dl_earfcn")
    public void setDlEarfcn(Integer dlEarfcn) {
        this.dlEarfcn = dlEarfcn;
    }

    @JsonProperty("n_antenna_dl")
    public Integer getNAntennaDl() {
        return nAntennaDl;
    }

    @JsonProperty("n_antenna_dl")
    public void setNAntennaDl(Integer nAntennaDl) {
        this.nAntennaDl = nAntennaDl;
    }

    @JsonProperty("n_antenna_ul")
    public Integer getNAntennaUl() {
        return nAntennaUl;
    }

    @JsonProperty("n_antenna_ul")
    public void setNAntennaUl(Integer nAntennaUl) {
        this.nAntennaUl = nAntennaUl;
    }

    @JsonProperty("global_timing_advance")
    public Integer getGlobalTimingAdvance() {
        return globalTimingAdvance;
    }

    @JsonProperty("global_timing_advance")
    public void setGlobalTimingAdvance(Integer globalTimingAdvance) {
        this.globalTimingAdvance = globalTimingAdvance;
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
