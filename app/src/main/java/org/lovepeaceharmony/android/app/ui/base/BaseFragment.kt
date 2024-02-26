package org.lovepeaceharmony.android.app.ui.base

import androidx.activity.result.ActivityResultCaller
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.olayg.network.ConnectivityStateHolder
import com.olayg.network.Event
import com.olayg.network.NetworkEvents
import org.lovepeaceharmony.android.app.utility.BetterActivityResult


abstract class BaseFragment : Fragment() {

    var networkConnected = false

    override fun onResume() {
        super.onResume()
        handleConnectivityChange()
    }

    fun observeNetworkEvents(owner: LifecycleOwner) = NetworkEvents.observe(owner) {
        if (it is Event.ConnectivityEvent) handleConnectivityChange()
    }

    private fun handleConnectivityChange() {
        networkConnected = ConnectivityStateHolder.isConnected
    }
}