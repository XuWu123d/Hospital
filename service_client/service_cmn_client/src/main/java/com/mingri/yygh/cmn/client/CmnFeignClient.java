package com.mingri.yygh.cmn.client;

import com.mingri.yygh.common.result.Result;
import com.mingri.yygh.model.cmn.Dict;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@FeignClient("service-cmn")
@Component
public interface CmnFeignClient {
//    //根据数据id查询子数据列表
//    @ApiOperation("根据数据id查询子数据列表")
//    @GetMapping("findChildData/{id}")
//    public Result findChildData(@PathVariable Long id);


    //根据value查询
    @PostMapping("admin/cmn/dict/getName/{value}")
    public String selectByValue(@PathVariable("value") String value);


    //根据dictCode和value查询
    @PostMapping("admin/cmn/dict/getName/{dictCode}/{value}")
    public String selectByDictcodeAndValue(@PathVariable("dictCode") String dictCode,
                                           @PathVariable("value") String value);

}
