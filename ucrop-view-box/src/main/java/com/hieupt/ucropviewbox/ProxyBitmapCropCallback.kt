package com.hieupt.ucropviewbox

import com.yalantis.ucrop.callback.BitmapCropCallback

/**
 * Created by HieuPT on 3/22/2020.
 */
open class ProxyBitmapCropCallback(delegate: BitmapCropCallback) : BitmapCropCallback by delegate