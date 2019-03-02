package org.biologer.biologer.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * Created by Miloš Popović on 2.3.2019.
 */
@Entity
public class TaxonLocalization {

    @Id (autoincrement = true)
    private Long id;
    private String name;
    private long taxonId;
    private long translationID;
    private String locale;
    private String native_name;
    @Generated(hash = 643398675)
    public TaxonLocalization(Long id, String name, long taxonId, long translationID, String locale, String native_name) {
        this.id = id;
        this.name = name;
        this.taxonId = taxonId;
        this.translationID = translationID;
        this.locale = locale;
        this.native_name = native_name;
    }
    @Generated(hash = 1201112011)
    public TaxonLocalization() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public long getTaxonId() {
        return this.taxonId;
    }
    public void setTaxonId(long taxonId) {
        this.taxonId = taxonId;
    }
    public long getTranslationId() {
        return this.translationID;
    }
    public void setTranslaitonId(long translationID) {
        this.translationID = translationID;
    }
    public String getLocale() { return this.locale; }
    public void setLocale(String locale) {
        this.locale = locale;
    }
    public String getNativeName() {
        return this.native_name;
    }
    public void setNativeName(String native_name) {
        this.native_name = native_name;
    }
    public long getTranslationID() {
        return this.translationID;
    }
    public void setTranslationID(long translationID) {
        this.translationID = translationID;
    }
    public String getNative_name() {
        return this.native_name;
    }
    public void setNative_name(String native_name) {
        this.native_name = native_name;
    }
}

