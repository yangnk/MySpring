package com.yangnk.mySpringMVC.test;

import javax.persistence.Column;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class JdbcTest {
    public static void main(String[] args) {
        Member condition = new Member();
        List<?> result = process(condition);
        System.out.println(Arrays.toString(result.toArray()));
    }

    private static List<?> process(Member condition) {
        Class<? extends Member> clazz = condition.getClass();
        List<Object> result = null;
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            //1.加载驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
            //2.建立连接
            con = DriverManager
                    .getConnection("jdbc:mysql://127.0.0.1:3306/test_db?serverTimezone=Asia/Shanghai","root","666666");

            //获取Field-Column和Column-Field的Map
            HashMap<String, String> fieldColumn = new HashMap();
            HashMap<String, String> columnField = new HashMap();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field :
                    fields) {
                //打开权限
                field.setAccessible(true);
                String fieldName = field.getName();
                String columnName = null;
                //判断是否有自定义名字的注解
                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    columnName = column.name();
                    fieldColumn.put(fieldName, columnName);
                    columnField.put(columnName, fieldName);
                } else {
                    fieldColumn.put(fieldName, fieldName);
                    columnField.put(fieldName, fieldName);
                }
            }
            //2.创建语句集
            StringBuilder sql = new StringBuilder();
            Table table = clazz.getAnnotation(Table.class);
            String tableName = table.name();
            sql.append("select * from " + tableName + " where 1=1 ");

            //拼接sql
            for (Field field : fields) {
                Object value = field.get(condition);
                if(null != value){
                    if(String.class == field.getType()){
                        sql.append(" and " + columnField.get(field.getName()) + " = '" + value + "'");
                    }else{
                        sql.append(" and " + columnField.get(field.getName()) + " = " + value);
                    }
                }
            }
            statement = con.prepareStatement(sql.toString());
            //4.执行获取结果集
            resultSet = statement.executeQuery();
            int columnCounts = resultSet.getMetaData().getColumnCount();
            result = new ArrayList();
            while (resultSet.next()){
                Object instance = clazz.newInstance();
                for (int i = 1; i <= columnCounts; i++) {
                    String columnName = resultSet.getMetaData().getColumnName(i);
                    Field field = clazz.getDeclaredField(fieldColumn.get(columnName));
                    field.setAccessible(true);
                    field.set(instance,resultSet.getObject(columnName));
                }
                result.add(instance);
            }
//            System.out.println(Arrays.toString(result.toArray()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        //5.关闭连接
        finally {
            try {
                con.close();
                statement.close();
                resultSet.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return result;
    }
}
