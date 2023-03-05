package com.liang.lom.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liang.lom.common.R;
import com.liang.lom.constant.Constant;
import com.liang.lom.entity.User;
import com.liang.lom.service.UserService;
import com.liang.lom.utils.MailSendUtils;
import com.liang.lom.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    /**
     * 发送验证码
     *
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    private R<String> userLogin(@RequestBody User user, HttpSession session) {
        // 获取手机号码
        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)) {
            // 随机生成验证码
            String code = ValidateCodeUtils.generateValidateCode(6).toString();
            // 发送短信验证码因为要钱所以暂时不用
            // 调用aliyun提供的短信服务发送验证码
            // SMSUtils.sendMessage("瑞吉外卖", "SMS_271360137", phone, code);

            // 通过邮件发送验证码
            // MailSendUtils.sendMail("lp2230037280@163.com", code);
            // 将验证码存储到Session中

            // 将验证码缓存到redis中，并且设置有效期为5分钟
            redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);
            // session.setAttribute(phone, code);

            return R.success("手机验证码短信发送成功" + code);
        }

        return R.error("短信发送失败");
    }

    @PostMapping("/login")
    public R<User> userLogin(@RequestBody Map userMap, HttpSession session) {
        log.info("userMap : {}", userMap);

        // 从userMap中获取手机号
        String phone = userMap.get("phone").toString();
        // 从userMap中获取验证码
        String code = userMap.get("code").toString();
        // 从Session中通过手机号码获取验证码
        // String codeInSession = session.getAttribute(phone).toString();

        // 从redis中把我们存储的验证码取出
        String codeInSession = redisTemplate.opsForValue().get(phone);

        // 进行验证码的比对
        if (codeInSession != null && codeInSession.equals(code)) {
            // 如果比对成功，说明登陆成功
            // 看手机号码是不是已经在我们的数据库表中,如果不在的话我们新增一条数据
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);
            // 新用户
            if (user == null) {
                User saveUser = new User();
                saveUser.setPhone(phone);
                saveUser.setStatus(1);
                userService.save(saveUser);
            }
            // 为true则继续执行
            assert user != null;
            session.setAttribute(Constant.USER, user.getId());

            // 如果用户登录成功我们就删除缓存中的验证码
            redisTemplate.delete(phone);

            return R.success(user);
        }
        return R.error("登录失败");
    }

    @PostMapping("/loginout")
    public R<String> loginout(HttpSession session) {
        session.removeAttribute(Constant.USER);
        return R.success("退出成功");
    }
}
