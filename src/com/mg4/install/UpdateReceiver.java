// SPDX-License-Identifier: GPL-3.0-only
package com.mg4.install;
import android.content.*;
public class UpdateReceiver extends BroadcastReceiver {
 public void onReceive(Context c,Intent i){if(Intent.ACTION_MY_PACKAGE_REPLACED.equals(i.getAction()))InstallService.report(c,"Updated MG4 Install to "+SelfUpdater.version(c));}
}
