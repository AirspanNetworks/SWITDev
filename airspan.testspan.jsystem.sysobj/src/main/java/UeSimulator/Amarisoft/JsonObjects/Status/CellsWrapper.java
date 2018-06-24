
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
    "0",
    "1",
    "2",
    "3"
})
public class CellsWrapper {

    @JsonProperty("0")
    private CellStatus cell0;
    @JsonProperty("1")
    private CellStatus cell1;
    @JsonProperty("2")
    private CellStatus cell2;
    @JsonProperty("3")
    private CellStatus cell3;
    
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("0")
    public CellStatus getCell0() {
        return cell0;
    }

    @JsonProperty("0")
    public void setCell0(CellStatus cell) {
        this.cell0 = cell;
    }
    
    @JsonProperty("1")
    public CellStatus getCell1() {
        return cell1;
    }

    @JsonProperty("1")
    public void setCell1(CellStatus cell) {
        this.cell1 = cell;
    }
    
    @JsonProperty("2")
    public CellStatus getCell2() {
        return cell2;
    }

    @JsonProperty("2")
    public void setCell2(CellStatus cell) {
        this.cell2 = cell;
    }
    
    @JsonProperty("3")
    public CellStatus getCell3() {
        return cell3;
    }

    @JsonProperty("3")
    public void setCell3(CellStatus cell) {
        this.cell3 = cell;
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
