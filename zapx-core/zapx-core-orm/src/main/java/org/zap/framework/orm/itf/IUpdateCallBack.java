package org.zap.framework.orm.itf;

import org.zap.framework.orm.base.FieldUpdated;

import java.util.List;

/**
 * Created by Shin on 2017/9/4.
 */
public interface IUpdateCallBack {


    void doWork(List<FieldUpdated> fieldUpdateds);
}
