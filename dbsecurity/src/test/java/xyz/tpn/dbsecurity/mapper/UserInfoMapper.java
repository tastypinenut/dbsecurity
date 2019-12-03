package xyz.tpn.dbsecurity.mapper;


import com.sun.xml.internal.ws.api.server.InstanceResolverAnnotation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.*;
import xyz.tpn.dbsecurity.model.UserInfo;

import java.util.List;

public interface UserInfoMapper {

    @Select("select * from t_user_info i where id=#{id}")
    UserInfo selectById(@Param("id") Long id);

    @Select("select * from t_user_info i")
    List<UserInfo> selectAll();

    @Select("select i.* from test.t_user_info i, test.t_param l where i.bank_card_no=l.param_value and l.param_name='bankCard'")
    List<UserInfo> joinSelectAllWithDatabaseName();

    @Select("select i.* from t_user_info i, t_param l where i.bank_card_no=l.param_value and l.param_name='bankCard'")
    List<UserInfo> joinSelectAll();

    @Select("select i.* from t_user_info i where i.bank_card_no in (select param_value from t_param l where l.id=2)")
    List<UserInfo> subSelectAll();

    @Select("select i.* from t_user_info i where i.bank_card_no in (select param_value from t_param l where l.param_value=#{phone} and l.param_name='bankCard') and i.name=#{name} and i.type=#{type}")
    List<UserInfo> subSelectAllWithParam(@Param("name") String name, @Param("type") String type, @Param("phone") String phone);

    @Select("select * from t_user_info i where name=#{name}")
    List<UserInfo> selectByName(@Param("name") String name);

    @Select("select * from t_user_info i where name='${name}'")
    List<UserInfo> selectByNameVariable(@Param("name") String name);

    @Select("select * from t_user_info i where name in (#{name}, #{name2})")
    List<UserInfo> selectByInName(@Param("name") String name, @Param("name2") String name2);

    @Select("select * from t_user_info i where type=#{type}")
    List<UserInfo> selectByType(@Param("type") String type);

    @Select("select count(name), name from t_user_info i where name = #{name}")
    Integer countByName(@Param("name") String name);

    @Select("select i.* from t_user_info i where i.id in (select param_value from t_param l where l.param_value=#{phone} and l.param_name='bankCard') and i.name=#{name}" +
            " union" +
            " select i.* from t_user_info i where i.type=#{type}")
    List<UserInfo> unionSelectAllWithParam(@Param("name") String name, @Param("type") String type, @Param("phone") String phone);

    @Select("select name from t_user_info i where name = #{name} group by name")
    List<UserInfo> selectByNameGroupBy(@Param("name") String name);

    @Select("select * from t_user_info i where name = #{name} order by name DESC")
    List<UserInfo> selectByNameOrderBy(@Param("name") String name);

    @Insert("insert into t_user_info(id, name, bank_card_no, type) values(#{id}, #{name}, #{bankCardNo}, #{type})")
    void insert(UserInfo userInfo);

    void insertBatch(List<UserInfo> userInfoList);

    @Update("update t_user_info set name= #{name}, bank_card_no = #{bankCardNo}, type='USER' where id=#{id}")
    void updateById(UserInfo userInfo);

    @Update("update t_user_info set type='USER', id=id, name= #{name}, bank_card_no = #{bankCardNo} where id=#{id} and type=#{type} and name=#{name}")
    void updateById2(UserInfo userInfo);

    @Update("update t_user_info set bank_card_no = #{bankCardNo}, type=#{type} where name= #{name}")
    void updateByName(UserInfo newUserInfo);

    @Update("update t_user_info set bank_card_no = #{user.bankCardNo}, type=#{user.type}, name=#{user.name} where name= #{name}")
    void updateUserInfoByName(@Param("user") UserInfo newUserInfo, @Param("name") String name);

    @Delete("delete from t_user_info where name=#{name}")
    void deleteByName(@Param("name") String name);

    @Delete("delete from t_user_info where name in (#{name},#{name2})")
    void deleteByInName(@Param("name") String name, @Param("name2") String name2);

    @Delete("delete from t_user_info")
    void deleteAll();

}
