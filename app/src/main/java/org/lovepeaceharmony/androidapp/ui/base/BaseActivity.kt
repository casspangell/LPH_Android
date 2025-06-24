package org.lovepeaceharmony.androidapp.ui.base

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import org.lovepeaceharmony.androidapp.utility.BetterActivityResult


abstract class BaseActivity : AppCompatActivity() {
    val activityLauncher by lazy { BetterActivityResult.registerActivityForResult(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display for Android 15+ (SDK 35+)
        // Note: enableEdgeToEdge() is deprecated in Android 15, but still works for backward compatibility
        // For Android 15+, edge-to-edge is enabled by default when targeting SDK 35
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            enableEdgeToEdge()
        }
        
        // Set up window insets handling using the latest APIs
        setupWindowInsets()
    }
    
    private fun setupWindowInsets() {
        // Use the latest WindowInsets API for Android 15+
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Apply padding to the root view to avoid content being hidden behind system bars
            view.updatePadding(
                top = insets.top,
                bottom = insets.bottom
            )
            
            WindowInsetsCompat.CONSUMED
        }
    }
}