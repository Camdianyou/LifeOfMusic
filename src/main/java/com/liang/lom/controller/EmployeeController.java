package com.liang.lom.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liang.lom.common.R;
import com.liang.lom.constant.Constant;
import com.liang.lom.entity.Employee;
import com.liang.lom.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     *
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    // HttpServletRequest将我们的登录数据存储在session中
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        // 将密码进行md5加密进行处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 根据用户名查数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        // 在数据库的表中我们可以看到这个字段加上了一个唯一约束，表示这个字段是唯一的，所以我们使用getOne方法
        Employee emp = employeeService.getOne(queryWrapper); // 这个只会得到为一个的一条数据
        // 未查询到用户
        if (emp == null) {
            return R.error("登陆失败,用户不存在");
        }

        // 进行密码的对比,不一致的时候返回登陆失败的消息
        if (!emp.getPassword().equals(password)) {
            return R.error("登陆失败,密码错误");
        }

        // 查看员工状态，如果已经被禁用则登陆失败
        if (emp.getStatus() == 0) {
            return R.error("登陆失败,该用户已经被禁用");
        }
        // 登录成功将用户的id存放在session中
        request.getSession().setAttribute(Constant.EMPLOYEE, emp.getId());
        // 登陆成功,将用户的信息返回前端
        return R.success(emp);
    }

    /**
     * 员工退出登录
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        // 清理session中保存的当前登录的员工id
        request.getSession().removeAttribute(Constant.EMPLOYEE);
        return R.success("退出成功");
    }

    /**
     * 员工的新增
     *
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> addEmployee(HttpServletRequest request, @RequestBody Employee employee) {
        // id自动使用雪花算法生成
        // 设置初始密码为123456,但是需要为md5加密的密码
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        // 通过自动填充的方式填写下面的字段
        // 设置用户创建时间
        //employee.setCreateTime(LocalDateTime.now());
        // 设置更新时间
        //employee.setUpdateTime(LocalDateTime.now());
        // 这是用户创建人 和 更新人
        //employee.setCreateUser((Long) request.getSession().getAttribute(Constant.EMPLOYEE));
        //employee.setUpdateUser((Long) request.getSession().getAttribute(Constant.EMPLOYEE));

        employeeService.save(employee);
        return R.success("添加成功!");
    }

    /**
     * 员工信息的分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> queryUserByPage(int page, int pageSize, String name) {
        log.info("page={},pageSize={},name={}", page, pageSize, name);

        // 分页构造器
        Page pageInfo = new Page(page, pageSize);

        // 条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        // 当名字不为null的时候我们添加这个条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        // 添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        // 执行查询
        employeeService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 修改员工信息
     *
     * @param request
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> updateStatus(HttpServletRequest request, @RequestBody Employee employee) {
        // 使用公共字段填充进行字段的填写
        // employee.setUpdateUser((Long) request.getSession().getAttribute(Constant.EMPLOYEE));
        // employee.setUpdateTime(LocalDateTime.now());
        employeeService.updateById(employee);
        return R.success("状态更新成功");
    }

    /**
     * 根据id查询员工信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> queryEmpById(@PathVariable Long id) {
        Employee employee = employeeService.getById(id);
        if (employee != null) {
            return R.success(employee);
        }
        return R.error("没有查询到对应的员工信息");
    }
}
