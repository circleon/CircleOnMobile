package com.developeek.circleon.domain.utils

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavOptions

object Utils {
    fun NavController.navigateWithoutAnimation(
        destinationId: Int,
        args: Bundle?,
    ) {
        val navOption =
            NavOptions
                .Builder()
                .setExitAnim(0)
                .setEnterAnim(0)
                .setPopExitAnim(0)
                .setPopEnterAnim(0)
                .build()

        navigate(destinationId, args, navOption)
    }
}
