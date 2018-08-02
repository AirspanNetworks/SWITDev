package UeSimulator.Amarisoft.JsonObjects.Actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import UeSimulator.Amarisoft.JsonObjects.ConfigFile.UeList;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "message", "ue_id" })
public class UEAction {

	public enum Actions {
		POWER_ON("power_on"),
		POWER_OFF("power_off"),
		UE_GET("ue_get"),
		UE_ADD("ue_add"),
		UE_DELETE("ue_del"),
		CONFIG_GET("config_get")
	    ;

	    private final String text;

	    /**
	     * @param text
	     */
	    private Actions(final String text) {
	        this.text = text;
	    }

	    /* (non-Javadoc)
	     * @see java.lang.Enum#toString()
	     */
	    @Override
	    public String toString() {
	        return text;
	    }
	}
	
	
	@JsonProperty("message")
	private String message;
	@JsonProperty("ue_id")
	private Integer ueId;
	@JsonProperty("list")
	private ArrayList<UeList> ueLists;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("message")
	public String getMessage() {
		return message;
	}

	@JsonProperty("message")
	public void setMessage(Actions message) {
		this.message = message.toString();
	}

	@JsonProperty("ue_id")
	public Integer getUeId() {
		return ueId;
	}

	@JsonProperty("ue_id")
	public void setUeId(Integer ueId) {
		this.ueId = ueId;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

	@JsonProperty("list")
	public ArrayList<UeList> getUeList() {
		return this.ueLists;
	}
	
	@JsonProperty("list")
	public void setUeList(ArrayList<UeList> ueLists) {
		this.ueLists = ueLists;
	}

}