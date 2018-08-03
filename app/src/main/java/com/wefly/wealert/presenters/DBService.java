package com.wefly.wealert.presenters;

import android.support.annotation.NonNull;

/**
 * Created by admin on 04/04/2018.
 */

public class DBService extends BaseService {

    protected @NonNull
    DataBasePresenter getDBManager(){
        try {
            if (DataBasePresenter.getInstance() != null)
                return DataBasePresenter.getInstance();
        } catch (Exception e){
        }
        DataBasePresenter.init(this);
        return DataBasePresenter.getInstance();

    }

}
