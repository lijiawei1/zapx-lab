package org.zap.framework.orm.sql.criteria;

import org.zap.framework.orm.sql.Criteria;

public class AND extends MultiLogicGroup {

  public AND(Criteria... criterias) {
      super("AND", criterias);
  }

}