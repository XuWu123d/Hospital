import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

import java.util.Map;

public class ExcelListener extends AnalysisEventListener<User> {

    //一行一行读取数据，从第二行开始，不包括表头信息
    @Override
    public void invoke(User user, AnalysisContext analysisContext) {
        System.out.println(user.getId()+"  "+user.getName());
    }

    //读取表头信息
    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        System.out.println("表头信息"+headMap);
    }

    //读取之后执行
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
