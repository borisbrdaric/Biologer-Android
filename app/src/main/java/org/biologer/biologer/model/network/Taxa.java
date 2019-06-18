
package org.biologer.biologer.model.network;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.biologer.biologer.model.Taxon;

public class Taxa {

    @JsonProperty("id")
    private long id;
    @JsonProperty("parent_id")
    private String parentId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("rank")
    private String rank;
    @JsonProperty("rank_level")
    private long rankLevel;
    @JsonProperty("author")
    private Object author;
    @JsonProperty("ancestry")
    private String ancestry;
    @JsonProperty("fe_old_id")
    private Object feOldId;
    @JsonProperty("fe_id")
    private Object feId;
    @JsonProperty("restricted")
    private boolean restricted;
    @JsonProperty("allochthonous")
    private boolean allochthonous;
    @JsonProperty("invasive")
    private boolean invasive;
    @JsonProperty("rank_translation")
    private String rankTranslation;
    @JsonProperty("native_name")
    private Object nativeName;
    @JsonProperty("description")
    private Object description;
    @JsonProperty("translations")
    private List<Translation> translations = null;
    @JsonProperty("parent")
    private Parent parent;
    @JsonProperty("stages")
    private List<Stage6> stages = null;
    @JsonProperty("activity")
    private List<Object> activity = null;

    public Taxon toTaxon(){
        Taxon taxon = new Taxon(id, name);
        return taxon;
    }

    @JsonProperty("id")
    public long getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(long id) {
        this.id = id;
    }

    @JsonProperty("parent_id")
    public String getParentId() {
        return parentId;
    }

    @JsonProperty("parent_id")
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("rank")
    public String getRank() {
        return rank;
    }

    @JsonProperty("rank")
    public void setRank(String rank) {
        this.rank = rank;
    }

    @JsonProperty("rank_level")
    public long getRankLevel() {
        return rankLevel;
    }

    @JsonProperty("rank_level")
    public void setRankLevel(long rankLevel) {
        this.rankLevel = rankLevel;
    }

    @JsonProperty("author")
    public Object getAuthor() {
        return author;
    }

    @JsonProperty("author")
    public void setAuthor(Object author) {
        this.author = author;
    }

    @JsonProperty("ancestry")
    public String getAncestry() {
        return ancestry;
    }

    @JsonProperty("ancestry")
    public void setAncestry(String ancestry) {
        this.ancestry = ancestry;
    }

    @JsonProperty("fe_old_id")
    public Object getFeOldId() {
        return feOldId;
    }

    @JsonProperty("fe_old_id")
    public void setFeOldId(Object feOldId) {
        this.feOldId = feOldId;
    }

    @JsonProperty("fe_id")
    public Object getFeId() {
        return feId;
    }

    @JsonProperty("fe_id")
    public void setFeId(Object feId) {
        this.feId = feId;
    }

    @JsonProperty("restricted")
    public boolean isRestricted() {
        return restricted;
    }

    @JsonProperty("restricted")
    public void setRestricted(boolean restricted) {
        this.restricted = restricted;
    }

    @JsonProperty("allochthonous")
    public boolean isAllochthonous() {
        return allochthonous;
    }

    @JsonProperty("allochthonous")
    public void setAllochthonous(boolean allochthonous) {
        this.allochthonous = allochthonous;
    }

    @JsonProperty("invasive")
    public boolean isInvasive() {
        return invasive;
    }

    @JsonProperty("invasive")
    public void setInvasive(boolean invasive) {
        this.invasive = invasive;
    }

    @JsonProperty("rank_translation")
    public String getRankTranslation() {
        return rankTranslation;
    }

    @JsonProperty("rank_translation")
    public void setRankTranslation(String rankTranslation) {
        this.rankTranslation = rankTranslation;
    }

    @JsonProperty("native_name")
    public Object getNativeName() {
        return nativeName;
    }

    @JsonProperty("native_name")
    public void setNativeName(Object nativeName) {
        this.nativeName = nativeName;
    }

    @JsonProperty("description")
    public Object getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(Object description) {
        this.description = description;
    }

    @JsonProperty("translations")
    public List<Translation> getTranslations() {
        return translations;
    }

    @JsonProperty("translations")
    public void setTranslations(List<Translation> translations) {
        this.translations = translations;
    }

    @JsonProperty("parent")
    public Parent getParent() {
        return parent;
    }

    @JsonProperty("parent")
    public void setParent(Parent parent) {
        this.parent = parent;
    }

    @JsonProperty("stages")
    public List<Stage6> getStages() {
        return stages;
    }

    @JsonProperty("stages")
    public void setStages(List<Stage6> stages) {
        this.stages = stages;
    }

    @JsonProperty("activity")
    public List<Object> getActivity() {
        return activity;
    }

    @JsonProperty("activity")
    public void setActivity(List<Object> activity) {
        this.activity = activity;
    }

}
