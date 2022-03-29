package com.hitcoder.network.model

/**
 * 文件下载策略
 *
 * [COVER] : 覆盖原文件
 *
 * [RENAME] : 文件名称冲突时，重命名
 *
 * [IF_EXIT] : 如果下载路径有文件，则直接返回下载成功
 */
enum class Cover {
    /**
     * 覆盖原文件
     */
    COVER,

    /**
     * 文件名称冲突时，重命名
     */
    RENAME,

    /**
     * 如果下载路径有文件，则直接返回下载成功
     */
    IF_EXIT
}