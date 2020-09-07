/**
 * @version $Id: ImageDecoder.java 177 2012-01-15 23:28:16Z mroland $
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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.util.List;

/**
 * Image decoder client.
 */
public final class ImageDecoder {
  public static final int INTERFACE_VERSION = 1;
  
  private Context mContext;
  private ServiceConnection mConnection;
  private IImageDecoderService mService;
  private boolean mIsBound;

  public ImageDecoder(Context context) {
    if (context == null) throw new IllegalArgumentException();

    mContext = context;
    mService = null;
    mIsBound = false;
  
    mConnection = new ServiceConnection() {
      public void onServiceConnected(ComponentName className, IBinder binder) {
        mService = IImageDecoderService.Stub.asInterface(binder);
      }

      public void onServiceDisconnected(ComponentName className) {
        mService = null;  //XXX: possible race condition
        mIsBound = false; //XXX: possible race condition
      }
    };
  }
  
  public boolean isSupported() {
    PackageManager pm = mContext.getPackageManager();
    
    if (pm != null) {
      List<ResolveInfo> svcs = pm.queryIntentServices(new Intent(IImageDecoderService.class.getName()), 0);
      
      if (svcs != null) {
        return !svcs.isEmpty();
      }
    }
    
    return false;
  }
  
  public void connect() {
    if (!mIsBound) {
      mService = null;

      mIsBound = mContext.bindService(
              new Intent(IImageDecoderService.class.getName()),
              mConnection,
              Context.BIND_AUTO_CREATE);
    }
  }
  
  public void close() {
    if ((mIsBound) && (mContext != null) && (mConnection != null)) {
      try {
        mContext.unbindService(mConnection);
      } catch (Exception e) {}
    }
    
    mService = null;  //XXX: possible race condition
    mIsBound = false; //XXX: possible race condition
  }

  public boolean isConnected() {
    return mIsBound && (mService != null);
  }
  
  public BitmapImage decodeImage(byte[] rawImageData) {
    if (mIsBound) {
      try {
        return mService.decodeImage(rawImageData);
      } catch (Exception e) {}
    }
    
    return null;
  }
}
