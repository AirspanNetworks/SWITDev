
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
	"pci",
	"dl_earfcn",
    "mode",
    "uldl_config",
    "sp_config",
    "n_rb_dl",
    "ul_earfcn",
    "n_rb_ul",
})
public class CellStatus {

    @JsonProperty("pci")
    private Integer pci;
    @JsonProperty("dl_earfcn")
    private Integer dl_earfcn;
    @JsonProperty("mode")
    private String mode;
    @JsonProperty("uldl_config")
    private Integer uldl_config;
    @JsonProperty("sp_config")
    private Integer sp_config;
    @JsonProperty("n_rb_dl")
    private Integer n_rb_dl;
    @JsonProperty("ul_earfcn")
    private Integer ul_earfcn;
    @JsonProperty("n_rb_ul")
    private Integer n_rb_ul;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("dl_earfcn")
    public Integer getDlEarfcn() {
        return dl_earfcn;
    }

    @JsonProperty("dl_earfcn")
    public void setDlEarfcn(Integer dlEarfcn) {
        this.dl_earfcn = dlEarfcn;
    }

    @JsonProperty("pci")
    public Integer getPci() {
        return pci;
    }

    @JsonProperty("pci")
    public void setPci(Integer pci) {
        this.pci = pci;
    }

    @JsonProperty("mode")
    public String getMode() {
        return mode;
    }

    @JsonProperty("mode")
    public void setMode(String mode) {
        this.mode = mode;
    }
    
    @JsonProperty("uldl_config")
    public Integer getUldlConfig() {
        return uldl_config;
    }

    @JsonProperty("uldl_config")
    public void setUldlConfig(Integer uldl_config) {
        this.uldl_config = uldl_config;
    }

    @JsonProperty("sp_config")
    public Integer getSpConfig() {
        return sp_config;
    }

    @JsonProperty("sp_config")
    public void setSpConfig(Integer sp_config) {
        this.sp_config = sp_config;
    }

    @JsonProperty("n_rb_dl")
    public Integer getNRbDl() {
        return n_rb_dl;
    }

    @JsonProperty("n_rb_dl")
    public void setNRbDl(Integer n_rb_dl) {
        this.n_rb_dl = n_rb_dl;
    }
    
    @JsonProperty("ul_earfcn")
    public Integer getUl_earfcnn() {
        return ul_earfcn;
    }

    @JsonProperty("ul_earfcn")
    public void setUl_earfcn(Integer ul_earfcn) {
        this.ul_earfcn = ul_earfcn;
    }

    @JsonProperty("n_rb_ul")
    public Integer getNRbUl() {
        return n_rb_ul;
    }

    @JsonProperty("n_rb_ul")
    public void setNRbUl(Integer n_rb_ul) {
        this.n_rb_ul = n_rb_ul;
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
