import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class User {
    @ExcelProperty(value = "用户编号",index = 0)  //第一列内容
    private int id;
    @ExcelProperty(value = "用户姓名",index = 1)  //第二列内容
    private String name;
}
