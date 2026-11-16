// SPDX-License-Identifier: GPL-3.0-only
package com.mg4.install;
import android.content.*;
import android.content.pm.PackageInstaller;

public class InstallResult extends BroadcastReceiver {
    public void onReceive(Context c,Intent i) {
        int s=i.getIntExtra(PackageInstaller.EXTRA_STATUS,PackageInstaller.STATUS_FAILURE);
        if(s==PackageInstaller.STATUS_PENDING_USER_ACTION) {
            Intent confirm=i.getParcelableExtra(Intent.EXTRA_INTENT);
            if(confirm!=null) {
                try { confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);c.startActivity(confirm);InstallService.report(c,"Confirm installation in Android’s installer."); }
                catch(Exception e) { InstallService.report(c,"Unable to open Android installer: "+e.getMessage()); }
            }
            return;
        }
        InstallService.report(c,s==PackageInstaller.STATUS_SUCCESS?"Installed successfully · "+i.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME):"Installation failed · "+i.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE));
    }
}
