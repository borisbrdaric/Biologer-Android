package org.biologer.biologer.model;

import org.biologer.biologer.model.network.*;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.OrderBy;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;
import org.greenrobot.greendao.DaoException;

/**
 * Created by brjovanovic on 2/22/2018.
 */

@Entity
public class Taxon {

    @Id
    private long id;
    private String name;
    @Generated(hash = 1863565783)
    public Taxon(long id, String name) {
        this.id = id;
        this.name = name;
    }
    @Generated(hash = 1232389924)
    public Taxon() {
    }
    public long getId() {
        return this.id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }


}
