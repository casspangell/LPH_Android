package org.lovepeaceharmony.androidapp.utility

import android.app.Activity
import android.content.pm.PackageManager

/**
 *
 * Created by Naveen Kumar M on 13/12/17.
 */
object PermissionUtil {

    /**
     * Check that all given permissions have been granted by verifying that each entry in the
     * given array is of the value [PackageManager.PERMISSION_GRANTED].
     *
     * @see Activity.onRequestPermissionsResult
     */
    fun verifyPermissions(grantResults: IntArray): Boolean {
        // At least one result must be checked.
        if (grantResults.isEmpty()) {
            return false
        }

        // Verify that each required permission has been granted, otherwise return false.
        return grantResults.none { it != PackageManager.PERMISSION_GRANTED }

//        for (result in grantResults) {
//            if (result != PackageManager.PERMISSION_GRANTED) {
//                return false
//            }
//        }


    }
}
