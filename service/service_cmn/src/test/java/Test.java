import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;

import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        String url="D:\\1.xlsx";
//        List<User> list=new ArrayList<>();
//        for (int i=0;i<10;i++) {
//            User user=new User();
//            user.setId(i);
//            user.setName("小明"+i);
//            list.add(user);
//        }
//        EasyExcel.write(url,User.class).sheet().doWrite(list);

        EasyExcel.read(url, User.class, new ExcelListener()).sheet().doRead();
    }
}
