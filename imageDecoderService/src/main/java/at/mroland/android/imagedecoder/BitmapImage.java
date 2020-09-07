/**
 * @version $Id: BitmapImage.java 173 2012-01-15 21:06:34Z mroland $
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

package at.mroland.android.imagedecoder;

import android.os.Parcel;
import android.os.Parcelable;
import android.graphics.Bitmap;

/**
 * Image container with meta information and pre-processing.
 */
public class BitmapImage implements Parcelable {
  public static final int TYPE_ANY = 0;
  public static final int TYPE_JPEG = 1;
  public static final int TYPE_GIF = 2;
  public static final int TYPE_PNG = 3;
  public static final int TYPE_BMP = 4;
  public static final int TYPE_JPEG2000 = 5;
  
  
  private Bitmap mBitmapImage;
  private int mImageType;
  
  protected BitmapImage(int imageType, Bitmap decodedImage) {
    mImageType = imageType;
    mBitmapImage = decodedImage;
  }
  
  public final int getImageType() { return mImageType; }
  public final Bitmap getImage() { return mBitmapImage; }
  
  
  public static final Parcelable.Creator<BitmapImage> CREATOR = new Parcelable.Creator<BitmapImage>() {
    public BitmapImage createFromParcel(Parcel in) {
      return new BitmapImage(in);
    }

    public BitmapImage[] newArray(int size) {
      return new BitmapImage[size];
    }
  };
  
  private BitmapImage(Parcel in) {
    mImageType = in.readInt();
    mBitmapImage = in.readParcelable(null);
  }

  public void writeToParcel(Parcel out, int flags) {
    out.writeInt(mImageType);
    out.writeParcelable(mBitmapImage, 0);
  }

  public int describeContents() {
    return 0;
  }
}
