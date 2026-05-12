package com.godlei.godleiaicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.godlei.godleiaicodemother.exception.BusinessException;
import com.godlei.godleiaicodemother.exception.ErrorCode;
import com.godlei.godleiaicodemother.exception.ThrowUtils;
import com.godlei.godleiaicodemother.manager.CosManager;
import com.godlei.godleiaicodemother.mapper.UserMapper;
import com.godlei.godleiaicodemother.model.dto.user.UserPasswordUpdateRequest;
import com.godlei.godleiaicodemother.model.dto.user.UserProfileUpdateRequest;
import com.godlei.godleiaicodemother.model.dto.user.UserQueryRequest;
import com.godlei.godleiaicodemother.model.entity.User;
import com.godlei.godleiaicodemother.model.enums.UserRoleEnum;
import com.godlei.godleiaicodemother.model.vo.LoginUserVO;
import com.godlei.godleiaicodemother.model.vo.UserVO;
import com.godlei.godleiaicodemother.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.godlei.godleiaicodemother.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final long AVATAR_MAX_SIZE = 5L * 1024 * 1024;

    private static final Set<String> ALLOWED_AVATAR_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");

    private static final Set<String> ALLOWED_AVATAR_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    @Resource
    private CosManager cosManager;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.mapper.selectCountByQuery(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }

        String encryptPassword = getEncryptPassword(userPassword);
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("无名");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "注册失败，数据库错误");
        }
        return user.getId();
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度过短");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过短");
        }

        String encryptPassword = getEncryptPassword(userPassword);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.mapper.selectOneByQuery(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }

        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    @Override
    public boolean updateMyUser(UserProfileUpdateRequest userProfileUpdateRequest, User loginUser) {
        ThrowUtils.throwIf(userProfileUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null || loginUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
        if (StrUtil.isBlank(userProfileUpdateRequest.getUserName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户昵称不能为空");
        }

        User user = new User();
        user.setId(loginUser.getId());
        user.setUserName(userProfileUpdateRequest.getUserName().trim());
        user.setUserAvatar(StrUtil.trim(userProfileUpdateRequest.getUserAvatar()));
        user.setUserProfile(StrUtil.trim(userProfileUpdateRequest.getUserProfile()));

        boolean result = this.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return true;
    }

    @Override
    public String uploadAvatar(MultipartFile avatarFile, User loginUser) {
        ThrowUtils.throwIf(avatarFile == null || avatarFile.isEmpty(), ErrorCode.PARAMS_ERROR, "头像文件不能为空");
        ThrowUtils.throwIf(loginUser == null || loginUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
        validateAvatarFile(avatarFile);

        String extension = resolveAvatarExtension(avatarFile);
        File tempFile = null;
        try {
            tempFile = File.createTempFile("avatar-", "." + extension);
            avatarFile.transferTo(tempFile);
            String avatarKey = buildAvatarKey(loginUser.getId(), extension);
            String avatarUrl = cosManager.uploadFile(avatarKey, tempFile);
            ThrowUtils.throwIf(StrUtil.isBlank(avatarUrl), ErrorCode.OPERATION_ERROR, "头像上传失败");
            return avatarUrl;
        } catch (IOException e) {
            log.error("头像上传失败, userId: {}", loginUser.getId(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "头像上传失败");
        } finally {
            if (tempFile != null && tempFile.exists()) {
                FileUtil.del(tempFile);
            }
        }
    }

    @Override
    public boolean changeMyPassword(UserPasswordUpdateRequest userPasswordUpdateRequest, User loginUser) {
        ThrowUtils.throwIf(userPasswordUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null || loginUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);

        String oldPassword = userPasswordUpdateRequest.getOldPassword();
        String newPassword = userPasswordUpdateRequest.getNewPassword();
        String checkPassword = userPasswordUpdateRequest.getCheckPassword();

        if (StrUtil.hasBlank(oldPassword, newPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        if (newPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过短");
        }
        if (!newPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的新密码不一致");
        }

        String encryptOldPassword = getEncryptPassword(oldPassword);
        if (!encryptOldPassword.equals(loginUser.getUserPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "旧密码错误");
        }

        User user = new User();
        user.setId(loginUser.getId());
        user.setUserPassword(getEncryptPassword(newPassword));

        boolean result = this.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return true;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream()
                .map(this::getUserVO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户未登录");
        }
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .eq("userRole", userRole)
                .like("userAccount", userAccount)
                .like("userName", userName)
                .like("userProfile", userProfile)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public String getEncryptPassword(String userPassword) {
        final String salt = "yupi";
        return DigestUtils.md5DigestAsHex((userPassword + salt).getBytes(StandardCharsets.UTF_8));
    }

    private void validateAvatarFile(MultipartFile avatarFile) {
        ThrowUtils.throwIf(avatarFile.getSize() > AVATAR_MAX_SIZE, ErrorCode.PARAMS_ERROR, "头像大小不能超过 5MB");

        String extension = resolveAvatarExtension(avatarFile);
        ThrowUtils.throwIf(!ALLOWED_AVATAR_EXTENSIONS.contains(extension), ErrorCode.PARAMS_ERROR, "头像格式不支持");

        String contentType = StrUtil.trim(avatarFile.getContentType());
        ThrowUtils.throwIf(StrUtil.isBlank(contentType) || !ALLOWED_AVATAR_CONTENT_TYPES.contains(contentType),
                ErrorCode.PARAMS_ERROR, "头像格式不支持");
    }

    private String resolveAvatarExtension(MultipartFile avatarFile) {
        String extension = FileUtil.extName(avatarFile.getOriginalFilename());
        return StrUtil.blankToDefault(StrUtil.trim(extension).toLowerCase(), "jpg");
    }

    private String buildAvatarKey(Long userId, String extension) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = String.format("user-%s-%s.%s", userId, UUID.randomUUID().toString().replace("-", ""), extension);
        return String.format("/avatars/%s/%s", datePath, fileName);
    }
}
