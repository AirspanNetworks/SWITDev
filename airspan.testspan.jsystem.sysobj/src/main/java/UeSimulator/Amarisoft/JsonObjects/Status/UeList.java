
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
    "imsi",
    "ue_id",
    "category",
    "timing_advance",
    "rnti",
    "cell_index",
    "power_on",
    "ip",
    "dl_bitrate",
    "ul_bitrate",
    "dl_rx_count",
    "dl_retx_count",
    "dl_err_count",
    "ul_tx_count",
    "ul_retx_count",
    "dl_mcs",
    "ul_mcs",
    "rrc_state",
    "cell_id",
    "rsrp",
    "snr",
    "emm_state"
})
public class UeList {

    @JsonProperty("imsi")
    private String imsi;
    @JsonProperty("ue_id")
    private Integer ueId;
    @JsonProperty("category")
    private Integer category;
    @JsonProperty("timing_advance")
    private Integer timingAdvance;
    @JsonProperty("rnti")
    private Integer rnti;
    @JsonProperty("cell_index")
    private Integer cell_index;
    @JsonProperty("power_on")
    private Boolean powerOn;
    @JsonProperty("ip")
	private String ip;
    @JsonProperty("dl_bitrate")
    private Double dlBitrate;
    @JsonProperty("ul_bitrate")
    private Double ulBitrate;
    @JsonProperty("dl_rx_count")
    private Integer dlRxCount;
    @JsonProperty("dl_retx_count")
    private Integer dlRetxCount;
    @JsonProperty("dl_err_count")
    private Integer dlErrCount;
    @JsonProperty("ul_tx_count")
    private Integer ulTxCount;
    @JsonProperty("ul_retx_count")
    private Integer ulRetxCount;
    @JsonProperty("dl_mcs")
    private Double dlMcs;
    @JsonProperty("ul_mcs")
    private Integer ulMcs;
    @JsonProperty("rrc_state")
    private String rrcState;
    @JsonProperty("cell_id")
    private Integer cellId;
    @JsonProperty("rsrp")
    private Double rsrp;
    @JsonProperty("snr")
    private Double snr;
    @JsonProperty("emm_state")
    private String emmState;
    @JsonProperty("pdn_list")
    private List<PdnList> pdnList = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("imsi")
    public String getImsi() {
        return imsi;
    }

    @JsonProperty("imsi")
    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    @JsonProperty("ue_id")
    public Integer getUeId() {
        return ueId;
    }

    @JsonProperty("ue_id")
    public void setUeId(Integer ueId) {
        this.ueId = ueId;
    }

    @JsonProperty("category")
    public Integer getCategory() {
        return category;
    }

    @JsonProperty("category")
    public void setCategory(Integer category) {
        this.category = category;
    }

    @JsonProperty("timing_advance")
    public Integer getTimingAdvance() {
        return timingAdvance;
    }

    @JsonProperty("timing_advance")
    public void setTimingAdvance(Integer timingAdvance) {
        this.timingAdvance = timingAdvance;
    }

    @JsonProperty("rnti")
    public Integer getRnti() {
        return rnti;
    }

    @JsonProperty("rnti")
    public void setRnti(Integer rnti) {
        this.rnti = rnti;
    }
    
    @JsonProperty("cell_index")
    public Integer getCellIndex() {
        return cell_index;
    }

    @JsonProperty("cell_index")
    public void setCellIndex(Integer cell_index) {
        this.cell_index = cell_index;
    }

    @JsonProperty("power_on")
    public Boolean getPowerOn() {
        return powerOn;
    }

    @JsonProperty("power_on")
    public void setPowerOn(Boolean powerOn) {
        this.powerOn = powerOn;
    }

    @JsonProperty("ip")
    public String getIp() {
        return ip;
    }

    @JsonProperty("ip")
    public void setIp(String ip) {
        this.ip = ip;
    }
    
    @JsonProperty("dl_bitrate")
    public Double getDlBitrate() {
        return dlBitrate;
    }

    @JsonProperty("dl_bitrate")
    public void setDlBitrate(Double dlBitrate) {
        this.dlBitrate = dlBitrate;
    }

    @JsonProperty("ul_bitrate")
    public Double getUlBitrate() {
        return ulBitrate;
    }

    @JsonProperty("ul_bitrate")
    public void setUlBitrate(Double ulBitrate) {
        this.ulBitrate = ulBitrate;
    }

    @JsonProperty("dl_rx_count")
    public Integer getDlRxCount() {
        return dlRxCount;
    }

    @JsonProperty("dl_rx_count")
    public void setDlRxCount(Integer dlRxCount) {
        this.dlRxCount = dlRxCount;
    }

    @JsonProperty("dl_retx_count")
    public Integer getDlRetxCount() {
        return dlRetxCount;
    }

    @JsonProperty("dl_retx_count")
    public void setDlRetxCount(Integer dlRetxCount) {
        this.dlRetxCount = dlRetxCount;
    }

    @JsonProperty("dl_err_count")
    public Integer getDlErrCount() {
        return dlErrCount;
    }

    @JsonProperty("dl_err_count")
    public void setDlErrCount(Integer dlErrCount) {
        this.dlErrCount = dlErrCount;
    }

    @JsonProperty("ul_tx_count")
    public Integer getUlTxCount() {
        return ulTxCount;
    }

    @JsonProperty("ul_tx_count")
    public void setUlTxCount(Integer ulTxCount) {
        this.ulTxCount = ulTxCount;
    }

    @JsonProperty("ul_retx_count")
    public Integer getUlRetxCount() {
        return ulRetxCount;
    }

    @JsonProperty("ul_retx_count")
    public void setUlRetxCount(Integer ulRetxCount) {
        this.ulRetxCount = ulRetxCount;
    }

    @JsonProperty("dl_mcs")
    public Double getDlMcs() {
        return dlMcs;
    }

    @JsonProperty("dl_mcs")
    public void setDlMcs(Double dlMcs) {
        this.dlMcs = dlMcs;
    }

    @JsonProperty("ul_mcs")
    public Integer getUlMcs() {
        return ulMcs;
    }

    @JsonProperty("ul_mcs")
    public void setUlMcs(Integer ulMcs) {
        this.ulMcs = ulMcs;
    }

    @JsonProperty("rrc_state")
    public String getRrcState() {
        return rrcState;
    }

    @JsonProperty("rrc_state")
    public void setRrcState(String rrcState) {
        this.rrcState = rrcState;
    }

    @JsonProperty("cell_id")
    public Integer getCellId() {
        return cellId;
    }

    @JsonProperty("cell_id")
    public void setCellId(Integer cellId) {
        this.cellId = cellId;
    }

    @JsonProperty("rsrp")
    public Double getRsrp() {
        return rsrp;
    }

    @JsonProperty("rsrp")
    public void setRsrp(Double rsrp) {
        this.rsrp = rsrp;
    }

    @JsonProperty("snr")
    public Double getSnr() {
        return snr;
    }

    @JsonProperty("snr")
    public void setSnr(Double snr) {
        this.snr = snr;
    }

    @JsonProperty("emm_state")
    public String getEmmState() {
        return emmState;
    }

    @JsonProperty("emm_state")
    public void setEmmState(String emmState) {
        this.emmState = emmState;
    }

    @JsonProperty("pdn_list")
    public List<PdnList> getPdnList() {
        return pdnList;
    }

    @JsonProperty("pdn_list")
    public void setPdnList(List<PdnList> pdnList) {
        this.pdnList = pdnList;
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
