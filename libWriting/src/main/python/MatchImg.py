'''
Author: liguoqiang
Date: 2021-01-01 22:41:06
LastEditors: liguoqiang
LastEditTime: 2021-04-27 15:26:16
Description: 
'''

import cv2 as cv
import numpy as np
import threading
import skimage.metrics as ssim

MinCx = 32
MinCy = 32
TotalSize = (MinCx*MinCy)

'''
    此类是用来解决：1、图片比对；2、坐标压感速度等检测；3、最后通过回调输出结果
'''
class MathTask(threading.Thread):
    def __init__(self, orgPng, orgPtLst, destPng, destPtLst, backCls) -> None:
        threading.Thread.__init__(self)
        self.orgPng = orgPng
        self.destPng = destPng
        self.orgPtLst = orgPtLst
        self.destPtLst = destPtLst
        self.backCls = backCls

    def start(self) -> bool:
        self.orgImg = cv.imread(self.orgPng, cv.IMREAD_UNCHANGED)
        self.destImg = cv.imread(self.destPng, cv.IMREAD_UNCHANGED)
        if self.orgImg is None or self.destImg is None:
            return False
        super().start()
        return True

    def __del__(self):
        try:
            self.join()
        except:
            print("close MatchThread")
        
    def run(self):
        self.matchWithHash()

    
    def matchWithHash(self):
        # qt 的pixmap格式转换成opencv格式
        #orgimg = convertImageToOpencv(self.orgPng)
        #destimg = convertImageToOpencv(self.destPng)
        # 加载图片
        
        # 图像默认为透明背景，增加白色背景，否则图像全部为黑色
        self.orgImg = addWhitebackground(self.orgImg)
        self.destImg = addWhitebackground(self.destImg)
        orgGray = cv.cvtColor(self.orgImg, cv.COLOR_BGR2GRAY)
        destGray = cv.cvtColor(self.destImg, cv.COLOR_BGR2GRAY)
        # 计算第一个图像轮廓，然后截取相同位置的图像然后比较
        # 采用轮廓算法逼近图形，获取坐标，裁剪后再进行比较
        # 两个图像裁剪相同的区域，裁剪成相同的大小
        # 先计算第一张图片轮廓，然
        # 后裁剪相同的区域
        newOrg = getImgUsingContour(orgGray)
        newDest = getImgUsingContour(destGray)
        score, diff = compareImgWithSSIM(newOrg, newDest)
        degree = score #(TotalSize - diff) / TotalSize
        print(score, degree)
        # cv.waitKey()
        # cv.destroyAllWindows()
        self.backCls.matchComplate(self.orgPng, degree, [])

# def convertImageToOpencv(pixmap):
#     qimg = pixmap.toImage()
#     temp_shape = (qimg.height(), qimg.bytesPerLine() * 8 // qimg.depth())
#     temp_shape += (4,)
#     ptr = qimg.bits()
#     ptr.setsize(qimg.byteCount())
#     result = np.array(ptr, dtype = np.uint8).reshape(temp_shape)
#     # 转换后，如果不需要背景通道的话，可以增加下面这行
#     #result = result[..., :3]
#     return result

# def convertOpencvToPixmap(cvimg):
#     height, width = cvimg.shape
#     depth = 3
#     cvimg = cv.cvtColor(cvimg, cv.COLOR_BGR2RGB)
#     qimg = QtGui.QPixmap.fromImage(QtGui.QImage(cvimg.data, width, height, width * depth, QtGui.QImage.Format_RGB888))
#     return qimg
    
# 为图片增加白色背景
def addWhitebackground(img):
    h,w = img.shape[:2]
    for cy in range(h):
        for cx in range(w):
            clr = img[cy, cx]
            if clr[3] == 0:
                img[cy, cx] = [255, 255, 255, 255]
    return img

# 计算图像轮廓，默认图像为白色背景黑色字体
# cv.THRESH_BINARY用来检测黑色背景白色图像
# cv.THRESH_BINARY_INV 用来检测白色背景黑色图像
def getContour(img):
    ret, thresh = cv.threshold(img, 200, 255, cv.THRESH_BINARY_INV)
    #contours, hierarchy = cv.findContours(thresh, cv.RETR_TREE, cv.CHAIN_APPROX_SIMPLE)
    contours, hierarchy = cv.findContours(thresh, cv.RETR_EXTERNAL, cv.CHAIN_APPROX_SIMPLE)
    return contours, hierarchy

#求平均哈希
def getAvgHash(image):
    avreage = np.mean(image)  #计算像素平均值
    hash = []
    h,w = image.shape
    for i in range(h):
        for j in range(w):
            if image[i, j] > avreage:
                hash.append(1)
            else:
                hash.append(0)
    return hash
# 计算差值哈希
# 计算差异值：dHash算法工作在相邻像素之间，这样每行9个像素之间产生了8个不同的差异，一共8行，则产生了64个差异值
# 获得指纹：如果左边的像素比右边的更亮，则记录为1，否则为0
def getdHash(img):
    hash = []
    h,w = img.shape
    for i in range(h):
        for j in range(w-1):
            if img[i, j] > img[i, j + 1]:
                hash.append(1)
            else:
                hash.append(0)
    return hash

# 计算汉明距离
def hammDistance(hash1, hash2):
    num = 0
    for index in range(len(hash1)):
        if hash1[index] != hash2[index]:
            num += 1
    print(num, len(hash1))
    return num

    
#差值哈希比较图像相似度,返回相似度
def getDistanceWithDHash(img1, img2):
    img1 = cv.resize(img1, (MinCx + 1, MinCy))
    img2 = cv.resize(img2, (MinCx + 1, MinCy))
    cv.imshow("img1", img1)
    cv.imshow("img2", img2)
    hash1 = getdHash(img1)
    hash2 = getdHash(img2)
    ret = hammDistance(hash1, hash2)
    return ret

# 首先获取图形的边沿轮廓，然后获取轮廓内图形
def getImgUsingContour(img):
    cnt, _ = getContour(img)
    left = top = right = bottom = 0
    first = True
    for item in iter(cnt):
        x,y,w,h=cv.boundingRect(item)
        if first:
            left = x
            top = y
            right = left + w
            bottom = top + h
            first = False
        else:
            if x < left:
                left = x
            if y < top:
                top = y
            r = x + w
            b = y + h
            if r > right:
                right = r
            if b > bottom:
                bottom = b
    newimg1 = img[top:bottom, left:right]
    # cv.imshow("newimg1", newimg1)
    # newimg = np.zeros([bottom - top, right - left], np.uint8)
    # newimg.fill(255)
    # cv.drawContours(newimg, cnt, -1, 0, offset=(-left, -top))
    # cv.imshow("newImg", newimg)
    # cv.waitKey()
    return newimg1

# 采用scikit-image库的compare_ssim进行相似度计算
def compareImgWithSSIM(img1, img2):
    img1 = cv.resize(img1, (MinCx, MinCy))
    img2 = cv.resize(img2, (MinCx, MinCy))
    (score, diff) = ssim.structural_similarity(img1, img2, full=True)
    return score, diff
