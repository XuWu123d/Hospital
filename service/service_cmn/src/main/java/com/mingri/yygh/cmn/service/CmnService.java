package com.mingri.yygh.cmn.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.yygh.model.cmn.Dict;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface CmnService extends IService<Dict> {

    List<Dict> findChildData(Long id);

    void exportDictData(HttpServletResponse response);

    void importDictData(MultipartFile file);
}
