
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
    "event",
    "start_time",
    "prog",
    "args"
})
public class SimEvent {

    @JsonProperty("event")
    private String event;
    @JsonProperty("start_time")
    private Integer startTime;
    @JsonProperty("prog")
    private String prog;
    @JsonProperty("args")
    private List<String> args = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("event")
    public String getEvent() {
        return event;
    }

    @JsonProperty("event")
    public void setEvent(String event) {
        this.event = event;
    }

    @JsonProperty("start_time")
    public Integer getStartTime() {
        return startTime;
    }

    @JsonProperty("start_time")
    public void setStartTime(Integer startTime) {
        this.startTime = startTime;
    }

    @JsonProperty("prog")
    public String getProg() {
        return prog;
    }

    @JsonProperty("prog")
    public void setProg(String prog) {
        this.prog = prog;
    }

    @JsonProperty("args")
    public List<String> getArgs() {
        return args;
    }

    @JsonProperty("args")
    public void setArgs(List<String> args) {
        this.args = args;
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
