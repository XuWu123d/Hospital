package com.mingri.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.common.rabbitmq.constant.MqConst;
import com.mingri.common.rabbitmq.service.RabbitService;
import com.mingri.yygh.common.exception.HospitalException;
import com.mingri.yygh.common.helper.HttpRequestHelper;
import com.mingri.yygh.common.result.ResultCodeEnum;
import com.mingri.yygh.enums.OrderStatusEnum;
import com.mingri.yygh.model.order.OrderInfo;
import com.mingri.yygh.model.user.Patient;
import com.mingri.yygh.order.mapper.OrderMapper;
import com.mingri.yygh.order.service.OrderService;
import com.mingri.yygh.order.service.WeixinService;
import com.mingri.yygh.user.client.PatientFeignClient;
import com.mingri.yygh.vo.hosp.ScheduleOrderVo;
import com.mingri.yygh.vo.msm.MsmVo;
import com.mingri.yygh.vo.order.*;
import org.joda.time.DateTime;
import com.mingri.yygh.hospital.client.HospitalFeignClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderInfo> implements OrderService {
    @Autowired
    private PatientFeignClient patientFeignClient;

    @Autowired
    private HospitalFeignClient hospitalFeignClient;

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private WeixinService weixinService;

    @Autowired
    private OrderMapper orderMapper;

    //生成挂号订单
    @Override
    public Long saveOrder(String scheduleId, Long patientId) {
        //远程调用获取就诊人信息
        Patient patient = patientFeignClient.getPatient(patientId);
        //远程调用获取排班信息
        ScheduleOrderVo schedule = hospitalFeignClient.getSchedule(scheduleId);
        //判断当前时间是否可以预约
//        if (new DateTime(schedule.getStartTime()).isAfterNow() ||
//            new DateTime(schedule.getEndTime()).isBeforeNow()) {
//            throw new HospitalException(ResultCodeEnum.TIME_NO);
//        }
        //获取签名信息
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(schedule.getHoscode());
        //添加到订单表
        OrderInfo orderInfo=new OrderInfo();
        //由于ScheduleOrderVo中的数据与OrderInfo中的数据基本相同，可以直接复制
        BeanUtils.copyProperties(schedule,orderInfo);
        //向OrderInfo中设置其他值
        String outTradeNo = System.currentTimeMillis()+""+new Random().nextInt(100);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setScheduleId(scheduleId);
        orderInfo.setUserId(patient.getUserId());
        orderInfo.setPatientId(patientId);
        orderInfo.setPatientName(patient.getName());
        orderInfo.setPatientPhone(patient.getPhone());
        orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());
        baseMapper.insert(orderInfo);
        //调用Hospital接口,实现预约挂号操作
        //使用map集合对医院接口的信息进行封装
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode",orderInfo.getHoscode());
        paramMap.put("depcode",orderInfo.getDepcode());
        paramMap.put("hosScheduleId",orderInfo.getScheduleId());
        paramMap.put("reserveDate",new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd"));
        paramMap.put("reserveTime", orderInfo.getReserveTime());
        paramMap.put("amount",orderInfo.getAmount());
        paramMap.put("name", patient.getName());
        paramMap.put("certificatesType",patient.getCertificatesType());
        paramMap.put("certificatesNo", patient.getCertificatesNo());
        paramMap.put("sex",patient.getSex());
        paramMap.put("birthdate", patient.getBirthdate());
        paramMap.put("phone",patient.getPhone());
        paramMap.put("isMarry", patient.getIsMarry());
        paramMap.put("provinceCode",patient.getProvinceCode());
        paramMap.put("cityCode", patient.getCityCode());
        paramMap.put("districtCode",patient.getDistrictCode());
        paramMap.put("address",patient.getAddress());
        //联系人
        paramMap.put("contactsName",patient.getContactsName());
        paramMap.put("contactsCertificatesType", patient.getContactsCertificatesType());
        paramMap.put("contactsCertificatesNo",patient.getContactsCertificatesNo());
        paramMap.put("contactsPhone",patient.getContactsPhone());
        paramMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(paramMap, signInfoVo.getSignKey());
        paramMap.put("sign", sign);

        //请求医院系统接口
        JSONObject result = HttpRequestHelper.sendRequest(paramMap, signInfoVo.getApiUrl() + "/order/submitOrder");
        if (result.getInteger("code")==200) {
            JSONObject jsonObject = result.getJSONObject("data");
            //预约记录唯一标识（医院预约的记录主键）
            String hosRecordId = jsonObject.getString("hosRecordId");
            //预约序号
            Integer number = jsonObject.getInteger("number");
            //取号时间
            String fetchTime = jsonObject.getString("fetchTime");
            //取号地址
            String fetchAddress = jsonObject.getString("fetchAddress");
            //更新订单
            orderInfo.setHosRecordId(hosRecordId);
            orderInfo.setNumber(number);
            orderInfo.setFetchTime(fetchTime);
            orderInfo.setFetchAddress(fetchAddress);
            baseMapper.updateById(orderInfo);

            //排班可预约数
            Integer reservedNumber = jsonObject.getInteger("reservedNumber");
            //排班剩余预约数
            Integer availableNumber = jsonObject.getInteger("availableNumber");
            //使用mq，发送mq消息，号源更新和短信通知
            OrderMqVo orderMqVo=new OrderMqVo();
            orderMqVo.setAvailableNumber(availableNumber);
            orderMqVo.setReservedNumber(reservedNumber);
            orderMqVo.setScheduleId(scheduleId);

            //短信提示
            MsmVo msmVo=new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
            msmVo.setTemplateCode("SMS_194640721");
            String reserveDate=new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")
                    +(orderInfo.getReserveTime()==0 ? "上午" : "下午");
            Map<String,Object> param=new HashMap<>();
            param.put("title",orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle());
            param.put("amount",orderInfo.getAmount());
            param.put("reserveDate",reserveDate);
            param.put("name",orderInfo.getPatientName());
            param.put("quitTime",new DateTime(orderInfo.getQuitTime()).toString("yyyy-MM-dd HH:mm"));
            msmVo.setParam(param);
            orderMqVo.setMsmVo(msmVo);
            //发送
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER,MqConst.ROUTING_ORDER,orderMqVo);
        } else {
            throw new HospitalException(result.getString("message"),ResultCodeEnum.FAIL.getCode());
        }
        return orderInfo.getId();
    }

    //订单详情显示
    @Override
    public OrderInfo getOrder(String orderId) {
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        return orderInfo;
    }

    //订单列表(分页)
    @Override
    public IPage<OrderInfo> getPage(Page<OrderInfo> orderPage, OrderQueryVo orderQueryVo) {
        String hosname = orderQueryVo.getKeyword(); //医院名称
        Long patientId = orderQueryVo.getPatientId(); //就诊人id
        String reserveDate = orderQueryVo.getReserveDate(); //安排时间
        String orderStatus = orderQueryVo.getOrderStatus(); //订单状态
        String createTimeBegin = orderQueryVo.getCreateTimeBegin(); //开始时间
        String createTimeEnd = orderQueryVo.getCreateTimeEnd(); //结束时间

        LambdaQueryWrapper<OrderInfo> wrapper=new LambdaQueryWrapper<>();
        wrapper.like(!StringUtils.isEmpty(hosname),OrderInfo::getHosname,hosname)
                .eq(!StringUtils.isEmpty(patientId),OrderInfo::getPatientId,patientId)
                .eq(!StringUtils.isEmpty(orderStatus),OrderInfo::getOrderStatus,orderStatus)
                .ge(!StringUtils.isEmpty(reserveDate),OrderInfo::getReserveDate,reserveDate)
                .ge(!StringUtils.isEmpty(createTimeBegin),OrderInfo::getCreateTime,createTimeBegin)
                .le(!StringUtils.isEmpty(createTimeEnd),OrderInfo::getCreateTime,createTimeEnd);
        IPage<OrderInfo> page = baseMapper.selectPage(orderPage, wrapper);
        //将编号变成对应值
        for (OrderInfo orderInfo:page.getRecords()) {
            orderInfo.getParam().put("orderStatusString",OrderStatusEnum.getStatusNameByStatus(orderInfo.getOrderStatus()));
        }
//        QueryWrapper<OrderInfo> queryWrapper=new QueryWrapper<>();
//        if (!StringUtils.isEmpty(hosname)) {
//            queryWrapper.like("hosname",hosname);
//        }
//        if (!StringUtils.isEmpty(patientId)) {
//            queryWrapper.eq("patient_id",patientId);
//        }
//        if (!StringUtils.isEmpty(orderStatus)) {
//            queryWrapper.eq("order_status",orderStatus);
//        }
//        if (!StringUtils.isEmpty(reserveDate)) {
//            queryWrapper.ge("reserve_date",reserveDate);
//        }
//        if (!StringUtils.isEmpty(createTimeBegin)) {
//            queryWrapper.ge("create_time_begin",createTimeBegin);
//        }
//        if (!StringUtils.isEmpty(createTimeEnd)) {
//            queryWrapper.le("create_time_end",createTimeEnd);
//        }
//        IPage<OrderInfo> page = baseMapper.selectPage(orderPage, queryWrapper);
        return page;
    }

    //取消预约
    @Override
    public Boolean cancelOrder(Long orderId) {
        //获取订单信息
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        DateTime quitTime=new DateTime(orderInfo.getQuitTime());
        //超过15点不能退号
//        if (quitTime.isBeforeNow()) {
//            throw new HospitalException(ResultCodeEnum.CANCEL_ORDER_NO);
//        }
        //调用医院接口
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(orderInfo.getHoscode());
        if (signInfoVo==null) {
            throw new HospitalException(ResultCodeEnum.PARAM_ERROR);
        }
        Map<String,Object> map=new HashMap<>();
        map.put("hoscode",orderInfo.getHoscode());
        map.put("hosRecordId",orderInfo.getHosRecordId());
        map.put("timestamp",HttpRequestHelper.getTimestamp());
        String sign=HttpRequestHelper.getSign(map,signInfoVo.getSignKey());
        map.put("sign",sign);

        JSONObject result=HttpRequestHelper.sendRequest(map,signInfoVo.getApiUrl()+"/order/updateCancelStatus");
        //根据医院接口返回数据
        if (result.getInteger("code")!=200) {
            throw new HospitalException(result.getString("message"),ResultCodeEnum.FAIL.getCode());
        } else {
            //判断当前订单是否可以取消
            if (orderInfo.getOrderStatus().intValue()==OrderStatusEnum.PAID.getStatus().intValue()) {
                //退款
                boolean isRefund = weixinService.refund(orderId);
                if (!isRefund) {
                    throw new HospitalException(ResultCodeEnum.CANCEL_ORDER_FAIL);
                }
                //更新订单状况
                orderInfo.setOrderStatus(OrderStatusEnum.CANCLE.getStatus());
                baseMapper.updateById(orderInfo);

                //发送mq信息更新预约数 我们与下单成功更新预约数使用相同的mq信息，不设置可预约数与剩余预约数，接收端可预约数减1即可
                OrderMqVo orderMqVo = new OrderMqVo();
                orderMqVo.setScheduleId(orderInfo.getScheduleId());
                //短信提示
                MsmVo msmVo = new MsmVo();
                msmVo.setPhone(orderInfo.getPatientPhone());
                msmVo.setTemplateCode("SMS_194640722");
                String reserveDate = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + (orderInfo.getReserveTime()==0 ? "上午": "下午");
                Map<String,Object> param = new HashMap<String,Object>(){{
                    put("title", orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle());
                    put("reserveDate", reserveDate);
                    put("name", orderInfo.getPatientName());
                }};
                msmVo.setParam(param);
                orderMqVo.setMsmVo(msmVo);
                rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);
            }
        }
        return true;
    }

    //就诊通知
    @Override
    public void patientTips() {
        //查询出需要就诊的对象信息
        QueryWrapper<OrderInfo> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("reserve_date",new DateTime().toString("yyyy-MM-dd")); //当天时间
        queryWrapper.ne("order_status",OrderStatusEnum.CANCLE.getStatus());  //除去取消预约的人
        List<OrderInfo> orderInfoList = baseMapper.selectList(queryWrapper);
        for (OrderInfo orderInfo:orderInfoList) {
            //短信提示
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
            String reserveDate = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + (orderInfo.getReserveTime()==0 ? "上午": "下午");
            Map<String,Object> param = new HashMap<String,Object>(){{
                put("title", orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle());
                put("reserveDate", reserveDate);
                put("name", orderInfo.getPatientName());
            }};
            msmVo.setParam(param);
//            System.out.println(msmVo);
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM, msmVo);
        }
    }

    //预约统计
    @Override
    public Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo) {
        List<OrderCountVo> orderCountVoList = orderMapper.selectOrderCount(orderCountQueryVo);
        //获取x轴需要的数据，日期数据
        List<String> dateList = orderCountVoList.stream().map(OrderCountVo::getReserveDate).collect(Collectors.toList());
        //获取y轴需要的数据，具体数据
        List<Integer> countList = orderCountVoList.stream().map(OrderCountVo::getCount).collect(Collectors.toList());
        //使用map集合对数据进行封装
        Map<String,Object> map=new HashMap<>();
        map.put("dataList",dateList);
        map.put("countList",countList);
        return map;
    }
}
