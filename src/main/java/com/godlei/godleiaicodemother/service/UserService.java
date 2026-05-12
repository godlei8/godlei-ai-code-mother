package com.godlei.godleiaicodemother.service;

import com.godlei.godleiaicodemother.model.dto.user.UserPasswordUpdateRequest;
import com.godlei.godleiaicodemother.model.dto.user.UserProfileUpdateRequest;
import com.godlei.godleiaicodemother.model.dto.user.UserQueryRequest;
import com.godlei.godleiaicodemother.model.entity.User;
import com.godlei.godleiaicodemother.model.vo.LoginUserVO;
import com.godlei.godleiaicodemother.model.vo.UserVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 当前登录用户更新个人资料
     *
     * @param userProfileUpdateRequest 资料更新请求
     * @param loginUser 当前登录用户
     * @return 是否更新成功
     */
    boolean updateMyUser(UserProfileUpdateRequest userProfileUpdateRequest, User loginUser);

    /**
     * 上传当前登录用户头像到 COS 并返回访问 URL
     *
     * @param avatarFile 上传的头像文件
     * @param loginUser 当前登录用户
     * @return 头像 URL
     */
    String uploadAvatar(MultipartFile avatarFile, User loginUser);

    /**
     * 当前登录用户修改密码
     *
     * @param userPasswordUpdateRequest 密码更新请求
     * @param loginUser 当前登录用户
     * @return 是否更新成功
     */
    boolean changeMyPassword(UserPasswordUpdateRequest userPasswordUpdateRequest, User loginUser);

    /**
     * 获取脱敏后的用户信息
     *
     * @param user 用户信息
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏后的用户信息（分页）
     *
     * @param userList 用户列表
     * @return
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 用户注销
     *
     * @param request
     * @return 退出登录是否成功
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 根据查询条件构造数据查询参数
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 加密
     *
     * @param userPassword 用户密码
     * @return 加密后的用户密码
     */
    String getEncryptPassword(String userPassword);
}
