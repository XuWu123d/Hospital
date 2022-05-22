package com.mingri.yygh.hosp.controller.api;

import com.mingri.yygh.common.exception.HospitalException;
import com.mingri.yygh.common.helper.HttpRequestHelper;
import com.mingri.yygh.common.result.Result;
import com.mingri.yygh.common.result.ResultCodeEnum;
import com.mingri.yygh.common.utils.MD5;
import com.mingri.yygh.hosp.service.DepartmentService;
import com.mingri.yygh.hosp.service.HospitalService;
import com.mingri.yygh.hosp.service.HospitalSetService;
import com.mingri.yygh.hosp.service.ScheduleService;
import com.mingri.yygh.model.hosp.Department;
import com.mingri.yygh.model.hosp.Hospital;
import com.mingri.yygh.model.hosp.Schedule;
import com.mingri.yygh.vo.hosp.DepartmentQueryVo;
import com.mingri.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/hosp")
public class ApiController {


    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private HospitalSetService hospitalSetService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ScheduleService scheduleService;

    //查询医院
    @PostMapping("hospital/show")
    public Result getHospital(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);
        //获取传过来的签名
        String hospSign =(String) map.get("sign");
        //获取医院编号
        String hoscode =(String) map.get("hoscode");
        String signKey = hospitalSetService.getSignKey(hoscode);
        String encrypt = MD5.encrypt(signKey);
        if (!hospSign.equals(encrypt)) {
            throw new HospitalException(ResultCodeEnum.SIGN_ERROR);
        }
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        return Result.ok(hospital);
    }

    //上传医院接口
    @PostMapping("saveHospital")
    public Result saveHosp(HttpServletRequest request) {
        //获取医院信息
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);
        //获取医院系统传递过来的签名(加密了的)
        String hospSign=(String) map.get("sign");
        //将它与数据库查询出来的签名进行比较
        String hoscode =(String) map.get("hoscode");
        String signKey = hospitalSetService.getSignKey(hoscode);
        String encrypt = MD5.encrypt(signKey);
        //判断签名是否一致
        if (!hospSign.equalsIgnoreCase(encrypt)) {
            throw new HospitalException(ResultCodeEnum.SIGN_ERROR);
        }

        //传输过程中“+”转换为了“ ”，因此我们要转换回来
        String logoData=(String) map.get("logoData");
        logoData= logoData.replaceAll(" ","+");
        map.put("logoData",logoData);

        hospitalService.save(map);
        return Result.ok();
    }


    //科室分页查询
    @PostMapping("department/list")
    public Result getDepartment(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);
        //获取展示页码信息
        int page = map.get("page")==null?1:Integer.parseInt((String) map.get("page"));
        int limit = map.get("limit")==null?1:Integer.parseInt((String) map.get("limit"));
        //获取签名
        String hoscode=(String) map.get("hoscode");
//        //传递过来的签名
//        String sign =(String) map.get("sign");
//        //获取数据库中的签名
//        String hoscode =(String) map.get("hoscode");
//        String signKey = hospitalSetService.getSignKey(hoscode);
//        String encrypt = MD5.encrypt(signKey);
//
//        if (!sign.equals(encrypt)) {
//            throw new HospitalException(ResultCodeEnum.SIGN_ERROR);
//        }
//        Hospital byHoscode = hospitalService.getByHoscode(hoscode);

        DepartmentQueryVo departmentQueryVo=new DepartmentQueryVo();
        departmentQueryVo.setHoscode(hoscode);
        Page<Department> departments= departmentService.findPageDepartment(page,limit,departmentQueryVo);

        return Result.ok(departments);
    }

    //上传科室
    @PostMapping("saveDepartment")
    public Result saveDepartment(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);
        //获取传递过来的签名
        String sign=(String) map.get("sign");
        //查询数据库
        String hoscode=(String) map.get("hoscode");
        String signKey = hospitalSetService.getSignKey(hoscode);
        String encrypt = MD5.encrypt(signKey);
        if (!encrypt.equals(sign)) {
            throw new HospitalException(ResultCodeEnum.SIGN_ERROR);
        }
        departmentService.save(map);
        return Result.ok();
    }

    //删除科室
    @PostMapping("department/remove")
    public Result removeDepartment(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);
        String hoscode=(String) map.get("hoscode");
        String depcode=(String) map.get("depcode");
        departmentService.remove(hoscode,depcode);
        return Result.ok();
    }

    //分页展示排班列表
    @PostMapping("schedule/list")
    public Result getSchedule(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);
        int page = map.get("page")==null?1:Integer.parseInt((String) map.get("page"));
        int limit = map.get("limit")==null?1:Integer.parseInt((String) map.get("limit"));
        //医院编号
        String hoscode=(String) map.get("hoscode");
        //科室编号
//        map.get("")
        ScheduleQueryVo scheduleQueryVo=new ScheduleQueryVo();
        scheduleQueryVo.setHoscode(hoscode);
        Page<Schedule> schedules= scheduleService.findPageSchedule(page,limit,scheduleQueryVo);
        return Result.ok(schedules);
    }

    //上传排班
    @PostMapping("saveSchedule")
    public Result saveSchedule(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);
        //签名校验
//        map.get("");
        scheduleService.save(map);
        return Result.ok();
    }

    //删除排班
    @PostMapping("schedule/remove")
    public Result removeSchedule(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);
        String hoscode=(String) map.get("hoscode");
        String hosScheduleId=(String) map.get("hosScheduleId");
        scheduleService.remove(hoscode,hosScheduleId);
        return Result.ok();
    }


//    //将公共的方法抽取出来
//    private void comment(HttpServletRequest request) {
//        Map<String, String[]> parameterMap = request.getParameterMap();
//        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);
//        //获取传过来的签名
//        String hospSign =(String) map.get("sign");
//        //获取医院编号
//        String hoscode =(String) map.get("hoscode");
//        String signKey = hospitalSetService.getSignKey(hoscode);
//        String encrypt = MD5.encrypt(signKey);
//        if (!hospSign.equals(encrypt)) {
//            throw new HospitalException(ResultCodeEnum.SIGN_ERROR);
//        }
//    }
}
