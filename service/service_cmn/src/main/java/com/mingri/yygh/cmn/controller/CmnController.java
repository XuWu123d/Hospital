package com.mingri.yygh.cmn.controller;

import com.mingri.yygh.cmn.service.CmnService;
import com.mingri.yygh.common.result.Result;
import com.mingri.yygh.model.cmn.Dict;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api("数据字典接口")
@RequestMapping("admin/cmn/dict")
@RestController
//@CrossOrigin  //允许跨域
public class CmnController {

    @Autowired
    private CmnService cmnService;

    //根据数据id查询子数据列表
    @ApiOperation("根据数据id查询子数据列表")
    @GetMapping("findChildData/{id}")
    public Result findChildData(@PathVariable Long id) {
        List<Dict> list= cmnService.findChildData(id);
        return Result.ok(list);
    }

    //导出数据字典接口
    @ApiOperation("导出数据字典接口")
    @GetMapping("/exportDict")
    public void exportDict(HttpServletResponse response) {
        cmnService.exportDictData(response);
    }

    //导入数据字典接口
    @ApiOperation("导入数据字典接口")
    @PostMapping("/importDict")
    public Result importDict(MultipartFile file) {
        cmnService.importDictData(file);
        return Result.ok();
    }


    //根据value查询
    @ApiOperation("根据value查询")
    @PostMapping("getName/{value}")
    public String selectByValue(@PathVariable String value) {
        return selectByDictcodeAndValue(" ",value);
    }


    //根据dictCode和value查询
    @PostMapping("getName/{dictCode}/{value}")
    public String selectByDictcodeAndValue(@PathVariable String dictCode,
                                           @PathVariable String value) {
        String cmn= cmnService.getCmnName(dictCode,value);
        return cmn;
    }

    //根据dictCode获取下级节点
    @ApiOperation("根据dictCode获取下级节点")
    @GetMapping("findByDictCode/{dictCode}")
    public Result findByDictCode(@PathVariable String dictCode) {
        List<Dict> dictList= cmnService.findByDictCode(dictCode);
        return Result.ok(dictList);
    }
}
