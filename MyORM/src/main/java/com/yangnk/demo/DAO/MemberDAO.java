package com.yangnk.demo.DAO;

import com.yangnk.demo.entity.Member;
import com.yangnk.framework.common.Page;
import com.yangnk.framework.core.BaseDaoSupport;
import com.yangnk.framework.core.QueryRule;
import org.springframework.stereotype.Repository;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Repository
public class MemberDAO extends BaseDaoSupport<Member,Long> {

    @Override
    protected String getPKColumn() {
        return "id";
    }

    @Resource(name="dataSource1")
    public void setDataSource(DataSource dataSource){
        super.setDataSourceReadOnly(dataSource);
        super.setDataSourceWrite(dataSource);
    }


    public List<Member> selectAll() throws  Exception{
        QueryRule queryRule = QueryRule.getInstance();
        queryRule.andLike("name","Mic%");
        return super.select(queryRule);
    }


    public Page<Member> selectForPage(int pageNo, int pageSize) throws Exception{
        QueryRule queryRule = QueryRule.getInstance();
        queryRule.andLike("name","Tom%");
        Page<Member> page = super.select(queryRule,pageNo,pageSize);
        return page;
    }

    public void select() throws Exception{
        String sql = "";
        List<Map<String,Object>> result = super.selectBySql(sql);
    }

    public boolean insert(Member entity) throws Exception{
        super.setTableName("t_mmmmm");
        return super.insert(entity);
    }
}
