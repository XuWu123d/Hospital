package com.mingri.yygh.oss.service;

import org.springframework.web.multipart.MultipartFile;

public interface OssFileService {
    //上传文件
    String upload(MultipartFile file);
}
