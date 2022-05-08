package com.mingri.yygh.hosp.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mingri.yygh.common.result.Result;
import com.mingri.yygh.common.utils.MD5;
import com.mingri.yygh.hosp.service.HospitalSetService;
import com.mingri.yygh.model.hosp.HospitalSet;
import com.mingri.yygh.vo.hosp.HospitalSetQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

@Api(tags = "医院设置管理")
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
@CrossOrigin  //允许跨域访问
public class HospitalSetController {

    @Autowired
    private HospitalSetService hospitalSetService;

    @ApiOperation(value = "获取所有医院设置")
    @GetMapping("findAll")
    public Result select() {
        List<HospitalSet> list= hospitalSetService.list();
        return Result.ok(list);
    }

    @ApiOperation(value = "逻辑删除医院设置")
    @DeleteMapping("{id}")
    public Result delete(@PathVariable Long id) {
        boolean flag = hospitalSetService.removeById(id);
        if (flag) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    //3 条件查询带分页
    @CrossOrigin
    @ApiOperation(value = "条件查询带分页")
    @PostMapping("findPageHospSet/{current}/{limit}")
    public Result findPageHospital(@PathVariable long current, @PathVariable long limit,
                                   @RequestBody(required = false) HospitalSetQueryVo hospitalSetQueryVo) {
        //创建page对象，传递当前页，每页记录数
        Page<HospitalSet> page=new Page<>(current,limit);
        //构建条件
        QueryWrapper<HospitalSet> wrapper=new QueryWrapper<>();
        if (hospitalSetQueryVo.getHosname()!=null) {  //医院名称
            wrapper.like("hosname",hospitalSetQueryVo.getHosname());
        }

        if (hospitalSetQueryVo.getHoscode()!=null) { //医院编号
            wrapper.eq("hoscode",hospitalSetQueryVo.getHoscode());
        }
        //调用方法实现分页查询
        Page<HospitalSet> page1 = hospitalSetService.page(page, wrapper);
        return Result.ok(page1);
    }

    //添加医院设置
    @ApiOperation(value = "添加医院设置")
    @PostMapping("saveHospitalSet")
    public Result save(@RequestBody HospitalSet hospitalSet) {
        //设置状态 1 使用 0 不能使用
        hospitalSet.setStatus(1);
        //设置签名秘钥
        Random random=new Random();
        hospitalSet.setSignKey(MD5.encrypt(System.currentTimeMillis()+""+random.nextInt(1000)));
        return hospitalSetService.save(hospitalSet) ? Result.ok() : Result.fail();
    }

    //根据id获取医院设置
    @ApiOperation(value = "根据id获取医院设置")
    @GetMapping("getHospitalById/{id}")
    public Result getHospitalById(@PathVariable Long id) {
        HospitalSet getHospitalById = hospitalSetService.getById(id);
        return Result.ok(getHospitalById);
    }

    //修改医院设置
    @ApiOperation(value = "修改医院设置")
    @PostMapping("updateHospital")
    public Result updateHospital(@RequestBody HospitalSet hospitalSet) {
        return hospitalSetService.updateById(hospitalSet) ? Result.ok() : Result.fail();
    }

    //批量删除设置
    @ApiOperation(value = "批量删除设置")
    @DeleteMapping("deleteHospital")   //@RequestBod修饰不能为空，所以不用判断是否为空
    public Result deleteHospital(@RequestBody List<Long> ids) {
        //批量删除中该集合中只要有一个存在就成功，如果都不存在就失败
        return hospitalSetService.removeByIds(ids) ? Result.ok() : Result.fail();
    }

    //医院设置锁定和解锁
    @PutMapping("lockHospitalSet/{id}/{status}")
    public Result lockHospitalSet(@PathVariable Long id,@PathVariable Integer status) {
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        hospitalSet.setStatus(status);
        return hospitalSetService.updateById(hospitalSet) ? Result.ok() : Result.fail();
    }

    //发送签名秘钥
    @PutMapping("key/{id}")
    public Result lockHospitalSet(@PathVariable Long id) {
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        String signKey = hospitalSet.getSignKey();
        Integer status = hospitalSet.getStatus();
        //发送信息

        return Result.ok();
    }
}
