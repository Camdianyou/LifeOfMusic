package com.liang.lom.controller;

import com.liang.lom.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * 进行文件上传下载的处理
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${lifeofmusic.path}")
    private String basePath;

    /**
     * 文件上传
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        // file是一个临时文件，需要转存到指定位置，否则本次请求结束后临时文件就会自动删除
        log.info("file {}", file.toString());
        // 原始文件名
        String originalFilename[] = file.getOriginalFilename().split("\\.");
        // 使用uuid重新生成文件名，防止文件名重复造成的文件覆盖
        String filename = UUID.randomUUID() + "." + originalFilename[1];
        // 创建一个目录对象
        File dir = new File(basePath);
        if (!dir.exists()) {
            // 当目录不存在的时候我们需要创建一个新的目录用来存储图片
            dir.mkdirs();
        }
        try {
            // 将临时文件转存
            file.transferTo(new File(basePath + filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return R.success(filename);
    }

    /**
     * 文件下载
     *
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        try {
            // 输入流，通过文件输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(basePath + name);
            // 输出流，通过输出流将文件写回浏览器，在浏览器中展示图片
            ServletOutputStream outputStream = response.getOutputStream();
            response.setContentType("image/jpeg");
            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }
            // 关闭资源
            outputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
