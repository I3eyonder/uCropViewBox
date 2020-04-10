# uCropViewBox
##### Customize [uCrop](https://github.com/Yalantis/uCrop) to support intercept touch event of GestureCropImageView and use difference files for input and output image.

##### Add UCropViewBox in case of you need to use uCrop as a single view.
**Implementation**
```
//Core library. Can be used standalone just like origin uCrop with ability to intercept touch for GestureCropImageView
implementation 'com.dev.hieupt:ucrop:2.2.4.1'
//uCropViewBox
implementation 'com.dev.hieupt:ucrop-view-box:1.4'
```
**Usage**
```
<com.hieupt.ucropviewbox.UCropViewBox
     android:id="@+id/uCropViewBox"
     android:layout_width="match_parent"
     android:layout_height="match_parent"
     app:cropFramePadding="8dp" />
```
