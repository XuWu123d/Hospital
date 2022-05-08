package com.mingri.yygh.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.yygh.cmn.listener.DictListener;
import com.mingri.yygh.cmn.mapper.CmnMapper;
import com.mingri.yygh.cmn.service.CmnService;
import com.mingri.yygh.model.cmn.Dict;
import com.mingri.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CmnServiceImpl extends ServiceImpl<CmnMapper, Dict> implements CmnService {

    //根据数据id查询子类数据列表
    @Override
    public List<Dict> findChildData(Long id) {
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("parent_id",id);
        //获取所有子类数据
        List<Dict> list = baseMapper.selectList(queryWrapper);
        for (Dict dict : list) {
            Long id1 = dict.getId();
            //子类是否还有子类
            boolean hasChildData = isHasChildData(id1);
            //实体类中设置是否有子类
            dict.setHasChildren(hasChildData);
        }
        return list;
    }

    //判断id下是否有子接口
    private boolean isHasChildData(Long id) {
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("parent_id",id);
        Integer integer = baseMapper.selectCount(queryWrapper);
        return integer>0;
    }

    //导出数据字典接口
    @Override
    public void exportDictData(HttpServletResponse response) {
        //设置下载信息
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String filename="dict";
        response.setHeader("Content-disposition","attachment;filename"+filename+".xlsx");
        //查询数据库
        List<Dict> dictList= baseMapper.selectList(null);
        //Dict转换为DictEeVo
        List<DictEeVo> dictEeVoList=new ArrayList<>();
        for (Dict dict : dictList) {
            DictEeVo dictEeVo=new DictEeVo();
            BeanUtils.copyProperties(dict,dictEeVo);
            dictEeVoList.add(dictEeVo);
        }
        //调用方法进行写操作
        try {
            EasyExcel.write(response.getOutputStream(), DictEeVo.class).sheet("dict")
                    .doWrite(dictEeVoList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //导入数据字典接口
    @Override
    public void importDictData(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(),DictEeVo.class,new DictListener(baseMapper)).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
