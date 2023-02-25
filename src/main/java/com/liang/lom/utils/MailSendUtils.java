package com.liang.lom.utils;

import org.bouncycastle.crypto.signers.ISOTrailers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Arrays;

@Component
public class MailSendUtils {

    private static JavaMailSender javaMailSender;

    @Autowired
    public void setMailSender(JavaMailSender javaMailSender) {
        MailSendUtils.javaMailSender = javaMailSender;
    }

    private static TemplateEngine templateEngine;

    @Autowired
    public void setTemplateEngine(TemplateEngine templateEngine) {
        MailSendUtils.templateEngine = templateEngine;
    }

    private static final String from = "2230037280@qq.com";

    /**
     * 邮箱发送验证码
     */
    public static void sendMail(String mailNumber, String code) {
        //创建邮件正文
        Context context = new Context();
        context.setVariable("verifyCode", Arrays.asList(code.split("")));

        //将模块引擎内容解析成html字符串
        String emailContent = templateEngine.process("mailTemplate", context);
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from + "(鹿鸣餐厅)");
            helper.setTo(mailNumber);
            helper.setSubject("鹿鸣餐厅");
            helper.setText(emailContent, true);
            // 发送一个复杂邮件
            javaMailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
