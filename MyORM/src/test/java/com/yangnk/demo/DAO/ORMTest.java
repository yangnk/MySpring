package com.yangnk.demo.DAO;

import com.alibaba.fastjson.JSON;
import com.yangnk.demo.entity.Member;
import com.yangnk.framework.common.Page;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import java.text.SimpleDateFormat;
import java.util.List;

@ContextConfiguration(locations = {"classpath:application-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class ORMTest {


    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmdd");

    @Autowired
    private MemberDAO memberDao;

    @Test
    public void testSelectForPage(){
        try {
            Page page = memberDao.selectForPage(2, 3);
            System.out.println("总条数： " + page.getTotal());
            System.out.println("当前第几页：" + page.getPageNo());
            System.out.println("每页多少条：" + page.getPageSize());
            System.out.println("本页的数据：" + JSON.toJSONString(page.getRows(),true));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void testSelectAllForMember(){
        try {
            List<Member> result = memberDao.selectAll();
            System.out.println(JSON.toJSONString(result,true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testInsertMember(){
        try {
            for (int age = 25; age < 35; age++) {
                Member member = new Member();
                member.setAge(age);
                member.setName("Tom");
                member.setAddr("Hunan Changsha");
                memberDao.insert(member);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}