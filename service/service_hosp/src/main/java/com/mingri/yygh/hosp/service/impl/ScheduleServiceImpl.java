package com.mingri.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mingri.yygh.common.exception.HospitalException;
import com.mingri.yygh.common.result.ResultCodeEnum;
import com.mingri.yygh.hosp.repository.HospitalRepository;
import com.mingri.yygh.hosp.repository.ScheduleRepository;
import com.mingri.yygh.hosp.service.DepartmentService;
import com.mingri.yygh.hosp.service.HospitalService;
import com.mingri.yygh.hosp.service.ScheduleService;
import com.mingri.yygh.model.hosp.BookingRule;
import com.mingri.yygh.model.hosp.Department;
import com.mingri.yygh.model.hosp.Hospital;
import com.mingri.yygh.model.hosp.Schedule;
import com.mingri.yygh.vo.hosp.BookingScheduleRuleVo;
import com.mingri.yygh.vo.hosp.ScheduleOrderVo;
import com.mingri.yygh.vo.hosp.ScheduleQueryVo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 排班service实现类
 */
@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private HospitalRepository hospitalRepository;

    //保存值班
    @Override
    public void save(Map<String, Object> map) {
        String mapString = JSONObject.toJSONString(map);
        Schedule schedule = JSONObject.parseObject(mapString, Schedule.class);
        //根据医院编号和值班编号查询
        String hoscode = schedule.getHoscode();
        String hosScheduleId = schedule.getHosScheduleId();
        Schedule data= scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode,hosScheduleId);

        if (data!=null) {
            schedule.setId(data.getId());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
        } else {
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            schedule.setStatus(1);
        }
        scheduleRepository.save(schedule);
    }

    //分页查询值班
    @Override
    public Page<Schedule> findPageSchedule(int page, int limit, ScheduleQueryVo scheduleQueryVo) {
        Pageable pageable= PageRequest.of(page-1,limit);

        Schedule schedule=new Schedule();
        BeanUtils.copyProperties(scheduleQueryVo,schedule);
        schedule.setIsDeleted(0);
        schedule.setStatus(1);

        ExampleMatcher matcher=ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        Example<Schedule> example=Example.of(schedule,matcher);
        Page<Schedule> all = scheduleRepository.findAll(example, pageable);
        return all;
    }

    //删除
    @Override
    public void remove(String hoscode, String hosScheduleId) {
        Schedule schedule= scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode,hosScheduleId);
        if (schedule!=null) {
            scheduleRepository.deleteById(schedule.getId());
        }
    }

    //根据医院编号和科室编号获取排班数据
    @Override
    public Map<String, Object> findPageScheduleByHoscodeAndDepcode(Integer page, Integer limit, String hoscode, String depcode) {
        //根据医院编号和科室编号查询
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
        //根据工作日workDate日期进行分组
        Aggregation aggregation=Aggregation.newAggregation(
                Aggregation.match(criteria), //匹配条件
                Aggregation.group("workDate")//根据workDate分组
                .first("workDate").as("workDate")
                //统计号源数量
                .count().as("docCount")
                .sum("reservedNumber").as("reservedNumber")
                .sum("availableNumber").as("availableNumber"),
                //排序
                Aggregation.sort(Sort.Direction.DESC,"workDate"),
                //实现分页
                Aggregation.skip((page-1)*limit),
                Aggregation.limit(limit)
        );
        //调用方法,最终执行
        //aggregate()是聚合查询，参数为数据，输入类型，输出类型
        AggregationResults<BookingScheduleRuleVo> aggregate =
                mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);

        List<BookingScheduleRuleVo> mappedResults = aggregate.getMappedResults();

        Aggregation totalAgg=Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
        );
        AggregationResults<BookingScheduleRuleVo> totalAggResults=
                mongoTemplate.aggregate(totalAgg,Schedule.class,BookingScheduleRuleVo.class);
        //分组之后的总记录数
        int total = totalAggResults.getMappedResults().size();
        //将日期对应的星期获取
        for (BookingScheduleRuleVo bookingScheduleRuleVo:mappedResults) {
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            String dayOfWeek = getDayOfWeek(new DateTime(workDate));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }
        //设置最终数据，进行返回
        Map<String,Object> result=new HashMap<>();
        result.put("bookingScheduleRuleList",mappedResults);
        result.put("total",total);
        //获取医院名称
        String hosName=hospitalService.getHospName(hoscode);
        //其他基础数据
        Map<String,String> baseMap=new HashMap<>();
        baseMap.put("hosname",hosName);
        result.put("baseMap",baseMap);
        return result;
    }

    //根据医院编号，科室编号和工作日期，查询排班的详细信息
    @Override
    public List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate) {
        //根据医院编号，科室编号和工作日期查询
        List<Schedule> scheduleList=
                scheduleRepository.getScheduleByHoscodeAndDepcodeAndWorkDate(hoscode,depcode,new DateTime(workDate).toDate());
        //把得到list集合遍历，向设置其他值：医院名称、科室名称、日期对应星期
        scheduleList.stream().forEach(item -> {
            packageSchedule(item);
        });
        return scheduleList;
    }

    //获取可预约排班数据
    @Override
    public Map<String, Object> getBookingSchedule(Integer page, Integer limit, String hoscode, String depcode) {
        Map<String,Object> result=new HashMap<>();
        //根据医院编号获取医院信息
        Hospital hospital = hospitalRepository.getHospitalByHoscode(hoscode);
        if (hoscode==null) {
            throw new HospitalException(ResultCodeEnum.DATA_ERROR);
        }
        //获取预约规则
        BookingRule bookingRule = hospital.getBookingRule();
        //获取可预约数据(分页)
        IPage page1 = getPage(page, limit,bookingRule);
        //当前可预约日期
        List<Date> records = page1.getRecords();
        //获取可预约日期里面科室的剩余预约数
        Criteria criteria=Criteria.where("hoscode").is(hoscode)
                .and("depcode").is(depcode).and("workDate").in(records);

        Aggregation agg=Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate").first("workDate").as("workDate")
                .count().as("docCount")
                .sum("availableNumber").as("availableNumber")
                .sum("reservedNumber").as("reservedNumber")
        );
        AggregationResults<BookingScheduleRuleVo> aggregate =
                mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> mappedResults = aggregate.getMappedResults();
        //合并数据 使用map集合，key代表日期，value代表预约规则和剩余数量等
        Map<Date,BookingScheduleRuleVo> collect = new HashMap<>();
        if (!CollectionUtils.isEmpty(mappedResults)) {
            collect = mappedResults.stream().collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate,
                    BookingScheduleRuleVo -> BookingScheduleRuleVo));
        }
        //获取可预约的排班规则
        List<BookingScheduleRuleVo> list=new ArrayList<>();
        for (int i=0;i<records.size();i++) {
            Date date = records.get(i);
            //从map集合中根据key日期获取value值
            BookingScheduleRuleVo bookingScheduleRuleVo = collect.get(date);

            //如果当天没有排班医生
            if (bookingScheduleRuleVo==null) {
                bookingScheduleRuleVo=new BookingScheduleRuleVo();
                //就诊医生人数
                bookingScheduleRuleVo.setDocCount(0);
                //科室剩余预约数
                bookingScheduleRuleVo.setAvailableNumber(-1);
            }
            bookingScheduleRuleVo.setWorkDate(date);
            bookingScheduleRuleVo.setWorkDateMd(date);
            //计算当前的日期对应星期
            String dayOfWeek = getDayOfWeek(new DateTime(date));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);

            //最后一页最后一条记录为即将预约   状态 0：正常 1：即将放号 -1：当天已停止挂号
            if(i == records.size()-1 && page == page1.getPages()) {
                bookingScheduleRuleVo.setStatus(1);
            } else {
                bookingScheduleRuleVo.setStatus(0);
            }
            //当天预约如果过了停号时间， 不能预约
            if(i == 0 && page == 1) {
                DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
                if(stopTime.isBeforeNow()) {
                    //停止预约
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }
            list.add(bookingScheduleRuleVo);
        }

        //可预约日期规则数据
        result.put("bookingScheduleList", list);
        result.put("total", page1.getTotal());
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        //医院名称
        baseMap.put("hosname", hospitalService.getHospName(hoscode));
        //科室
        Department department =departmentService.getDepartment(hoscode, depcode);
        //大科室名称
        baseMap.put("bigname", department.getBigname());
        //科室名称
        baseMap.put("depname", department.getDepname());
        //月
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
        //放号时间
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        //停号时间
        baseMap.put("stopTime", bookingRule.getStopTime());
        result.put("baseMap", baseMap);
        return result;
    }

    //根据排班id获取排班数据
    @Override
    public Schedule getScheduleById(String scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        packageSchedule(schedule);
        return schedule;
    }

    //根据排班id获取预约下单数据
    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
        //排班信息
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        if(null == schedule) {
            throw new HospitalException(ResultCodeEnum.PARAM_ERROR);
        }

        //获取预约规则信息
        Hospital hospital = hospitalService.getByHoscode(schedule.getHoscode());
        if(null == hospital) {
            throw new HospitalException(ResultCodeEnum.DATA_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();
        if(null == bookingRule) {
            throw new HospitalException(ResultCodeEnum.PARAM_ERROR);
        }

        scheduleOrderVo.setHoscode(schedule.getHoscode());
        scheduleOrderVo.setHosname(hospitalService.getHospName(schedule.getHoscode()));
        scheduleOrderVo.setDepcode(schedule.getDepcode());
        scheduleOrderVo.setDepname(departmentService.getDepname(schedule.getHoscode(), schedule.getDepcode()));
        scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId());
        scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber());
        scheduleOrderVo.setTitle(schedule.getTitle());
        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());
        scheduleOrderVo.setAmount(schedule.getAmount());

        //退号截止天数（如：就诊前一天为-1，当天为0）
        int quitDay = bookingRule.getQuitDay();
        DateTime quitTime = this.getDateTime(new DateTime(schedule.getWorkDate()).plusDays(quitDay).toDate(), bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitTime.toDate());

        //预约开始时间
        DateTime startTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startTime.toDate());

        //预约截止时间
        DateTime endTime = this.getDateTime(new DateTime().plusDays(bookingRule.getCycle()).toDate(), bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endTime.toDate());

        //当天停止挂号时间
        DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
        scheduleOrderVo.setStopTime(stopTime.toDate());
        return scheduleOrderVo;
    }

    //更新排班数据，mq调用
    @Override
    public void update(Schedule schedule) {
        schedule.setUpdateTime(new Date());
        scheduleRepository.save(schedule);
    }

    //获取可预约的日志分页数据
    private IPage getPage(Integer page,Integer limit,BookingRule bookingRule) {
//        System.out.println(new Date());
        //获取当天放号时间：年 月 日 时 分
        DateTime releaseTime = getDateTime(new Date(), bookingRule.getReleaseTime());
//        DateTime releaseTime=new DateTime("2020-12-24T08:30:00.000+08:00");
        //获取周期
        Integer cycle = bookingRule.getCycle();
        //当前时间大于放号时间，只能算明天预约(放号后，挂号的是前一天的预约)
        if (releaseTime.isBeforeNow()) {
            cycle++;
        }
        //获取可预约的所有日期
        List<Date> dateList=new ArrayList<>();
        for (int i=0;i<cycle;i++) {
            DateTime curDateTime = new DateTime().plusDays(i);
            String dataString = curDateTime.toString("yyyy-MM-dd");
            dateList.add(new DateTime(dataString).toDate());
        }
        //一页放7条数据，超过需要进行分页
        List<Date> list=new ArrayList<>();
        int start=(page-1)*limit;
        int end=page*limit;
        if (dateList.size()<limit) {
            end= dateList.size();
        }

        for (int i=start;i<end;i++) {
            list.add(dateList.get(i));
        }
        IPage<Date> iPage=new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page,limit,dateList.size());
        iPage.setRecords(list);
        return iPage;
    }

    //将日期”yyyy-MM-dd“转换为dataTime
    private DateTime getDateTime(Date date,String timeString) {
        String string = new DateTime(date).toString("yyyy-MM-dd")+" "+timeString;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(string);
        return dateTime;
    }

    //设置其他值：医院名称、科室名称、日期对应星期
    private void packageSchedule(Schedule schedule) {
        schedule.getParam().put("hosname",hospitalService.getHospName(schedule.getHoscode()));
        schedule.getParam().put("depname",departmentService.getDepname(schedule.getHoscode(),schedule.getDepcode()));
        schedule.getParam().put("dayOfWork",getDayOfWeek(new DateTime(schedule.getWorkDate())));
    }

    /**
     * 根据日期获取周几数据
     * @param dateTime
     * @return
     */
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }

}
