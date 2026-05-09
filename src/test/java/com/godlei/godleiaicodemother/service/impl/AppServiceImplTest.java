package com.godlei.godleiaicodemother.service.impl;

import com.godlei.godleiaicodemother.constant.UserConstant;
import com.godlei.godleiaicodemother.exception.BusinessException;
import com.godlei.godleiaicodemother.exception.ErrorCode;
import com.godlei.godleiaicodemother.model.entity.App;
import com.godlei.godleiaicodemother.model.entity.User;
import com.godlei.godleiaicodemother.model.vo.AppVO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

class AppServiceImplTest {

    @Test
    void getAppVOByUserShouldAllowAdminToViewOtherUsersApp() {
        AppServiceImpl appService = spy(new AppServiceImpl());
        App app = App.builder().id(123L).appName("admin-visible-app").userId(999L).build();
        doReturn(app).when(appService).getById(123L);

        User loginUser = User.builder().id(1L).userRole(UserConstant.ADMIN_ROLE).build();

        AppVO result = appService.getAppVOByUser(123L, loginUser);

        assertNotNull(result);
        assertEquals(app.getId(), result.getId());
        assertEquals(app.getAppName(), result.getAppName());
        assertEquals(app.getUserId(), result.getUserId());
    }

    @Test
    void getAppVOByUserShouldRejectNonOwnerNonAdmin() {
        AppServiceImpl appService = spy(new AppServiceImpl());
        App app = App.builder().id(123L).appName("private-app").userId(999L).build();
        doReturn(app).when(appService).getById(123L);

        User loginUser = User.builder().id(1L).userRole(UserConstant.DEFAULT_ROLE).build();

        BusinessException exception = assertThrows(BusinessException.class,
                () -> appService.getAppVOByUser(123L, loginUser));

        assertEquals(ErrorCode.NO_AUTH_ERROR.getCode(), exception.getCode());
    }
}
