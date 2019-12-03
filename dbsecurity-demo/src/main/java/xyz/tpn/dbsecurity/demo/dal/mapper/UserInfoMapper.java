package xyz.tpn.dbsecurity.demo.dal.mapper;


import xyz.tpn.dbsecurity.demo.dal.model.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserInfoMapper {

    List<UserInfo> selectAll();

    List<UserInfo> joinSelectAll();

    List<UserInfo> selectByName(@Param("name") String name);

    List<UserInfo> selectByType(@Param("type") String type);

    void insert(UserInfo userInfo);

    void insertBatch(List<UserInfo> userInfoList);

    void updateById(UserInfo userInfo);

    void updateByName(UserInfo newUserInfo);

    void updateUserInfoByName(@Param("user") UserInfo newUserInfo, @Param("name") String name);

    void deleteAll();

    UserInfo selectById(Integer id);
}
