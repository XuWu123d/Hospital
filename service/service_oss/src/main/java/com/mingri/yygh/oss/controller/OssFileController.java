package com.mingri.yygh.oss.controller;

import com.mingri.yygh.common.result.Result;
import com.mingri.yygh.oss.service.OssFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 上传文件到阿里云
 */
@RestController
@RequestMapping("/api/oss")
public class OssFileController {

    @Autowired
    private OssFileService ossFileService;

    @PostMapping("fileUpload")
    public Result upload(MultipartFile file) {
        String url = ossFileService.upload(file);
        return Result.ok(url);
    }
}
