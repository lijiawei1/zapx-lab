package org.zapx.demo.mvc.entity;

import org.hibernate.validator.constraints.Range;

/**
 * 数据合法性校验测试
 *
 * Created by Shin on 2017/9/26.
 */
public class VModel {

    @Range(min = 1, max = 9, message = "只能从1-9")
    private int grade;

    @Range(min = 1, max = 99, message = "只能从1-99")
    private int classroomNumber;

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public int getClassroomNumber() {
        return classroomNumber;
    }

    public void setClassroomNumber(int classroomNumber) {
        this.classroomNumber = classroomNumber;
    }
}
