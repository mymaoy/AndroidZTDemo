/**
 * @version $Id: BitmapImageFactory.java 169 2012-01-15 18:33:24Z mroland $
 *
 * @author Michael Roland <mi.roland@gmail.com>
 *
 * Copyright (c) 2012 Michael Roland
 *
 * ALL RIGHTS RESERVED.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name Michael Roland nor the names of any contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL MICHAEL ROLAND BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package at.mroland.android.apps.imagedecoder;

import at.mroland.android.imagedecoder.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import jj2000.JJ2000Frontend;

/**
 * Image container with meta information and pre-processing.
 */
public final class BitmapImageFactory extends BitmapImage {
  public static BitmapImageFactory get(byte[] rawImageData) {
    if (rawImageData == null) return null;
    
    Bitmap decodedImage = null;

    // try to decode JPEG2000
    decodedImage = JJ2000Frontend.decode(rawImageData);

    if (decodedImage != null) {
      return new BitmapImageFactory(BitmapImage.TYPE_JPEG2000, decodedImage);
    }

    
    // try to Android-supported image types
    decodedImage = BitmapFactory.decodeByteArray(rawImageData, 0, rawImageData.length);

    if (decodedImage != null) {
      // TODO: try to detect actual image type
      return new BitmapImageFactory(BitmapImage.TYPE_ANY, decodedImage);
    }
    
    return null;
  }

  
  private BitmapImageFactory(int imageType, Bitmap decodedImage) {
    super(imageType, decodedImage);
  }
}
