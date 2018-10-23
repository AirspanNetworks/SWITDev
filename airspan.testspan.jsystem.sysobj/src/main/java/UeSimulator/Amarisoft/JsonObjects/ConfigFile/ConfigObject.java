
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
    "log_options",
    "log_filename",
    "com_addr",
    "rf_driver",
    "tx_gain",
    "rx_gain",
    "bandwidth",
    "multi_ue",
    "cells",
    "ue_list"
})
public class ConfigObject {

    @JsonProperty("log_options")
    private String logOptions;
    @JsonProperty("log_filename")
    private String logFilename;
    @JsonProperty("com_addr")
    private String comAddr;
    @JsonProperty("rf_driver")
    private RfDriver rfDriver;
    @JsonProperty("global_timing_advance")
    private Integer globalTimingAdvance;
	@JsonProperty("tx_gain")
    private Double txGain;
    @JsonProperty("rx_gain")
    private Double rxGain;
    @JsonProperty("bandwidth")
    private Integer bandwidth;
    @JsonProperty("multi_ue")
    private Boolean multiUe;
    @JsonProperty("cells")
    private List<Cell> cells = null;
    @JsonProperty("ue_list")
    private List<UeList> ueList = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("log_options")
    public String getLogOptions() {
        return logOptions;
    }

    @JsonProperty("log_options")
    public void setLogOptions(String logOptions) {
        this.logOptions = logOptions;
    }

    @JsonProperty("log_filename")
    public String getLogFilename() {
        return logFilename;
    }

    @JsonProperty("log_filename")
    public void setLogFilename(String logFilename) {
        this.logFilename = logFilename;
    }

    @JsonProperty("com_addr")
    public String getComAddr() {
        return comAddr;
    }

    @JsonProperty("com_addr")
    public void setComAddr(String comAddr) {
        this.comAddr = comAddr;
    }

    @JsonProperty("rf_driver")
    public RfDriver getRfDriver() {
        return rfDriver;
    }

    @JsonProperty("rf_driver")
    public void setRfDriver(RfDriver rfDriver) {
        this.rfDriver = rfDriver;
    }
    
    @JsonProperty("global_timing_advance")
    public Integer getGlobalTimingAdvance() {
		return globalTimingAdvance;
	}
    @JsonProperty("global_timing_advance")
	public void setGlobalTimingAdvance(Integer globalTimingAdvance) {
		this.globalTimingAdvance = globalTimingAdvance;
	}
    
    @JsonProperty("tx_gain")
    public Double getTxGain() {
        return txGain;
    }

    @JsonProperty("tx_gain")
    public void setTxGain(Double txGain) {
        this.txGain = txGain;
    }

    @JsonProperty("rx_gain")
    public Double getRxGain() {
        return rxGain;
    }

    @JsonProperty("rx_gain")
    public void setRxGain(Double rxGain) {
        this.rxGain = rxGain;
    }

    @JsonProperty("bandwidth")
    public Integer getBandwidth() {
        return bandwidth;
    }

    @JsonProperty("bandwidth")
    public void setBandwidth(Integer bandwidth) {
        this.bandwidth = bandwidth;
    }

    @JsonProperty("multi_ue")
    public Boolean getMultiUe() {
        return multiUe;
    }

    @JsonProperty("multi_ue")
    public void setMultiUe(Boolean multiUe) {
        this.multiUe = multiUe;
    }

    @JsonProperty("cells")
    public List<Cell> getCells() {
        return cells;
    }

    @JsonProperty("cells")
    public void setCells(List<Cell> cells) {
        this.cells = cells;
    }

    @JsonProperty("ue_list")
    public List<UeList> getUeList() {
        return ueList;
    }

    @JsonProperty("ue_list")
    public void setUeList(List<UeList> ueList) {
        this.ueList = ueList;
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
