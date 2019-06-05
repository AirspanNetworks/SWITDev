
package UeSimulator.Amarisoft.JsonObjects.ConfigFile;

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
    "as_release",
    "cell_index",
    "ue_category",
    "sim_algo",
    "half_duplex",
    "forced_cqi",
    "forced_ri",
    "imsi",
    "K",
    "op",
    "tun_setup_script",
    "sim_events",
    "power_control_enabled",
    "channel",
    "speed",
    "direction",
    "position"
})
public class UeList {

	@JsonProperty("ue_id")
    private Integer ueId;
    @JsonProperty("as_release")
    private Integer asRelease;
    @JsonProperty("cell_index")
    private Integer cellIndex;
    @JsonProperty("ue_category")
    private Integer ueCategory;
    @JsonProperty("sim_algo")
    private String simAlgo;
    @JsonProperty("half_duplex")
    private Boolean halfDuplex;
    @JsonProperty("forced_cqi")
    private Integer forcedCqi;
    @JsonProperty("forced_ri")
    private Integer forcedRi;
    @JsonProperty("imeisv")
	private String imeisv;
    @JsonProperty("imsi")
    private String imsi;
    @JsonProperty("K")
    private String k;
    @JsonProperty("op")
    private String op;
    @JsonProperty("tun_setup_script")
    private String tunSetupScript;
    @JsonProperty("sim_events")
    private List<SimEvent> simEvents = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    //ue_id
    @JsonProperty("power_control_enabled")
    private Boolean powerControlEnabled;
    @JsonProperty("channel")
    private Channel channel;
    @JsonProperty("speed")
    private Float speed;
    @JsonProperty("direction")
    private Float direction;
    @JsonProperty("position")
    private List<Float> position;

    @JsonProperty("ue_id")
    public Integer getUeId() {
        return ueId;
    }

    @JsonProperty("ue_id")
    public void setUeId(Integer ueId) {
        this.ueId = ueId;
    }
    
    @JsonProperty("as_release")
    public Integer getAsRelease() {
        return asRelease;
    }

    @JsonProperty("as_release")
    public void setAsRelease(Integer asRelease) {
        this.asRelease = asRelease;
    }
    
    @JsonProperty("cell_index")
    public Integer getCellIndex() {
        return cellIndex;
    }

    @JsonProperty("cell_index")
    public void setCellIndex(Integer cellIndex) {
        this.cellIndex = cellIndex;
    }

    @JsonProperty("ue_category")
    public Integer getUeCategory() {
        return ueCategory;
    }

    @JsonProperty("ue_category")
    public void setUeCategory(Integer ueCategory) {
        this.ueCategory = ueCategory;
    }

    @JsonProperty("sim_algo")
    public String getSimAlgo() {
        return simAlgo;
    }

    @JsonProperty("sim_algo")
    public void setSimAlgo(String simAlgo) {
        this.simAlgo = simAlgo;
    }

    @JsonProperty("half_duplex")
    public Boolean getHalfDuplex() {
        return halfDuplex;
    }

    @JsonProperty("half_duplex")
    public void setHalfDuplex(Boolean halfDuplex) {
        this.halfDuplex = halfDuplex;
    }

    @JsonProperty("forced_cqi")
    public Integer getForcedCqi() {
        return forcedCqi;
    }

    @JsonProperty("forced_cqi")
    public void setForcedCqi(Integer forcedCqi) {
        this.forcedCqi = forcedCqi;
    }

    @JsonProperty("forced_ri")
    public Integer getForcedRi() {
        return forcedRi;
    }

    @JsonProperty("forced_ri")
    public void setForcedRi(Integer forcedRi) {
        this.forcedRi = forcedRi;
    }
    
    @JsonProperty("imeisv")
    public String getImeisv() {
    	return imeisv;
	}
    
    @JsonProperty("imeisv")
    public void setImeisv(String imeisv) {
    	this.imeisv = imeisv;
	}

    @JsonProperty("imsi")
    public String getImsi() {
        return imsi;
    }

    @JsonProperty("imsi")
    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    @JsonProperty("K")
    public String getK() {
        return k;
    }

    @JsonProperty("K")
    public void setK(String k) {
        this.k = k;
    }

    @JsonProperty("op")
    public String getOp() {
        return op;
    }

    @JsonProperty("op")
    public void setOp(String op) {
        this.op = op;
    }

    @JsonProperty("tun_setup_script")
    public String getTunSetupScript() {
        return tunSetupScript;
    }

    @JsonProperty("tun_setup_script")
    public void setTunSetupScript(String tunSetupScript) {
        this.tunSetupScript = tunSetupScript;
    }

    @JsonProperty("sim_events")
    public List<SimEvent> getSimEvents() {
        return simEvents;
    }

    @JsonProperty("sim_events")
    public void setSimEvents(List<SimEvent> simEvents) {
        this.simEvents = simEvents;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @JsonProperty("power_control_enabled")
    public Boolean getPowerControlEnabled() {
        return powerControlEnabled;
    }

    @JsonProperty("power_control_enabled")
    public void setPowerControlEnabled(Boolean powerControlEnabled) {
        this.powerControlEnabled = powerControlEnabled;
    }

    @JsonProperty("channel")
    public Channel getChannel() {
        return channel;
    }

    @JsonProperty("channel")
    public void setChannel(Channel channel) {
        this.channel = channel;
    }
    
    @JsonProperty("speed")
    public Float getSpeed() {
        return speed;
    }

    @JsonProperty("speed")
    public void setSpeed(Float speed) {
        this.speed = speed;
    }
    
    @JsonProperty("direction")
    public Float getDirection() {
        return direction;
    }

    @JsonProperty("direction")
    public void setDirection(Float direction) {
        this.direction = direction;
    }
    
    @JsonProperty("position")
    public List<Float> getPosition() {
        return position;
    }

    @JsonProperty("position")
    public void setPosition(List<Float> position) {
        this.position = position;
    }
}
