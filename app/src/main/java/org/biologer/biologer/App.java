package org.biologer.biologer;

import android.app.Application;

import org.biologer.biologer.model.DaoMaster;
import org.biologer.biologer.model.DaoSession;
import org.greenrobot.greendao.database.Database;

/**
 * Created by brjovanovic on 12/24/2017.
 */

public class App extends Application {

    private static App app;
    private DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();

        app = this;

        //za GreenDAO bazu, obavezno
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "notes-db");
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();

    }

    public static App get(){
        return app;
    }

    public DaoSession getDaoSession() {return daoSession;}
}
