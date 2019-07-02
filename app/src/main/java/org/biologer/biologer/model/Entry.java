package org.biologer.biologer.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by brjovanovic on 2/22/2018.
 */

@Entity
public class Entry {

    @Id(autoincrement = true)
    private Long id;
    private Long taxonId;
    private String taxonSuggestion;
    private String year;
    private String month;
    private String day;
    private String comment;
    private Integer number;
    private String sex;
    private Long stage;
    private String deadOrAlive;
    private String causeOfDeath;
    private double lattitude;
    private double longitude;
    private Double accuracy;
    private double elevation;
    private String location;
    private String slika1;
    private String slika2;
    private String slika3;
    private String projectId;
    private String foundOn;
    private String data_licence;
    private int image_licence;
    private String time;
    private String habitat;

    @Generated(hash = 1632895889)
    public Entry(Long id, Long taxonId, String taxonSuggestion, String year, String month,
            String day, String comment, Integer number, String sex, Long stage,
            String deadOrAlive, String causeOfDeath, double lattitude, double longitude,
            Double accuracy, double elevation, String location, String slika1, String slika2,
            String slika3, String projectId, String foundOn, String data_licence,
            int image_licence, String time, String habitat) {
        this.id = id;
        this.taxonId = taxonId;
        this.taxonSuggestion = taxonSuggestion;
        this.year = year;
        this.month = month;
        this.day = day;
        this.comment = comment;
        this.number = number;
        this.sex = sex;
        this.stage = stage;
        this.deadOrAlive = deadOrAlive;
        this.causeOfDeath = causeOfDeath;
        this.lattitude = lattitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.elevation = elevation;
        this.location = location;
        this.slika1 = slika1;
        this.slika2 = slika2;
        this.slika3 = slika3;
        this.projectId = projectId;
        this.foundOn = foundOn;
        this.data_licence = data_licence;
        this.image_licence = image_licence;
        this.time = time;
        this.habitat = habitat;
    }
    @Generated(hash = 1759844922)
    public Entry() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getYear() {
        return this.year;
    }
    public void setYear(String year) {
        this.year = year;
    }
    public String getMonth() {
        return this.month;
    }
    public void setMonth(String month) {
        this.month = month;
    }
    public String getDay() {
        return this.day;
    }
    public void setDay(String day) {
        this.day = day;
    }
    public String getComment() {
        return this.comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public Integer getNumber() {
        return this.number;
    }
    public void setNumber(Integer number) {
        this.number = number;
    }
    public String getSex() {
        return this.sex;
    }
    public void setSex(String sex) {
        this.sex = sex;
    }
    public Long getStage() {
        return this.stage;
    }
    public void setStage(Long stage) {
        this.stage = stage;
    }
    public String getDeadOrAlive() {
        return this.deadOrAlive;
    }
    public void setDeadOrAlive(String deadOrAlive) {
        this.deadOrAlive = deadOrAlive;
    }
    public String getCauseOfDeath() {
        return this.causeOfDeath;
    }
    public void setCauseOfDeath(String causeOfDeath) {
        this.causeOfDeath = causeOfDeath;
    }
    public double getLattitude() {
        return this.lattitude;
    }
    public void setLattitude(double lattitude) {
        this.lattitude = lattitude;
    }
    public double getLongitude() {
        return this.longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public double getAccuracy() {
        return this.accuracy;
    }
    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }
    public double getElevation() {
        return this.elevation;
    }
    public void setElevation(double elevation) {
        this.elevation = elevation;
    }
    public String getLocation() {
        return this.location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public String getSlika1() {
        return this.slika1;
    }
    public void setSlika1(String slika1) {
        this.slika1 = slika1;
    }
    public String getSlika2() {
        return this.slika2;
    }
    public void setSlika2(String slika2) {
        this.slika2 = slika2;
    }
    public String getSlika3() {
        return this.slika3;
    }
    public void setSlika3(String slika3) {
        this.slika3 = slika3;
    }
    public String getProjectId() {
        return this.projectId;
    }
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
    public String getFoundOn() {
        return this.foundOn;
    }
    public void setFoundOn(String foundOn) {
        this.foundOn = foundOn;
    }
    public String getData_licence() {
        return this.data_licence;
    }
    public void setData_licence(String data_licence) {
        this.data_licence = data_licence;
    }
    public int getImage_licence() {
        return this.image_licence;
    }
    public void setImage_licence(int image_licence) {
        this.image_licence = image_licence;
    }
    public String getTime() {
        return this.time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public Long getTaxonId() {
        return this.taxonId;
    }
    public void setTaxonId(Long taxonId) {
        this.taxonId = taxonId;
    }
    public String getTaxonSuggestion() {
        return this.taxonSuggestion;
    }
    public void setTaxonSuggestion(String taxonSuggestion) {
        this.taxonSuggestion = taxonSuggestion;
    }
    public String getHabitat() {
        return this.habitat;
    }
    public void setHabitat(String habitat) {
        this.habitat = habitat;
    }
}