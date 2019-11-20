- 这是一个android camera2 API的demo；
- 通过使用`状态机`设计模式，让复杂的camera2更加简单地操作：更容易让人理解camera2；
- 本demo包含4种state模式，根据预览需求的session不同而创建(Camera2关键在于理解session和贴入的surface，因此参考AbstractStateBase中2个Surfaces的创建)；
- 由2种View（surfaceView、TextureView）定制预览。
	MediaRecord录像。
	ImageReader拍照。
- 基本涵盖了一般的需求功能。自认比Camera2Basic和Camera2Video简单，架构清晰。
- 包含很多小工具类。
- 详情：https://blog.csdn.net/jzlhll123/article/details/80700189
界面：
![预览1](https://github.com/jzlhll/AndroidCam2Demo/blob/master/Screenshots/a.png)
![预览2](https://github.com/jzlhll/AndroidCam2Demo/blob/master/Screenshots/b.png)

  备注：

   2019.11.17更新，代码架构和状态机代码优化。
   另外，Camera2其实已经臭名远扬，复杂的session和build，大量的回调，架构的复杂性可见一斑。并且由于legacy以及很多小芯片，小厂公司对Camera2架构的支持不够。兼容性也是一个问题。另外，学习Camera1不是什么坏事。
