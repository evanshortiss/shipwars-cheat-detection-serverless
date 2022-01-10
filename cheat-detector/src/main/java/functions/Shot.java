package functions;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "match",
    "game",
    "shots",
    "human",
    "by"
})
@Generated("jsonschema2pojo")
public class Shot {

    @JsonProperty("match")
    private String match;
    @JsonProperty("game")
    private String game;
    @JsonProperty("shots")
    private Integer shots;
    @JsonProperty("human")
    private Boolean human;
    @JsonProperty("by")
    private By by;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("match")
    public String getMatch() {
        return match;
    }

    @JsonProperty("match")
    public void setMatch(String match) {
        this.match = match;
    }

    @JsonProperty("game")
    public String getGame() {
        return game;
    }

    @JsonProperty("game")
    public void setGame(String game) {
        this.game = game;
    }

    @JsonProperty("shots")
    public Integer getShots() {
        return shots;
    }

    @JsonProperty("shots")
    public void setShots(Integer shots) {
        this.shots = shots;
    }

    @JsonProperty("human")
    public Boolean getHuman() {
        return human;
    }

    @JsonProperty("human")
    public void setHuman(Boolean human) {
        this.human = human;
    }

    @JsonProperty("by")
    public By getBy() {
        return by;
    }

    @JsonProperty("by")
    public void setBy(By by) {
        this.by = by;
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
