package com.mingri.yygh.cmn.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.mingri.yygh.cmn.mapper.CmnMapper;
import com.mingri.yygh.model.cmn.Dict;
import com.mingri.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class DictListener extends AnalysisEventListener<DictEeVo> {
    private CmnMapper cmnMapper;

    public DictListener(CmnMapper cmnMapper) {
        this.cmnMapper=cmnMapper;
    }

    //一行一行读取数据
    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext analysisContext) {
        Dict dict=new Dict();
        dict.setIsDeleted(0);  //默认逻辑删除为0，即不删除
        BeanUtils.copyProperties(dictEeVo,dict);
        cmnMapper.insert(dict);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
