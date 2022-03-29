package com.hitcoder.network.interfaces

import com.hitcoder.network.model.Cover

interface FileDownload {

    /**
     * 是否覆盖下载
     *
     * 默认值为[Cover.COVER] 支持断点续传的覆盖下载
     */
    fun cover(): Cover = Cover.COVER
    
    /**
     * 开始下载文件
     */
    fun downloadStart()

    /**
     * 下载进度更新，预计会调用100次
     */
    fun downloadInProgress(writeBytes: Long, totalLength: Long)

    /**
     * 下载升级文件成功
     *
     * 当[cover]返回值为[Cover.RENAME]时，下载文件遇到同名文件时，会按照重命名规则
     * 获取新的文件名称，传入回调函数的路径为新文件名对应的路径
     *
     * @param filePath 文件保存路径
     */
    fun downloadSuccess(filePath: String)

    /**
     * 升级文件下载失败
     * @param errorMessage 失败原因
     */
    fun downloadFail(errorMessage: String)

    /**
     * 取消下载升级文件
     */
    fun downloadCancel() {}

}