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
    "scoreDelta",
    "attacker"
})
@Generated("jsonschema2pojo")
public class Bonus {

    @JsonProperty("match")
    private String match;
    @JsonProperty("game")
    private String game;
    @JsonProperty("shots")
    private Integer shots;
    @JsonProperty("scoreDelta")
    private Integer scoreDelta;
    @JsonProperty("attacker")
    private String attacker;
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

    @JsonProperty("scoreDelta")
    public Integer getscoreDelta() {
        return scoreDelta;
    }

    @JsonProperty("scoreDelta")
    public void setscoreDelta(Integer scoreDelta) {
        this.scoreDelta = scoreDelta;
    }

    @JsonProperty("attacker")
    public String getAttacker() {
        return attacker;
    }

    @JsonProperty("attacker")
    public void setAttacker(String attacker) {
        this.attacker = attacker;
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
