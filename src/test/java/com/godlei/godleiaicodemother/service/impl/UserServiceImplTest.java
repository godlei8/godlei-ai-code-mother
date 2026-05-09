package com.godlei.godleiaicodemother.service.impl;

import com.godlei.godleiaicodemother.exception.BusinessException;
import com.godlei.godleiaicodemother.exception.ErrorCode;
import com.godlei.godleiaicodemother.model.dto.user.UserPasswordUpdateRequest;
import com.godlei.godleiaicodemother.model.dto.user.UserProfileUpdateRequest;
import com.godlei.godleiaicodemother.model.entity.User;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class UserServiceImplTest {

    @Test
    void updateMyUserShouldPersistCurrentLoginUserProfile() {
        UserServiceImpl userService = spy(new UserServiceImpl());
        doReturn(true).when(userService).updateById(any(User.class));

        User loginUser = User.builder().id(123L).build();
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setUserName("新昵称");
        request.setUserAvatar("https://example.com/avatar.png");
        request.setUserProfile("新的个人简介");

        boolean result = userService.updateMyUser(request, loginUser);

        assertTrue(result);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).updateById(userCaptor.capture());
        assertEquals(loginUser.getId(), userCaptor.getValue().getId());
        assertEquals("新昵称", userCaptor.getValue().getUserName());
        assertEquals("https://example.com/avatar.png", userCaptor.getValue().getUserAvatar());
        assertEquals("新的个人简介", userCaptor.getValue().getUserProfile());
    }

    @Test
    void changeMyPasswordShouldRejectWrongOldPassword() {
        UserServiceImpl userService = spy(new UserServiceImpl());
        User loginUser = User.builder()
                .id(123L)
                .userPassword(userService.getEncryptPassword("oldPassword123"))
                .build();
        UserPasswordUpdateRequest request = new UserPasswordUpdateRequest();
        request.setOldPassword("wrongPassword123");
        request.setNewPassword("newPassword123");
        request.setCheckPassword("newPassword123");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.changeMyPassword(request, loginUser));

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void changeMyPasswordShouldPersistEncryptedPasswordWhenRequestIsValid() {
        UserServiceImpl userService = spy(new UserServiceImpl());
        doReturn(true).when(userService).updateById(any(User.class));

        User loginUser = User.builder()
                .id(123L)
                .userPassword(userService.getEncryptPassword("oldPassword123"))
                .build();
        UserPasswordUpdateRequest request = new UserPasswordUpdateRequest();
        request.setOldPassword("oldPassword123");
        request.setNewPassword("newPassword123");
        request.setCheckPassword("newPassword123");

        boolean result = userService.changeMyPassword(request, loginUser);

        assertTrue(result);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).updateById(userCaptor.capture());
        assertEquals(loginUser.getId(), userCaptor.getValue().getId());
        assertEquals(userService.getEncryptPassword("newPassword123"), userCaptor.getValue().getUserPassword());
    }
}
