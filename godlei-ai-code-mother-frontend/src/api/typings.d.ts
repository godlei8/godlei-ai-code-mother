declare namespace API {
  type LongId = string

  type App = {
    id?: LongId
    appName?: string
    cover?: string
    initPrompt?: string
    codeGenType?: string
    deployKey?: string
    deployedTime?: string
    priority?: number
    userId?: LongId
    editTime?: string
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type AppAddRequest = {
    appName?: string
    initPrompt?: string
  }

  type AppAdminQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: LongId
    appName?: string
    cover?: string
    initPrompt?: string
    codeGenType?: string
    deployKey?: string
    priority?: number
    userId?: LongId
  }

  type AppAdminUpdateRequest = {
    id?: LongId
    appName?: string
    cover?: string
    priority?: number
  }

  type AppDeployRequest = {
    appId?: LongId
  }

  type AppListPageRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    appName?: string
  }

  type AppUserUpdateRequest = {
    id?: LongId
    appName?: string
  }

  type AppVO = {
    id?: LongId
    appName?: string
    cover?: string
    initPrompt?: string
    codeGenType?: string
    deployKey?: string
    deployedTime?: string
    priority?: number
    userId?: LongId
    editTime?: string
    createTime?: string
    updateTime?: string
  }

  type BaseResponseApp = {
    code?: number
    data?: App
    message?: string
  }

  type BaseResponseAppVO = {
    code?: number
    data?: AppVO
    message?: string
  }

  type BaseResponseBoolean = {
    code?: number
    data?: boolean
    message?: string
  }

  type BaseResponseChatHistoryCursorVO = {
    code?: number
    data?: ChatHistoryCursorVO
    message?: string
  }

  type BaseResponseLoginUserVO = {
    code?: number
    data?: LoginUserVO
    message?: string
  }

  type BaseResponseLong = {
    code?: number
    data?: LongId
    message?: string
  }

  type BaseResponsePageAppVO = {
    code?: number
    data?: PageAppVO
    message?: string
  }

  type BaseResponsePageChatHistoryAdminVO = {
    code?: number
    data?: PageChatHistoryAdminVO
    message?: string
  }

  type BaseResponsePageUserVO = {
    code?: number
    data?: PageUserVO
    message?: string
  }

  type BaseResponseString = {
    code?: number
    data?: string
    message?: string
  }

  type BaseResponseUser = {
    code?: number
    data?: User
    message?: string
  }

  type BaseResponseUserVO = {
    code?: number
    data?: UserVO
    message?: string
  }

  type ChatHistoryAdminQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    appId?: LongId
    userId?: LongId
    messageType?: string
  }

  type ChatHistoryAdminVO = {
    id?: LongId
    message?: string
    messageType?: string
    appId?: LongId
    userId?: LongId
    createTime?: string
    appName?: string
  }

  type ChatHistoryCursorVO = {
    records?: ChatHistoryVO[]
    hasMore?: boolean
    nextBeforeCreateTime?: string
    nextBeforeId?: LongId
  }

  type ChatHistoryLatestRequest = {
    appId?: LongId
    pageSize?: number
  }

  type ChatHistoryOlderRequest = {
    appId?: LongId
    pageSize?: number
    beforeCreateTime?: string
    beforeId?: LongId
  }

  type ChatHistoryVO = {
    id?: LongId
    message?: string
    messageType?: string
    appId?: LongId
    userId?: LongId
    createTime?: string
  }

  type chatToGenCodeParams = {
    appId: LongId
    message: string
  }

  type DeleteRequest = {
    id?: LongId
  }

  type getAppByIdAdminParams = {
    id: LongId
  }

  type getAppVOParams = {
    id: LongId
  }

  type getUserByIdParams = {
    id: LongId
  }

  type getUserVOByIdParams = {
    id: LongId
  }

  type LoginUserVO = {
    id?: LongId
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
    updateTime?: string
  }

  type PageAppVO = {
    records?: AppVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageChatHistoryAdminVO = {
    records?: ChatHistoryAdminVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageUserVO = {
    records?: UserVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type ServerSentEventString = true

  type serveStaticResourceParams = {
    deployKey: string
  }

  type User = {
    id?: LongId
    userAccount?: string
    userPassword?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    editTime?: string
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type UserAddRequest = {
    userName?: string
    userAccount?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserLoginRequest = {
    userAccount?: string
    userPassword?: string
  }

  type UserPasswordUpdateRequest = {
    oldPassword?: string
    newPassword?: string
    checkPassword?: string
  }

  type UserProfileUpdateRequest = {
    userName?: string
    userAvatar?: string
    userProfile?: string
  }

  type UserQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: LongId
    userName?: string
    userAccount?: string
    userProfile?: string
    userRole?: string
  }

  type UserRegisterRequest = {
    userAccount?: string
    userPassword?: string
    checkPassword?: string
  }

  type UserUpdateRequest = {
    id?: LongId
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserVO = {
    id?: LongId
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
  }
}
