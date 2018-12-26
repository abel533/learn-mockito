package com.github.abel533.dubbo.api;

public interface EmployeeService {

    /**
     * 根据 id 获取员工姓名
     *
     * @param id
     * @return
     */
    String getEmployeeName(Long id);

}
