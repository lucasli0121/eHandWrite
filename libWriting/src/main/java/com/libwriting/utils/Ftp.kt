package com.libwriting.utils

import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import org.apache.commons.net.ftp.FTPReply
import org.apache.commons.net.io.CopyStreamEvent
import org.apache.commons.net.io.CopyStreamListener
import java.io.*

/*
    提供FTP得传输功能
 */
object Ftp {
    /**
     * Description: 向FTP服务器上传文件
     *
     * @param url
     * FTP服务器hostname
     * @param port
     * FTP服务器端口，如果默认端口请写-1
     * @param username
     * FTP登录账号
     * @param password
     * FTP登录密码
     * @param path
     * FTP服务器保存目录
     * @param filename
     * 上传到FTP服务器上的文件名
     * @param input
     * 输入流
     * @return 成功返回true，否则返回false
     */
    fun uploadFile(
        url: String?,
        port: Int,
        username: String?,
        password: String?,
        path: String?,
        filename: String?,
        input: InputStream,
        listener: OnTranslateListener?
    ): Boolean {
        var success = false
        val ftp = FTPClient()
        ftp.copyStreamListener = object : CopyStreamListener {
            override fun bytesTransferred(event: CopyStreamEvent?) {
                // TODO Auto-generated method stub
            }

            override fun bytesTransferred(totalBytes: Long, bytes: Int, arg2: Long) {
                // TODO Auto-generated method stub
                listener?.OnTranslate(totalBytes, bytes.toLong())
            }
        }
        while (!success) {
            try {
                if (ftp.isConnected) {
                    ftp.disconnect()
                }
                // 连接FTP服务器
                if (port > -1) {
                    ftp.connect(url, port)
                } else {
                    ftp.connect(url)
                }
                var reply: Int = ftp.replyCode
                if (FTPReply.isPositiveCompletion(reply)) {
                    // 登录FTP
                    val loginResult: Boolean = ftp.login(username, password)
                    if (loginResult) {
                        ftp.setFileType(FTPClient.BINARY_FILE_TYPE)
                        ftp.changeWorkingDirectory(path)
                        ftp.bufferSize = 2048
                        ftp.enterLocalPassiveMode()
                        ftp.controlEncoding = "UTF-8"
                        //						FTPFile[] files = ftp.listFiles(path);
//						for(int i = 0; i < files.length; i++) {
//							if(files[i].getName().equals(filename)) {
//								ftp.deleteFile(filename);
//								break;
//							}
//						}
                        val uploadRes: Boolean = ftp.storeFile(filename, input)
                        input.close()
                        success = true
                        ftp.logout()
                    }
                }
            } catch (e: IOException) {
                success = false
                e.printStackTrace()
            } finally {
                if (ftp.isConnected) {
                    try {
                        ftp.disconnect()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return success
    }

    fun uploadFile(
        url: String?,
        port: Int,
        username: String?,
        password: String?,
        path: String?,
        remoteFile: String?,
        localFile: String?,
        listener: OnTranslateListener?
    ): Boolean {
        try {
            val file = File(localFile)
            val input: InputStream = FileInputStream(file)
            return uploadFile(url, port, username, password, path, remoteFile, input, listener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * Description: 从FTP服务器下载文件
     *
     * @param url
     * FTP服务器hostname
     * @param port
     * FTP服务器端口
     * @param username
     * FTP登录账号
     * @param password
     * FTP登录密码
     * @param remotePath
     * FTP服务器上的相对路径
     * @param fileName
     * 要下载的文件名
     * @param localPath
     * 下载后保存到本地的路径
     * @return
     */
    fun downloadFile(
        url: String?, port: Int, username: String?,
        password: String?, remotePath: String?, fileName: Array<String>,
        localPath: String
    ): Boolean {
        var success = false
        val ftp = FTPClient()
        try {

            // 连接FTP服务器
            if (port > -1) {
                ftp.connect(url, port)
            } else {
                ftp.connect(url)
            }
            ftp.login(username, password) // 登录
            val reply: Int = ftp.replyCode
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect()
                return success
            }
            ftp.changeWorkingDirectory(remotePath) // 转移到FTP服务器目录
            val fs: Array<FTPFile> = ftp.listFiles()
            for (i in fileName.indices) {
                for (ff in fs) {
                    val name = String(ff.name.toByteArray())
                    if (name == fileName[i]) {
                        val localFile = File("$localPath/$name")
                        val `is`: OutputStream = FileOutputStream(localFile)
                        ftp.retrieveFile(name, `is`)
                        `is`.close()
                    }
                }
            }
            ftp.logout()
            success = true
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (ftp.isConnected) {
                try {
                    ftp.disconnect()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return success
    }

    /**
     * <删除FTP上的文件> <远程删除FTP服务器上的录音文件>
     *
     * @param url
     * FTP服务器IP地址
     * @param port
     * FTP服务器端口
     * @param username
     * FTP服务器登录名
     * @param password
     * FTP服务器密码
     * @param remotePath
     * 远程文件路径
     * @param fileName
     * 待删除的文件名
     * @return
     * @see [类、类.方法、类.成员]
    </远程删除FTP服务器上的录音文件></删除FTP上的文件> */
    fun deleteFtpFile(
        url: String?, port: Int, username: String?,
        password: String?, remotePath: String, fileName: String
    ): Boolean {
        var success = false
        val ftp = FTPClient()
        try {

            // 连接FTP服务器
            if (port > -1) {
                ftp.connect(url, port)
            } else {
                ftp.connect(url)
            }

            // 登录
            ftp.login(username, password)
            val reply: Int = ftp.replyCode
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect()
                return success
            }

            // 转移到FTP服务器目录
            ftp.changeWorkingDirectory(remotePath)
            success = ftp.deleteFile("$remotePath/$fileName")
            ftp.logout()
        } catch (e: IOException) {
            e.printStackTrace()
            success = false
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return success
    }

    interface OnTranslateListener {
        fun OnTranslate(totalBytes: Long, transBytes: Long)
    }
}